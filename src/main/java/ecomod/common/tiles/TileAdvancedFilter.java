package ecomod.common.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import buildcraft.api.tiles.IHasWork;
import ecomod.api.EcomodAPI;
import ecomod.api.EcomodStuff;
import ecomod.api.pollution.PollutionData;
import ecomod.api.pollution.PollutionData.PollutionType;
import ecomod.common.pollution.PollutionUtils;
import ecomod.common.pollution.config.PollutionSourcesConfig;
import ecomod.common.utils.EMUtils;
import ecomod.core.EMConsts;
import ecomod.core.EcologyMod;
import ecomod.core.stuff.EMConfig;
import ecomod.core.stuff.EMIntermod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "buildcraft.api.tiles.IHasWork", modid = "buildcraft")
public class TileAdvancedFilter extends TileEnergy implements IFluidHandler, IHasWork
{
	public FluidTank tank;
	
	public int ticks = 0;
	
	public TileAdvancedFilter()
	{
		super(EMConfig.adv_filter_energy);
		
		tank = new AdvFilterTank(EMConfig.adv_filter_capacity, this);
	}

	@Override
	public boolean hasWork()
	{
		return isWorking();
	}
	
	public  boolean was_working = false;
	private int i1 = 0;
	
	public float vent_rotation = 0F;
	private float rps = 0F;
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(ticks > (20 * EMConfig.adv_filter_delay_secs) && (ticks - i1) > 340)
		{
			ticks = 0;
			i1 = 0;
		}
		
		if(worldObj.isRemote)
		{
			if(was_working)
			{
				if(rps < EMConfig.advanced_filter_max_rps)
					rps += 1 / (4F * 20F);
			}
			else
			{
				if(rps >= 0)
					rps -= 1 / (2F * 20F);
			}
			if(rps < 0)
				rps = 0;
			
			vent_rotation = MathHelper.wrapAngleTo180_float(vent_rotation + 360 * rps / 20F);
		}
		
		if(ticks % (20 * EMConfig.adv_filter_delay_secs) == 0)
		{	
			if(worldObj.isRemote)
			{
				
			}
			else
			{
				sendUpdatePacket();
				if(isWorking())
				{
					if(!was_working)
					{
						was_working = true;
						i1 = ticks;
					}
					
					if(energy.extractEnergyNotOfficially(EMConfig.advanced_filter_energy_per_second * EMConfig.adv_filter_delay_secs, false) == EMConfig.advanced_filter_energy_per_second * EMConfig.adv_filter_delay_secs)
					{
						EcomodAPI.emitPollution(worldObj, getChunkCoords(), getSource(), false);
						
						tank.fill(getProduction(), true);
					
						EMUtils.pushFluidAround(worldObj, getPos(), tank);
					}
				}
				else
				{
					if(was_working)
					{
						was_working = false;
						i1 = 0;
					}
				}
			}
		}
		
		if(!worldObj.isRemote)
				if(was_working)
				{
					if((ticks - i1) % (340) == 0)//17*20
						worldObj.playSoundEffect(xCoord+0.5D, yCoord+0.5D, zCoord+0.5D, "ecomod:advanced_filter_working", 2F, 1F);
				}
		
		++ticks;
	}
	
	public PollutionData getSource() {
		return PollutionSourcesConfig.getSource("advanced_filter_reduction");
	}
	
	public boolean isWorking()
	{
		boolean ret = true;
		
		ret &= canSeeDaylight();
		
		if(ret)
		ret &= worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		
		if(ret)
		ret &= hasEnoughEnergy();
		
		if(ret)
		ret &= getProduction() != null;
		
		if(ret)
		ret &= hasEnoughTankSpace();
		
		return ret;
	}

	public boolean canSeeDaylight() {
		return PollutionUtils.hasSurfaceAccess(worldObj, getPos());
	}

	public boolean hasEnoughEnergy() {
		return this.getEnergyStored() >= (EMConfig.advanced_filter_energy_per_second * EMConfig.adv_filter_delay_secs);
	}

	public boolean hasEnoughTankSpace() {
		return tank.getCapacity() >= getProduction().amount + tank.getFluidAmount();
	}
	
	public FluidStack getProduction()
	{
		PollutionData pd = EcomodAPI.getPollution(worldObj, getChunkCoords().getLeft(), getChunkCoords().getRight());
		
		if(pd == null || pd == PollutionData.getEmpty())
			return null;
		
		FluidStack ret = new FluidStack(EcomodStuff.concentrated_pollution, 0);
		
		PollutionData advanced_filter_reduction = getSource();
		
		for(PollutionType type : PollutionType.values())
		{
			if(pd.get(type) >= advanced_filter_reduction.get(type))
			{
				switch(type)
				{
					case AIR:
						ret.amount += advanced_filter_reduction.get(type);
						break;
						
					case WATER:
						ret.amount += advanced_filter_reduction.get(type) * 2;
						break;
						
					case SOIL:
						ret.amount += advanced_filter_reduction.get(type) * 4;
						break;
				}
			}
		}
		
		ret.amount = -ret.amount;
		
		if(ret.amount <= 0)
			return null;
		
		return ret;
	}
	
	
	@Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        tank.readFromNBT(tag);
        was_working = tag.getBoolean("was_working");
    }
	
	@Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tank.writeToNBT(tag);
        tag.setBoolean("was_working", was_working);
    }
    
    private static class AdvFilterTank extends FluidTank
    {
    	public AdvFilterTank(int capacity, TileAdvancedFilter tile)
        {
            super(capacity);
            
            this.tile = tile;
        }
    }
    
    @Override
	public int getSinkTier() {
		return 3;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}
}
