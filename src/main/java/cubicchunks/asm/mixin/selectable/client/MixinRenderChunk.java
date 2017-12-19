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
package cubicchunks.asm.mixin.selectable.client;

import static cubicchunks.client.RenderConstants.RENDER_CHUNK_CENTER_POS;
import static cubicchunks.client.RenderConstants.RENDER_CHUNK_MAX_POS_OFFSET;
import static cubicchunks.client.RenderConstants.RENDER_CHUNK_SIZE;

import javax.annotation.ParametersAreNonnullByDefault;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.chunk.RenderChunk;
/**
 * Fixes renderEntities crashing when rendering cubes
 * that are not at existing array index in chunk.getEntityLists(),
 * <p>
 * Allows to render cubes outside of 0..256 height range.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mixin(RenderChunk.class)
public class MixinRenderChunk {

    @ModifyConstant(method = "setPosition", constant = @Constant(intValue = 16))
    public int onSetPosition(int oldValue) {
        return RENDER_CHUNK_SIZE;
    }
    
    @ModifyConstant(method = "rebuildChunk", constant = @Constant(intValue = 15))
    public int onRebuildChunk(int oldValue) {
        return RENDER_CHUNK_MAX_POS_OFFSET;
    }
    
    @ModifyConstant(method = "rebuildWorldView", constant = @Constant(intValue = 16))
    public int onRebuildWorldView(int oldValue) {
        return RENDER_CHUNK_SIZE;
    }
    
    @ModifyConstant(method = "getDistanceSq", constant = @Constant(doubleValue = 8.0D))
    public double onDistanceSq(double oldValue) {
        return RENDER_CHUNK_CENTER_POS;
    }
}
