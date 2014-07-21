package mod.alternative;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * This is your basic Minecraft block, again if you don't know what you're doing here,
 * leave now and learn to mod for Minecraft first
 *
 * @author theoriginalbit
 */
public class BlockAlternative extends BlockContainer {

    public BlockAlternative() {
        super(1451, Material.rock);
        setCreativeTab(CreativeTabs.tabBlock);
        setUnlocalizedName("examplemod.alternative");
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
            return new TileAlternative();
        }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister registry) {
        blockIcon = registry.registerIcon("examplemod:alternative");
    }
}
