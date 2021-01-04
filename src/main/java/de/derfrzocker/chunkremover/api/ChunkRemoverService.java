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

package de.derfrzocker.chunkremover.api;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ChunkRemoverService {

    /**
     * @param name           of the chunk validator
     * @param chunkValidator to register
     * @throws IllegalArgumentException if name or chunk validator is null
     * @throws IllegalArgumentException if a chunk validator with the same name is already registered
     */
    void registerChunkValidator(@NotNull String name, @NotNull ChunkValidator chunkValidator);

    /**
     * @param world         from the chunk
     * @param chunkPosition of the chunk in question
     * @return true if the chunk should get generated, otherwise false
     * @throws IllegalArgumentException if world or chunkPosition is null
     */
    boolean shouldGenerate(@NotNull World world, @NotNull ChunkPosition chunkPosition);

    /**
     * @param worldName from the world
     * @param worldData to set
     * @return the old world data of the world if present or null if not
     * @throws IllegalArgumentException if worldName or worldData is null
     */
    @Nullable
    WorldData setWorldData(@NotNull String worldName, @NotNull WorldData worldData);

    /**
     * @param worldName to get the WorldData from
     * @return the WorldData for the given world name or null if not present
     * @throws IllegalArgumentException if worldName is null
     */
    @Nullable
    WorldData getWorldData(@NotNull String worldName);

    /**
     * @param worldName to remove
     * @throws IllegalArgumentException if worldName is null
     */
    void removeWorldData(@NotNull String worldName);

    /**
     * @return a new Set with all world data world names
     */
    @NotNull
    Set<String> getWorldDataNames();

}
