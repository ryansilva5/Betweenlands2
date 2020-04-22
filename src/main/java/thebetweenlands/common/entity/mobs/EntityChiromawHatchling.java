package thebetweenlands.common.entity.mobs;


import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.client.render.particle.BatchedParticleRenderer;
import thebetweenlands.client.render.particle.DefaultParticleBatches;
import thebetweenlands.client.render.particle.ParticleFactory.ParticleArgs;
import thebetweenlands.common.entity.EntityProximitySpawner;
import thebetweenlands.common.registries.ItemRegistry;

public class EntityChiromawHatchling extends EntityProximitySpawner {

	public int prevRise;
	public float feederRotation, prevFeederRotation;
	public float headPitch, prevHeadPitch;
	public int eatingCooldown;
	public final int MAX_EATING_COOLDOWN = 240; // set to whatever time between hunger cycles
	public final int MIN_EATING_COOLDOWN = 0;
	public final int MAX_RISE = 40;
	public final int MIN_RISE = 0; 
	public final int MAX_FOOD_NEEDED = 1; // amount of times needs to be fed
	public int prevTransformTick;

	private static final DataParameter<Boolean> IS_RISING = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> RISE_COUNT = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_HUNGRY = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> FOOD_COUNT = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> IS_CHEWING = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> TRANSFORM = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> TRANSFORM_COUNT = EntityDataManager.createKey(EntityChiromawHatchling.class, DataSerializers.VARINT);
	public EntityChiromawHatchling(World world) {
		super(world);
		setSize(1F, 1F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(IS_RISING, false);
		dataManager.register(RISE_COUNT, 0);
		dataManager.register(IS_HUNGRY, true);
		dataManager.register(FOOD_COUNT, 0);
		dataManager.register(IS_CHEWING, false);
		dataManager.register(TRANSFORM, false);
		dataManager.register(TRANSFORM_COUNT, 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		prevRise = getRiseCount();
		prevFeederRotation = feederRotation;
		prevHeadPitch = headPitch;
		prevTransformTick = getTransformCount();
		if(!getEntityWorld().isRemote)
			checkArea();

		if (getEntityWorld().isRemote) {
			checkFeeder();
			if (getRising() && getRiseCount() >= MAX_RISE) {
				if (!getIsHungry())
					if (headPitch < 40)
						headPitch += 8;
				if (getIsHungry())
					if (headPitch > 0)
						headPitch -= 8;
			}

			if (!getRising() && getRiseCount() < MAX_RISE)
				headPitch = getRiseCount();

			if (getAmountEaten() >= MAX_FOOD_NEEDED && !getIsChewing()) {
				spawnLightningArcs(); // TODO maybe else something to show this is ready to transform/transforming
			}

			if (getIsChewing()) {
				if (getTransformCount() < 60)
					spawnEatingParticles();
			}
		}

		if (!getEntityWorld().isRemote) {

			if (!getRising() && getRiseCount() > MIN_RISE)
				setRiseCount(getRiseCount() - 4);

			if (getRising() && getRiseCount() < MAX_RISE)
				setRiseCount(getRiseCount() + 4);

			if (!getIsHungry()) {
				eatingCooldown--;
				if (eatingCooldown <= MAX_EATING_COOLDOWN && eatingCooldown > MAX_EATING_COOLDOWN - 60 && !getIsChewing())
					setIsChewing(true);
				if (eatingCooldown < MAX_EATING_COOLDOWN - 60 && getIsChewing())
					setIsChewing(false);
				if (eatingCooldown <= MIN_EATING_COOLDOWN && getAmountEaten() < MAX_FOOD_NEEDED)
					setIsHungry(true);
			}

			if (!getEntityWorld().isRemote && getIsTransforming())
				if (getTransformCount() <= 60)
					setTransformCount(getTransformCount() + 1);
		}
	}

	private void spawnEatingParticles() {
		// TODO 
		
	}

	@SideOnly(Side.CLIENT)
	private void spawnLightningArcs() {
		if(this.world.rand.nextInt(2) == 0) {
			float ox = this.world.rand.nextFloat() - 0.5f;
			float oy = this.world.rand.nextFloat() - 0.5f + ((float)this.motionY + getTransformCount() * 0.02F);
			float oz = this.world.rand.nextFloat() - 0.5f;
			
			Particle particle = BLParticles.LIGHTNING_ARC.create(this.world, this.posX, this.posY + 0.5F + getTransformCount() * 0.02F, this.posZ, 
					ParticleArgs.get()
					.withMotion(this.motionX, this.motionY, this.motionZ)
					.withColor(0.3f, 0.5f, 1.0f, 0.9f)
					.withData(new Vec3d(this.posX + ox, this.posY + oy, this.posZ + oz)));
			
			BatchedParticleRenderer.INSTANCE.addParticle(DefaultParticleBatches.BEAM, particle);
		}
	}

	protected Entity checkFeeder() {
		Entity entity = null;
			List<EntityPlayer> list = getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, proximityBox());
			for (int entityCount = 0; entityCount < list.size(); entityCount++) {
				entity = list.get(entityCount);
				if (entity != null)
					if (entity instanceof EntityPlayer)
						if (!isDead && getRiseCount() >= MAX_RISE)
							lookAtFeeder(entity, 30F);
			}

			if (entity == null && getRiseCount() > MIN_RISE)
				feederRotation = updateFeederRotation(feederRotation, 0F, 30F);

		return entity;
	}

	@Override
	protected Entity checkArea() {
		Entity entity = null;
		if (!getEntityWorld().isRemote) {// && getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL) {
			List<EntityPlayer> list = getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, proximityBox());
			for (int entityCount = 0; entityCount < list.size(); entityCount++) {
				entity = list.get(entityCount);
				if (entity != null) {
					if (entity instanceof EntityPlayer) {// && !((EntityPlayer) entity).isSpectator() && !((EntityPlayer) entity).isCreative()) {
						if (canSneakPast() && entity.isSneaking())
							return null;
						else if (checkSight() && !canEntityBeSeen(entity))
							return null;
						else {
							if(!getRising())
								setRising(true);
						}
						if (!isDead && getRiseCount() >= MAX_RISE) {
							if (getAmountEaten() >= MAX_FOOD_NEEDED && eatingCooldown <= 0) {
								Entity spawn = getEntitySpawned();
								if (spawn != null) {
									performPreSpawnaction(entity, spawn);
									if (!isDead && getTransformCount() >= 60) {
										if (!spawn.isDead) { // just in case
											getEntityWorld().spawnEntity(spawn);
										}
										performPostSpawnaction(entity, spawn);
										setDead();
									}
								}
							}
						}
					}
				}
			}
			if (entity == null && getRiseCount() > MIN_RISE && !getIsTransforming()) {
				if (getRising())
					setRising(false);
			}
		}
		return entity;
	}

