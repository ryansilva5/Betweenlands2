package thebetweenlands.recipes;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thebetweenlands.items.BLItemRegistry;

/**
 * Created by Bart on 07/12/2015.
 */
public class BookCloneRecipe implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world) {
        int i = 0;
        ItemStack itemstack = null;

        for (int j = 0; j < inventoryCrafting.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inventoryCrafting.getStackInSlot(j);

            if (itemstack1 != null) {
                if (itemstack1.getItem() == BLItemRegistry.manualGuideBook || itemstack1.getItem() == BLItemRegistry.manualHL) {
                    if (itemstack != null) {
                        return false;
                    }

                    itemstack = itemstack1;
                } else {
                    if (itemstack1.getItem() != Items.book) {
                        return false;
                    }

                    ++i;
                }
            }
        }

        return itemstack != null && i > 0;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
        int i = 0;
        ItemStack itemstack = null;

        for (int j = 0; j < inventoryCrafting.getSizeInventory(); ++j) {
            ItemStack itemstack1 = inventoryCrafting.getStackInSlot(j);

            if (itemstack1 != null) {
                if (itemstack1.getItem() == BLItemRegistry.manualGuideBook || itemstack1.getItem() == BLItemRegistry.manualGuideBook) {
                    if (itemstack != null) {
                        return null;
                    }

                    itemstack = itemstack1;
                } else {
                    if (itemstack1.getItem() != Items.book) {
                        return null;
                    }

                    ++i;
                }
            }
        }

        if (itemstack != null && i >= 1) {
            ItemStack itemstack2;
            if (itemstack.getItem() == BLItemRegistry.manualGuideBook)
                itemstack2 = new ItemStack(BLItemRegistry.manualGuideBook, i + 1);
            else
                itemstack2 = new ItemStack(BLItemRegistry.manualHL, i + 1);
            if (itemstack.getTagCompound() != null)
                itemstack2.setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());

            if (itemstack.hasDisplayName())
                itemstack2.setStackDisplayName(itemstack.getDisplayName());

            return itemstack2;
        }

        return null;
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }
}
