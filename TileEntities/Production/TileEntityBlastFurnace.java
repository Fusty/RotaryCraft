/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities.Production;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import Reika.DragonAPI.Interfaces.XPProducer;
import Reika.DragonAPI.Libraries.ReikaInventoryHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.RotaryCraft.Auxiliary.ItemStacks;
import Reika.RotaryCraft.Auxiliary.TemperatureTE;
import Reika.RotaryCraft.Base.TileEntity.InventoriedRCTileEntity;
import Reika.RotaryCraft.Registry.DifficultyEffects;
import Reika.RotaryCraft.Registry.MachineRegistry;

public class TileEntityBlastFurnace extends InventoriedRCTileEntity implements TemperatureTE, XPProducer {

	private int temperature;
	public ItemStack[] inventory = new ItemStack[14];
	public int meltTime = 0;

	public static final int SMELTTEMP = 600;
	public static final int MAXTEMP = 1200;
	public static final float SMELT_XP = 0.6F;

	private float xp;

	@Override
	protected int getActiveTexture() {
		return (temperature >= SMELTTEMP && this.haveIngredients() ? 1 : 0);
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		tickcount++;
		if (tickcount >= 20) {
			this.updateTemperature(world, x, y, z, meta);
			tickcount = 0;
		}
		if (temperature < SMELTTEMP) {
			meltTime = 0;
			return;
		}
		if (!this.haveIngredients()) {
			meltTime = 0;
			return;
		}
		meltTime++;
		if (meltTime >= this.getMeltTime()) {
			this.smelt();
		}
	}

	private boolean haveIngredients() {
		if (inventory[0] == null)
			return false;
		if (inventory[0].itemID != Item.coal.itemID)
			return false;
		if (inventory[11] == null)
			return false;
		if (inventory[11].itemID != Item.gunpowder.itemID)
			return false;


		if (inventory[10] != null) {
			if (inventory[10].itemID != ItemStacks.steelingot.itemID || inventory[10].getItemDamage() != ItemStacks.steelingot.getItemDamage() || inventory[10].stackSize+9 >= ItemStacks.steelingot.getMaxStackSize()) {
				if (inventory[13] != null) {
					if (inventory[13].itemID != ItemStacks.steelingot.itemID || inventory[13].getItemDamage() != ItemStacks.steelingot.getItemDamage() || inventory[13].stackSize+9 >= ItemStacks.steelingot.getMaxStackSize()) {
						if (inventory[12] != null) {
							if (inventory[12].itemID != ItemStacks.steelingot.itemID || inventory[12].getItemDamage() != ItemStacks.steelingot.getItemDamage() || inventory[12].stackSize+9 >= ItemStacks.steelingot.getMaxStackSize()) {
								return false;
							}
						}
					}
				}
			}
		}
		if (!ReikaInventoryHelper.checkForItem(Item.ingotIron.itemID, inventory))
			return false;
		return true;
	}

	public int getTemperatureScaled(int p1) {
		return ((p1*temperature)/MAXTEMP);
	}

