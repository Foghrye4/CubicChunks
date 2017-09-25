/*
 *  This file is part of Cubic Chunks Mod, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package cubicchunks.asm.mixin.fixes.common;

import javax.annotation.ParametersAreNonnullByDefault;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Mixin(PathNavigate.class)
public abstract class MixinPathNavigate {
	@Shadow
	protected EntityLiving entity;

	@Redirect(method = "getPathToPos", at = @At(value = "NEW", target = "net/minecraft/world/ChunkCache"))
	private ChunkCache newChunkCacheToPosRedirect(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn, BlockPos target) {
	    int x1 = (int)entity.posX;
        int y1 = (int)entity.posY;
        int z1 = (int)entity.posZ;
        int x2 = target.getX();
        int y2 = target.getY();
        int z2 = target.getZ();
        return new ChunkCache(worldIn, new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)), new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)), subIn);
	}
	
	@Redirect(method = "getPathToEntityLiving", at = @At(value = "NEW", target = "net/minecraft/world/ChunkCache"))
	private ChunkCache newChunkCacheToLivingRedirect(World worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn, Entity target) {
        int x1 = (int)entity.posX;
        int y1 = (int)entity.posY;
        int z1 = (int)entity.posZ;
        int x2 = (int)target.posX;
        int y2 = (int)target.posY;
        int z2 = (int)target.posZ;
        return new ChunkCache(worldIn, new BlockPos(Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2)), new BlockPos(Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)), subIn);
	}
}
