package cubicchunks.debug.item;

import cubicchunks.CubicChunks;
import cubicchunks.debug.ItemRegistered;
import cubicchunks.network.PacketCube;
import cubicchunks.network.PacketDispatcher;
import cubicchunks.util.CubeCoords;
import cubicchunks.world.ICubeCache;
import cubicchunks.world.ICubicWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class CheckLightDownwardsItem extends ItemRegistered {

	public CheckLightDownwardsItem(String name) {
		super(name);
	}

	@Override public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing faceHit, float hitX, float hitY, float hitZ) {
		ICubicWorld world = (ICubicWorld) worldIn;
		if(!world.isCubicWorld() || world.isRemote()) {
			return EnumActionResult.PASS;
		}
		//serverside
		BlockPos placePos = pos.offset(faceHit);
		for(int y = 0; y > -64; y--) {
			BlockPos checkPos = placePos.add(0, y, 0);
			CubicChunks.DEBUG_REMOVE_BEFORE_COMMIT = true;
			world.checkLightFor(EnumSkyBlock.SKY, checkPos);
			CubicChunks.DEBUG_REMOVE_BEFORE_COMMIT = false;
		}
		ICubeCache cubeCache = world.getCubeCache();

		for(int y = 0; y >= -64; y -= 16) {
			CubeCoords coords = CubeCoords.fromBlockCoords(pos.getX(), pos.getY() + y, pos.getZ());
			coords.forEachWithinRange(1,
					(p)-> PacketDispatcher.sendTo(new PacketCube(cubeCache.getCube(p), PacketCube.Type.UPDATE), (EntityPlayerMP) playerIn));
		}
		playerIn.addChatMessage(new TextComponentString("Successfully updated lighting column starting from " + placePos));

		return EnumActionResult.PASS;
	}

}