	private void smelt() {
		meltTime = 0;
		if (worldObj.isRemote)
			return;
		ReikaInventoryHelper.decrStack(0, inventory);
		int num = ReikaInventoryHelper.countNumStacks(Item.ingotIron.itemID, -1, inventory);
		if ((int)Math.sqrt(num) > 1 && rand.nextInt(3) == 0) {
			if (rand.nextInt((int)Math.sqrt(num)) > 0) {
				ReikaInventoryHelper.decrStack(11, inventory);
			}
		}
		if (ReikaRandomHelper.doWithChance(DifficultyEffects.BONUSSTEEL.getDouble()*ReikaMathLibrary.intpow(1.005, num*num))) {
			num *= 1+rand.nextFloat();
		}
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.valueOf(num));
		if (!ReikaInventoryHelper.addOrSetStack(ItemStacks.steelingot.itemID, num, ItemStacks.steelingot.getItemDamage(), inventory, 10))
			if (!ReikaInventoryHelper.addOrSetStack(ItemStacks.steelingot.itemID, num, ItemStacks.steelingot.getItemDamage(), inventory, 12))
				if (!ReikaInventoryHelper.addOrSetStack(ItemStacks.steelingot.itemID, num, ItemStacks.steelingot.getItemDamage(), inventory, 13))
					if (!this.checkSpreadFit(num))
						return;
		for (int i = 1; i < inventory.length-1; i++) {
			if (inventory[i] != null) {
				if (inventory[i].itemID == Item.ingotIron.itemID) {
					ReikaInventoryHelper.decrStack(i, inventory);
				}
			}
		}
		xp += SMELT_XP*num;
	}

	public void dropXP() {
		ReikaWorldHelper.splitAndSpawnXP(worldObj, xCoord+rand.nextFloat(), yCoord+1.25F, zCoord+rand.nextFloat(), (int)xp);
		xp = 0;
	}

	public float getXP() {
		return xp;
	}

	public void clearXP() {
		xp = 0;
	}

	public void addXPToPlayer(EntityPlayer ep) {
		ep.addExperience((int)xp);
		this.clearXP();
	}

	private boolean checkSpreadFit(int num) {
		int maxfit = 0;
		int f1 = ItemStacks.steelingot.getMaxStackSize()-inventory[10].stackSize;
		int f2 = ItemStacks.steelingot.getMaxStackSize()-inventory[12].stackSize;
		int f3 = ItemStacks.steelingot.getMaxStackSize()-inventory[13].stackSize;
		maxfit = f1+f2+f3;
		if (num > maxfit)
			return false;
		if (f1 > num) {
			inventory[10].stackSize += num;
			return true;
		}
		else {
			inventory[10].stackSize = inventory[10].getMaxStackSize();
			num -= f1;
		}
		if (f2 > num) {
			inventory[12].stackSize += num;
			return true;
		}
		else {
			inventory[12].stackSize = inventory[12].getMaxStackSize();
			num -= f2;
		}
		if (f3 > num) {
			inventory[12].stackSize += num;
			return true;
		}
		else {
			inventory[13].stackSize = inventory[13].getMaxStackSize();
			num -= f3;
		}
		return true;
	}

	public int getMeltTime() {
		int time = 2*((MAXTEMP-(temperature-SMELTTEMP))/12);
		if (time < 1)
			return 1;
		return time;
	}

	public int getCookScaled(int p1) {
		if (temperature < SMELTTEMP)
			return 0;
		return ((p1*meltTime)/this.getMeltTime());
	}

	public void updateTemperature(World world, int x, int y, int z, int meta) {
		int Tamb = ReikaWorldHelper.getBiomeTemp(world, x, z);

		ForgeDirection waterside = ReikaWorldHelper.checkForAdjMaterial(world, x, y, z, Material.water);
		if (waterside != null) {
			Tamb /= 2;
		}
		ForgeDirection iceside = ReikaWorldHelper.checkForAdjBlock(world, x, y, z, Block.ice.blockID);
		if (iceside != null) {
			if (Tamb > 0)
				Tamb /= 4;
			ReikaWorldHelper.changeAdjBlock(world, x, y, z, iceside, Block.waterMoving.blockID, 0);
		}
		ForgeDirection fireside = ReikaWorldHelper.checkForAdjBlock(world, x, y, z, Block.fire.blockID);
		if (fireside != null) {
			Tamb += 200;
		}
		ForgeDirection lavaside = ReikaWorldHelper.checkForAdjMaterial(world, x, y, z, Material.lava);
		if (lavaside != null) {
			Tamb += 600;
		}
		if (temperature > Tamb)
			temperature--;
		if (temperature > Tamb*2)
			temperature--;
		if (temperature < Tamb)
			temperature++;
		if (temperature*2 < Tamb)
			temperature++;
		if (temperature > MAXTEMP)
			temperature = MAXTEMP;
		if (temperature > 100) {
			ForgeDirection side = ReikaWorldHelper.checkForAdjBlock(world, x, y, z, Block.snow.blockID);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, x, y, z, side, 0, 0);
			side = ReikaWorldHelper.checkForAdjBlock(world, x, y, z, Block.ice.blockID);
			if (side != null)
				ReikaWorldHelper.changeAdjBlock(world, x, y, z, side, Block.waterMoving.blockID, 0);
		}
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory()
	{
		return inventory.length;
	}

	/**
	 * Returns the stack in slot i
	 */
	public ItemStack getStackInSlot(int par1)
	{
		return inventory[par1];
	}

	/**
	 *
	 */
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		inventory[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound NBT)
	{
		super.writeToNBT(NBT);
		NBT.setInteger("melt", meltTime);
		NBT.setInteger("temp", temperature);
		NBT.setFloat("exp", xp);

		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inventory.length; i++)
		{
			if (inventory[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				inventory[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		NBT.setTag("Items", nbttaglist);
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound NBT)
	{
		super.readFromNBT(NBT);
		meltTime = NBT.getInteger("melt");
		temperature = NBT.getInteger("temp");
		xp = NBT.getFloat("exp");

		NBTTagList nbttaglist = NBT.getTagList("Items");
		inventory = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); i++)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
			byte byte0 = nbttagcompound.getByte("Slot");

			if (byte0 >= 0 && byte0 < inventory.length)
			{
				inventory[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack is) {
		if (is == null)
			return false;
		if (i == 0)
			return is.itemID == Item.coal.itemID;
		else if (i == 11)
			return is.itemID == Item.gunpowder.itemID;
		else if (i <= 9)
			return is.itemID == Item.ingotIron.itemID;
		else
			return false;
	}

	@Override
	public boolean hasModelTransparency() {
		return false;
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getMachineIndex() {
		return MachineRegistry.BLASTFURNACE.ordinal();
	}

	@Override
	public int getThermalDamage() {
		return 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return i == 10 || i == 12 || i == 13;
	}

	@Override
	public int getRedstoneOverride() {
		if (!this.haveIngredients())
			return 15;
		return 0;
	}

	@Override
	public void addTemperature(int temp) {
		temperature += temp;
	}

	@Override
	public int getTemperature() {
		return temperature;
	}

	@Override
	public void overheat(World world, int x, int y, int z) {

	}

	@Override
	public void onEMP() {}
}
