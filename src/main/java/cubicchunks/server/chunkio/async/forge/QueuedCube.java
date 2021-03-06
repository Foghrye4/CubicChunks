/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package cubicchunks.server.chunkio.async.forge;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import cubicchunks.world.ICubicWorld;
import mcp.MethodsReturnNonnullByDefault;

/**
 * Taking from Sponge, with modifications
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
class QueuedCube {
	final int x;
	final int y;
	final int z;
	@Nonnull final ICubicWorld world;

	QueuedCube(int x, int y, int z, ICubicWorld world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}

	@Override
	public int hashCode() {
		return (x*31 + y*23*z*29) ^ world.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (object instanceof QueuedCube) {
			QueuedCube other = (QueuedCube) object;
			return x == other.x && y == other.y && z == other.z && world == other.world;
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.addValue(this.world)
			.add("x", this.x)
			.add("y", this.y)
			.add("z", this.z)
			.toString();
	}
}