	@Override
	protected void performPreSpawnaction(Entity targetEntity, Entity entitySpawned) {
		if(!getIsTransforming())
			setTransformAnimation(true);
		EntityLiving entityliving = (EntityLiving)entitySpawned;
		((EntityChiromawTame) entityliving).setOwnerId(targetEntity.getUniqueID()); // just for now
		double distanceX = targetEntity.posX - posX;
		double distanceZ = targetEntity.posZ - posZ;
		float angle = (float) (MathHelper.atan2(distanceZ, distanceX) * (180D / Math.PI)) - 90F;
		entityliving.setLocationAndAngles(getPosition().getX() + 0.5F, getPosition().getY() + 1F, getPosition().getZ() + 0.5F, MathHelper.wrapDegrees(angle), 0.0F);
		// mojang pls - why wont it spawn rotated?
	}

	public void lookAtFeeder(Entity entity, float maxYawIncrease) {
		double distanceX = entity.posX - posX;
		double distanceZ = entity.posZ - posZ;
		float angle = (float) (MathHelper.atan2(distanceZ, distanceX) * (180D / Math.PI)) - 90.0F;
		feederRotation = updateFeederRotation(feederRotation, angle, maxYawIncrease);
	}

	private float updateFeederRotation(float angle, float targetAngle, float maxIncrease) {
		float f = MathHelper.wrapDegrees(targetAngle - angle);
		if (f > maxIncrease)
			f = maxIncrease;
		if (f < -maxIncrease)
			f = -maxIncrease;
		return angle + f;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.isEmpty() && getIsHungry()) {
			if (stack.getItem() == ItemRegistry.SNAIL_FLESH_RAW) {
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
					if (stack.getCount() <= 0)
						player.setHeldItem(hand, ItemStack.EMPTY);
				}
				eatingCooldown = MAX_EATING_COOLDOWN;
				setAmountEaten(getAmountEaten() + 1);
				setIsHungry(false);
				return true;
			}
		}
		return super.processInteract(player, hand);
	}

	@Nullable
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		if (!getEntityWorld().isRemote)
			setLocationAndAngles(posX, posY, posZ, 0F, 0.0F); // stahp random rotating on spawn with an egg mojang pls
		return livingdata;
	}

	private void setRising(boolean rise) {
		dataManager.set(IS_RISING, rise);
	}

    public boolean getRising() {
        return dataManager.get(IS_RISING);
    }

	private void setRiseCount(int riseCountIn) {
		dataManager.set(RISE_COUNT, riseCountIn);
	}

	public int getRiseCount() {
		return dataManager.get(RISE_COUNT);
	}

	private void setAmountEaten(int foodIn) {
		dataManager.set(FOOD_COUNT, foodIn);
	}

	private int getAmountEaten() {
		return dataManager.get(FOOD_COUNT);
	}

	private void setIsHungry(boolean hungry) {
		dataManager.set(IS_HUNGRY, hungry);
	}

	public boolean getIsHungry() {
		return dataManager.get(IS_HUNGRY);
	}

	private void setIsChewing(boolean chewing) {
		dataManager.set(IS_CHEWING, chewing);
	}

	public boolean getIsChewing() {
		return dataManager.get(IS_CHEWING);
	}

	private void setTransformAnimation(boolean transform) {
		dataManager.set(TRANSFORM, transform);
	}

	public boolean getIsTransforming() {
		return dataManager.get(TRANSFORM);
	}
	
	private void setTransformCount(int transformCountIn) {
		dataManager.set(TRANSFORM_COUNT, transformCountIn);
	}

	public int getTransformCount() {
		return dataManager.get(TRANSFORM_COUNT);
	}

	@Override
	public void onKillCommand() {
		setDead();
	}

	@Override
	protected boolean isMovementBlocked() {
		return true;
	}

	@Override
    public boolean canBePushed() {
        return false;
    }

	@Override
    public boolean canBeCollidedWith() {
        return true;
    }

	@Override
	public void addVelocity(double x, double y, double z) {
	}

	@Override
	public boolean getIsInvulnerable() {
		return false;
	}

	@Override
	protected float getProximityHorizontal() {
		return 5F;
	}

	@Override
	protected float getProximityVertical() {
		return 1F;
	}

	@Override
	protected AxisAlignedBB proximityBox() {
		return new AxisAlignedBB(getPosition()).grow(getProximityHorizontal(), getProximityVertical(), getProximityHorizontal());
	}

	@Override
	protected boolean canSneakPast() {
		return true;
	}

	@Override
	protected boolean checkSight() {
		return true;
	}

	@Override
	protected Entity getEntitySpawned() {
		EntityChiromawTame entity = new EntityChiromawTame(getEntityWorld());
		return entity;
	}

	@Override
	protected int getEntitySpawnCount() {
		return 1;
	}

	@Override
	protected boolean isSingleUse() {
		return true;
	}

	@Override
	protected int maxUseCount() {
		return 1;
	}
}