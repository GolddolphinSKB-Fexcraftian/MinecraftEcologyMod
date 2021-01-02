package ecomod.common.pollution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import buildcraft.api.tiles.IHasWork;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.registry.GameRegistry;
import ecomod.api.EcomodStuff;
import ecomod.api.pollution.IRespirator;
import ecomod.api.pollution.PollutionData;
import ecomod.common.utils.EMUtils;
import ecomod.common.utils.newmc.EMBlockPos;
import ecomod.core.EMConsts;
import ecomod.core.stuff.EMIntermod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import ecomod.core.stuff.EMIntermod;
import cpw.mods.fml.common.Loader;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler.CrafterPatternInventory;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFurnaceHeater;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityThermoelectricGen;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnaceAdvanced;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;

import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;

public class PollutionUtils
{
	public static Chunk coordsToChunk(World w, Pair<Integer, Integer> coord)
	{
		return w.getChunkFromChunkCoords(coord.getLeft(), coord.getRight());
	}
	
	public static boolean isTEWorking(World w, TileEntity te)
	{	
		//TODO add more checks
		if(te instanceof TileEntityFurnace)
		{
			return ((TileEntityFurnace)te).isBurning();
		}
		
		if(ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tiles") && te instanceof IHasWork)
		{
			return ((IHasWork)te).hasWork();
		}

		if (Loader.isModLoaded("ThermalExpansion") && te instanceof IHasWork) {
			return ((IHasWork)te).hasWork();
		}

		if (Loader.isModLoaded("ImmersiveEngineering")) {
			if (te instanceof TileEntityArcFurnace) {
				return ((TileEntityArcFurnace)te).active;
			} else if (te instanceof TileEntityAssembler) {
				for(int p=0; p<((TileEntityAssembler)te).patterns.length; p++)
				{
					CrafterPatternInventory pattern = ((TileEntityAssembler)te).patterns[p];
					if(pattern.inv[9]!=null && ((TileEntityAssembler)te).canOutput(pattern.inv[9], p))
						return true;
				}
				return false;
			} else if (te instanceof TileEntityBlastFurnacePreheater) {
				return ((TileEntityBlastFurnacePreheater)te).active;
			} else if (te instanceof TileEntityCrusher) {
				return ((TileEntityCrusher)te).active;
			} else if (te instanceof TileEntityDieselGenerator) {
				return ((TileEntityDieselGenerator)te).active;
			} else if (te instanceof TileEntityExcavator) {
				return ((TileEntityExcavator)te).active;
			} else if (te instanceof TileEntityFurnaceHeater) {
				return ((TileEntityFurnaceHeater)te).active;
			} else if (te instanceof TileEntityRefinery) {
				return ((TileEntityRefinery)te).tank2.getFluidAmount() > 0;
			} else if (te instanceof TileEntityThermoelectricGen) {
				TileEntityThermoelectricGen ete = (TileEntityThermoelectricGen)te;
				int energy = 0;
				for(ForgeDirection fd : new ForgeDirection[]{ForgeDirection.UP,ForgeDirection.SOUTH,ForgeDirection.EAST})
					if(!w.isAirBlock(te.xCoord+fd.offsetX, te.yCoord+fd.offsetY, te.zCoord+fd.offsetZ) && w.isAirBlock(te.xCoord+fd.getOpposite().offsetX, te.yCoord+fd.getOpposite().offsetY, te.zCoord+fd.getOpposite().offsetZ))
					{
						int temp0 = getTemperature(te.xCoord+fd.offsetX, te.yCoord+fd.offsetY, te.zCoord+fd.offsetZ, w);
						int temp1 = getTemperature(te.xCoord+fd.getOpposite().offsetX, te.yCoord+fd.getOpposite().offsetY, te.zCoord+fd.getOpposite().offsetZ, w);
						if(temp0>-1&&temp1>-1)
						{
							int diff = Math.abs(temp0-temp1);
							energy += (int) diff;
						}
					}
				return energy > 0;

			} else if (te instanceof TileEntityBlastFurnace) {
				return ((TileEntityBlastFurnace)te).active;	
			} else if (te instanceof TileEntityBlastFurnaceAdvanced) {
				return ((TileEntityBlastFurnaceAdvanced)te).active;
			} else if (te instanceof TileEntityCokeOven) {
				return ((TileEntityCokeOven)te).active;
			}

		}
		
		return true;
	}

	public static int getTemperature(int x, int y, int z, World w)
	{
		Fluid f = getFluid(x,y,z, w);
		if(f!=null)
			return f.getTemperature(w, x, y, z);
		return ThermoelectricHandler.getTemperature(w.getBlock(x,y,z), w.getBlockMetadata(x,y,z));
	}
	public static Fluid getFluid(int x, int y, int z, World w)
	{
		Block b = w.getBlock(x, y, z);
		Fluid f = FluidRegistry.lookupFluidForBlock(b);
		if(f==null && b instanceof BlockDynamicLiquid && w.getBlockMetadata(x, y, z)==0)
			if(b.getMaterial().equals(Material.water))
				f = FluidRegistry.WATER;
			else if(b.getMaterial().equals(Material.lava))
				f = FluidRegistry.LAVA;
		if(b instanceof IFluidBlock && !((IFluidBlock)b).canDrain(w, x, y, z))
			return null;
		if(b instanceof BlockStaticLiquid && w.getBlockMetadata(x, y, z)!=0)
			return null;
		if(f==null)
			return null;
		return f;
	}
	
	public static PollutionData pollutionMaxFrom(PollutionData a, PollutionData b)
	{
		for(PollutionData.PollutionType type : PollutionData.PollutionType.values())
		{
			if(b.get(type) > a.get(type))
				a.set(type, b.get(type));
		}
		
		return a;
	}
	
	public static final ForgeDirection horizontal_facings[] = new ForgeDirection[]{ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.WEST, ForgeDirection.SOUTH};
	
	private static boolean hasSurfaceAccess(World w, int x, int y, int z, HashMap<Long, HashSet<Integer>> was_at)
	{
		long xz = (long)x << 32 | z & 0xffffffffL;
        HashSet<Integer> ys = was_at.get(xz);
		if(ys != null && ys.contains(y))
			return false;

		int bpup = y+1;
		if(w.canBlockSeeTheSky(x, y, z)) {
			if (isBlockHollow(w, x, bpup, z, ForgeDirection.UP))
				return true;
		} else if(isBlockHollow(w, x, bpup, z, ForgeDirection.UP) && hasSurfaceAccess(w, x, bpup, z, was_at))
			return true;
		
		for(ForgeDirection facing : horizontal_facings)
		{
			int bx = x + facing.offsetX;
			int by = y + facing.offsetY;
			int bz = z + facing.offsetZ;
			
            HashSet<Integer> bys = was_at.get((long)bx << 32 | bz & 0xffffffffL);
			if (bys != null && bys.contains(by))
				continue;
			if(isBlockHollow(w, bx, by, bz, facing))
			{
				if(w.canBlockSeeTheSky(bx, by, bz))
					return true;
				else {
					if (isBlockHollow(w, bx, by+1, bz, ForgeDirection.UP) && hasSurfaceAccess(w, bx, by+1, bz, was_at))
						return true;
				}
			}
		}

		if(ys == null) {
			ys = new HashSet<Integer>();
			ys.add(y);
			was_at.put(xz, ys);
		} else
			ys.add(y);
		return false;
	}
	
	public static boolean isBlockHollow(World world, int x, int y, int z, ForgeDirection direction)
	{
		int rv3 = isBlockAirPenetratorCFG(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
		
		if(rv3 > 0)
			return true;
		else if(rv3 < 0)
			return false;
		
		AxisAlignedBB aabb = world.getBlock(x, y, z).getCollisionBoundingBoxFromPool(world, x, y, z);
		
		if(aabb == null)
			return true;
		
		if(aabb == AxisAlignedBB.getBoundingBox(-1, -1, -1, 1, 1, 1))
			return false;

		if(direction == ForgeDirection.WEST || direction == ForgeDirection.EAST)
		{
			return aabb.maxY - aabb.minY < 0.9D || aabb.maxZ - aabb.minZ < 0.9D;
		}
		else if(direction == ForgeDirection.DOWN || direction == ForgeDirection.UP)
		{
			return aabb.maxX - aabb.minX < 0.9D || aabb.maxZ - aabb.minZ < 0.9D;
		}
		else if(direction == ForgeDirection.NORTH || direction == ForgeDirection.SOUTH)
		{
			return aabb.maxY - aabb.minY < 0.9D || aabb.maxX - aabb.minX < 0.9D;
		}

		return false;
	}
	
	
	/**
	 * @return 0 if the block custom air penetration value is undefined. 1 if block is penetrating air, -1 if not.
	 */
	public static int isBlockAirPenetratorCFG(Block block, int meta)
	{
		if(block == null || EcomodStuff.additional_blocks_air_penetrating_state == null)
			return 0;
		
		String searchkey = GameRegistry.findUniqueIdentifierFor(block).toString();
		Boolean statekey = EcomodStuff.additional_blocks_air_penetrating_state.get(searchkey);
		if(statekey != null)
			return statekey ? 1 : -1;
		String searchmeta ="@"+meta;
		
		statekey = EcomodStuff.additional_blocks_air_penetrating_state.get(searchkey + searchmeta);
		if (statekey != null)
			return statekey ? 1 : -1;
		
		return 0;
	}
	
	public static boolean hasSurfaceAccess(World w, int x, int y, int z)
	{	
		return hasSurfaceAccess(w, x, y, z, new HashMap<Long, HashSet<Integer>>());
	}
	
	public static boolean hasSurfaceAccess(World w, EMBlockPos bp)
	{	
		return hasSurfaceAccess(w, bp.getX(), bp.getY(), bp.getZ());
	}
	
	public static boolean isEntityRespirating(EntityLivingBase entity)
	{
		return isEntityRespirating(entity, true);
	}
	
	public static boolean isEntityRespirating(EntityLivingBase entity, boolean decr)
	{
		ItemStack is = entity.getEquipmentInSlot(4);
		
		return is != null && is.getItem() instanceof IRespirator && ((IRespirator) is.getItem()).isRespirating(entity, is, decr);
	}
}
