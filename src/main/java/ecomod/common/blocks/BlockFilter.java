package ecomod.common.blocks;

import ecomod.api.EcomodBlocks;
import ecomod.api.EcomodItems;
import ecomod.api.EcomodStuff;
import ecomod.common.tiles.TileFilter;
import ecomod.core.EMConsts;
import ecomod.core.stuff.EMConfig;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class BlockFilter extends Block implements ITileEntityProvider {

	public BlockFilter() {
		super(Material.rock);
		this.setCreativeTab(EcomodStuff.ecomod_creative_tabs);
		this.setHardness(8F);
		this.setResistance(5F);
		
		this.setHarvestLevel("pickaxe", 1);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileFilter();
	}


	@Override
	public boolean isBlockNormalCube() {
		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}


	@Override
	public boolean isOpaqueCube() {
		return false;
	}


	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		return false;
	}
	
	@Override
	public String getUnlocalizedName() {
		return "tile."+EMConsts.modid+".filter";
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
		return EcomodBlocks.FRAME.getIcon(side, 0);
	}

	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer e, int metadata, float sidex, float sidey, float sidez) {
		if (w.isRemote) {
			return true;
		}
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileFilter) {
			if (((TileFilter) te).hasEnoughEnergy(x, y, z)) {
				e.addChatComponentMessage(new ChatComponentText("[Basic Filter] Extracting correctly."));

			} else {
				e.addChatComponentMessage(new ChatComponentText("[Basic Filter] Cannot filter. Not enough energy!"));
			}

			e.addChatComponentMessage(new ChatComponentText(
					"[Basic Filter] Will extract 1 pollution per second emitted from adjacent blocks every tick."));
			e.addChatComponentMessage(new ChatComponentText(
					"[Basic Filter] Will use " + EMConfig.filter_energy_per_emission + " RF per pollution removed."));
		}
		return true;
	}
}
