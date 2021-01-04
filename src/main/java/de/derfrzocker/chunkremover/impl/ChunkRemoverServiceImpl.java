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

package de.derfrzocker.chunkremover.impl;

import de.derfrzocker.chunkremover.api.*;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkRemoverServiceImpl implements ChunkRemoverService {

    private final Map<String, ChunkValidator> chunkValidators = new HashMap<>();
    private final Map<String, WorldData> worldDatas = new ConcurrentHashMap<>();

    @Override
    public void registerChunkValidator(@NotNull String name, @NotNull ChunkValidator chunkValidator) {
        Validate.notNull(name, "Chunk Validator name cannot be null");
        Validate.notNull(chunkValidator, "ChunkValidator cannot be null");
        Validate.isTrue(!chunkValidators.containsKey(name), "A ChunkValidator with the name '" + name + "' is already registered");

        chunkValidators.put(name, chunkValidator);
    }

    @Override
    public boolean shouldGenerate(@NotNull World world, @NotNull ChunkPosition chunkPosition) {
        WorldData worldData = worldDatas.get(world.getName());
        if (worldData == null) {
            return true;
        }

        ChunkValidator chunkValidator = chunkValidators.get(worldData.getChunkValidatorName());
        if (chunkValidator == null) {
            //TODO log
            return true;
        }

        return chunkValidator.shouldGenerate(new WorldInfo(world.getName(), world.getSeed(), worldData), chunkPosition);
    }

    @Nullable
    @Override
    public WorldData setWorldData(@NotNull String worldName, @NotNull WorldData worldData) {
        Validate.notNull(worldName, "World name cannot be null");
        Validate.notNull(worldData, "WorldData cannot be null");

        return worldDatas.put(worldName, worldData);
    }

    @Nullable
    @Override
    public WorldData getWorldData(@NotNull String worldName) {
        Validate.notNull(worldName, "World name cannot be null");

        return worldDatas.get(worldName);
    }

    @Override
    public void removeWorldData(@NotNull String worldName) {
        Validate.notNull(worldName, "World name cannot be null");

        worldDatas.remove(worldName);
    }

    @NotNull
    @Override
    public Set<String> getWorldDataNames() {
        return new HashSet<>(worldDatas.keySet());
    }

}
