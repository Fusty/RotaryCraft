/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft.TileEntities.Piping;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import Reika.RotaryCraft.Auxiliary.Interfaces.PumpablePipe;
import Reika.RotaryCraft.Base.TileEntity.TileEntityPiping;
import Reika.RotaryCraft.Registry.MachineRegistry;

public class TileEntityHose extends TileEntityPiping implements PumpablePipe {

	private int lubricant = 0;

	@Override
	public MachineRegistry getMachine() {
		return MachineRegistry.HOSE;
	}

	@Override
	public boolean canConnectToPipe(MachineRegistry m) {
		return m == MachineRegistry.HOSE || m == MachineRegistry.VALVE || m == MachineRegistry.SEPARATION || m == MachineRegistry.SUCTION;
	}

	@Override
	public IIcon getBlockIcon() {
		return Blocks.planks.getIcon(0, 0);
	}

	@Override
	public boolean hasLiquid() {
		return lubricant > 0;
	}

	@Override
	public Fluid getFluidType() {
		return this.hasLiquid() ? FluidRegistry.getFluid("lubricant") : null;
	}

	@Override
	public int getFluidLevel() {
		return lubricant;
	}

	@Override
	protected void setFluid(Fluid f) { }

	@Override
	protected void setLevel(int amt) {
		lubricant = amt;
	}

	@Override
	protected boolean interactsWithMachines() {
		return true;
	}

	@Override
	protected void onIntake(TileEntity te) {

	}

	@Override
	public boolean isValidFluid(Fluid f) {
		return f.equals(FluidRegistry.getFluid("lubricant"));
	}

	@Override
	public boolean canReceiveFromPipeOn(ForgeDirection side) {
		return true;
	}

	@Override
	public boolean canEmitToPipeOn(ForgeDirection side) {
		return true;
	}

	@Override
	public Block getPipeBlockType() {
		return Blocks.planks;
	}

	@Override
	public boolean canIntakeFromIFluidHandler(ForgeDirection side) {
		return side.offsetY != 0;
	}

	@Override
	public boolean canOutputToIFluidHandler(ForgeDirection side) {
		return side.offsetY == 0;
	}

	@Override
	public boolean canTransferTo(PumpablePipe p, ForgeDirection dir) {
		return p instanceof TileEntityHose;
	}

	@Override
	public void transferFrom(PumpablePipe from, int amt) {
		((TileEntityHose)from).lubricant -= amt;
		lubricant += amt;
	}
}
