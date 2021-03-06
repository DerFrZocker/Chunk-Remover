/*
 * MIT License
 *
 * Copyright (c) 2021 Marvin (DerFrZocker)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.derfrzocker.chunkremover.impl.v1_16_R3;

import net.minecraft.server.v1_16_R3.ChunkStatus;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.RegionLimitedWorldAccess;
import net.minecraft.server.v1_16_R3.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

public class SpawnChunkWorldAccess extends RegionLimitedWorldAccess {

    private final WorldServer worldServer;

    public SpawnChunkWorldAccess(WorldServer worldserver, List<IChunkAccess> list) {
        super(worldserver, list);

        this.worldServer = worldserver;
    }

    @Nullable
    @Override
    public IChunkAccess getChunkAt(int x, int z, ChunkStatus chunkstatus, boolean flag) {
        IChunkAccess iChunkAccess = super.getChunkAt(x, z, chunkstatus, false);

        if (iChunkAccess != null || !flag) {
            return iChunkAccess;
        }

        return worldServer.getChunkAt(x, z, ChunkStatus.STRUCTURE_STARTS, flag);
    }

}
