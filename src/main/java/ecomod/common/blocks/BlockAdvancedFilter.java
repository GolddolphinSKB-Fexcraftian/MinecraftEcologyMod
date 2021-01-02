package ecomod.common.blocks;

import ecomod.api.EcomodBlocks;
import ecomod.api.EcomodItems;
import ecomod.api.EcomodStuff;
import ecomod.common.tiles.TileAdvancedFilter;
import ecomod.core.EMConsts;
import ecomod.core.EcologyMod;
import ecomod.core.stuff.EMConfig;
import ecomod.api.pollution.PollutionData.PollutionType;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import scala.reflect.internal.Trees.New;
import net.minecraft.util.ChatComponentText;

public class BlockAdvancedFilter extends Block implements ITileEntityProvider{

	public BlockAdvancedFilter()
	{
		super(Material.iron);
		this.setCreativeTab(EcomodStuff.ecomod_creative_tabs);
		
		this.setHardness(15F);
		this.setResistance(10F);
		
		this.setHarvestLevel("pickaxe", 2);
	}

	public boolean isNormalCube()
    {
        return false;
    }

    public boolean isOpaqueCube()
    {
        return false;
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileAdvancedFilter();
	}

	@Override
	public MapColor getMapColor(int meta) {
		return MapColor.diamondColor;
	}
	
	public String getUnlocalizedName()
	{
		return "tile."+EMConsts.modid+".advanced_filter";
	}
	
	@Override
    public int getRenderBlockPass()
    {
        return 0;
    }
	
	@Override
	public int getRenderType()
    {
        return 2634;
    }
	
	@Override
	public boolean renderAsNormalBlock()
    {
        return false;
    }
	
	@Override
	public IIcon getIcon(int side, int meta)
	{
		return EcomodBlocks.FRAME.getIcon(side, 1);
	}

	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer e, int metadata, float sidex,
			float sidey, float sidez) {
		if (w.isRemote) {
			return true;
		}
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileAdvancedFilter) {
			if (((TileAdvancedFilter) te).hasEnoughEnergy() && ((TileAdvancedFilter) te).canSeeDaylight() && ((TileAdvancedFilter) te).hasEnoughTankSpace()) {
				e.addChatComponentMessage(new ChatComponentText("[Advanced Filter] Able to extract."));

			} else if (((TileAdvancedFilter) te).canSeeDaylight()) {
				e.addChatComponentMessage(new ChatComponentText("[Advanced Filter] The filter needs to see daylight!"));
			} else if (((TileAdvancedFilter) te).hasEnoughEnergy()) {
				e.addChatComponentMessage(
						new ChatComponentText("[Advanced Filter] The filter does not have enough energy to run!"));
			} else if (((TileAdvancedFilter) te).hasEnoughTankSpace()) {
				e.addChatComponentMessage(
						new ChatComponentText("[Advanced Filter] The filter is full of polluted liquid!"));
				e.addChatComponentMessage(
						new ChatComponentText("[Advanced Filter] You need to extract the liquid for the filter to work!"));
			}

			e.addChatComponentMessage(new ChatComponentText(
					"[Advanced Filter] Will extract the following pollution per " + EMConfig.adv_filter_delay_secs + " seconds"));
			e.addChatComponentMessage(
					new ChatComponentText("Air: " + ((TileAdvancedFilter) te).getSource().get(PollutionType.AIR)));
			e.addChatComponentMessage(
					new ChatComponentText("Water: " + ((TileAdvancedFilter) te).getSource().get(PollutionType.WATER)));
			e.addChatComponentMessage(
					new ChatComponentText("Soil: " + ((TileAdvancedFilter) te).getSource().get(PollutionType.SOIL)));
			e.addChatComponentMessage(new ChatComponentText(
					"[Advanced Filter] Will use " + EMConfig.advanced_filter_energy_per_second + " RF per second"));
		}
		return true;
	}
}
