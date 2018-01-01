package ecomod.common.tiles;

import org.apache.commons.lang3.tuple.Pair;

import cofh.api.energy.IEnergyReceiver;
import cofh.api.energy.IEnergyStorage;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import ecomod.common.utils.EMEnergyStorage;
import ecomod.common.utils.EMUtils;
import ecomod.common.utils.newmc.EMBlockPos;
import ecomod.core.stuff.EMConfig;
import ecomod.network.EMPacketHandler;
import ecomod.network.EMPacketUpdateTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;


@cpw.mods.fml.common.Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")
public class TileEnergy extends TileEntity implements IEnergyReceiver, IEnergyStorage, ic2.api.energy.tile.IEnergySink
{
	EMEnergyStorage energy;
	
	public TileEnergy(int max_energy)
	{
		super();
		energy = new EMEnergyStorage(max_energy, true);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		energy = energy.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		energy.writeToNBT(nbt);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return energy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) 
	{
		return 0;
	}

	@Override
	public int getEnergyStored() 
	{
		return energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored() 
	{
		return energy.getMaxEnergyStored();
	}
	
	public Pair<Integer, Integer> getChunkCoords()
	{
		return EMUtils.blockPosToPair(new EMBlockPos(xCoord, yCoord, zCoord));
	}
	
	public void sendUpdatePacket()
	{
		EMPacketHandler.WRAPPER.sendToAllAround(new EMPacketUpdateTileEntity(this), new TargetPoint(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 8));
	}
	
	public void receiveUpdatePacket(EMPacketUpdateTileEntity packet)
	{
		readFromNBT(packet.getData());
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
		return EMConfig.eu_to_rf_conversion != 0;
	}

	@Override
	public double getDemandedEnergy() {
		return Math.min(Math.max(this.getMaxEnergyStored() - this.getEnergyStored(), 0) / 4D, 512D);
	}

	public int getSinkTier() {
		return 2;
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
		energy.receiveEnergy((int)Math.floor(amount * EMConfig.eu_to_rf_conversion), false);
		return 0;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return energy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return energy.getMaxEnergyStored();
	}
	
	public EMBlockPos getPos()
	{
		return new EMBlockPos(xCoord, yCoord, zCoord);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		
		if(!worldObj.isRemote)
		if(EMConfig.eu_to_rf_conversion != 0 && Loader.isModLoaded("IC2"))
		{
			MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileUnloadEvent(this));
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		
		if(!worldObj.isRemote)
		if(EMConfig.eu_to_rf_conversion != 0 && Loader.isModLoaded("IC2"))
		{
			MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileUnloadEvent(this));
		}
	}
	
	private boolean ic2_tile_loaded = false;

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!worldObj.isRemote)
		if(!ic2_tile_loaded && Loader.isModLoaded("IC2") && EMConfig.eu_to_rf_conversion != 0)
		{
			MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileLoadEvent(this));
			ic2_tile_loaded = true;
		}
	}
	
	
}
