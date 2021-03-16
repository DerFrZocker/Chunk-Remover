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

import de.derfrzocker.chunkremover.api.ChunkRemoverService;
import de.derfrzocker.chunkremover.api.Dimension;
import de.derfrzocker.chunkremover.api.WorldData;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class WorldHandler_v1_16_R3 implements Listener {

    @NotNull
    private final Plugin plugin;
    @NotNull
    private final Supplier<ChunkRemoverService> serviceSupplier;

    private final Map<String, ChunkGenerator> chunkOverriders = new ConcurrentHashMap<>();

    public WorldHandler_v1_16_R3(@NotNull Plugin plugin, @NotNull Supplier<ChunkRemoverService> serviceSupplier) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(serviceSupplier, "Service supplier cannot be null");

        this.plugin = plugin;
        this.serviceSupplier = serviceSupplier;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!chunkOverriders.containsKey(event.getWorld().getName())) {
            inject(event.getWorld());

            WorldData worldData = serviceSupplier.get().getWorldData(event.getWorld().getName());
            if (event.isNewChunk() && worldData != null && worldData.shouldAffectSpawnChunks()) {
                // only affect spawn chunks when the first chunk is new
                handleNewGenerated(event.getChunk());
            }

        }
    }

    private void inject(World bukkitWorld) {
        // checking if the Bukkit world is an instance of CraftWorld, if not return
        if (!(bukkitWorld instanceof CraftWorld)) {
            return;
        }
        final CraftWorld world = (CraftWorld) bukkitWorld;

        try {
            // get the playerChunkMap where the ChunkGenerator is store, that we need to override
            final PlayerChunkMap playerChunkMap = world.getHandle().getChunkProvider().playerChunkMap;

            // get the ChunkGenerator from the PlayerChunkMap
            final Field ChunkGeneratorField = PlayerChunkMap.class.getDeclaredField("chunkGenerator");
            ChunkGeneratorField.setAccessible(true);
            final Object chunkGeneratorObject = ChunkGeneratorField.get(playerChunkMap);

            // return, if the chunkGeneratorObject is not an instance of ChunkGenerator
            if (!(chunkGeneratorObject instanceof ChunkGenerator)) {
                return;
            }

            final ChunkGenerator chunkGenerator = (ChunkGenerator) chunkGeneratorObject;

            // create a new ChunkOverrider
            final ChunkGenerator overrider = new ChunkOverrider(chunkGenerator, bukkitWorld, getDimension(world.getHandle()), serviceSupplier);
            chunkOverriders.put(world.getName(), overrider);

            // set the ChunkOverrider to the PlayerChunkMap
            ChunkGeneratorField.set(playerChunkMap, overrider);

        } catch (final Exception e) {
            throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
        }
    }

    private Dimension getDimension(WorldServer worldServer) {
        ResourceKey<DimensionManager> dimensionKey = worldServer.getTypeKey();

        if (dimensionKey == DimensionManager.OVERWORLD) {
            return Dimension.OVERWORLD;
        }
        if (dimensionKey == DimensionManager.THE_NETHER) {
            return Dimension.NETHER;
        }
        if (dimensionKey == DimensionManager.THE_END) {
            return Dimension.THE_END;
        }

        return Dimension.CUSTOM;
    }

    private void handleNewGenerated(Chunk chunk) {
        WorldServer worldServer = ((CraftWorld) chunk.getWorld()).getHandle();
        ChunkGenerator chunkOverrider = chunkOverriders.get(chunk.getWorld().getName());
        ChunkCoordIntPair[] chunkCoordIntPairs = new ChunkCoordIntPair[25];
        IChunkAccess[] odlChunkAccesses = new IChunkAccess[25];
        IChunkAccess[] newChunkAccesses = new IChunkAccess[25];

        { // filling arrays
            int i = 0;
            // one chunk generates a 21x21 area wth structure start
            // and a 5x5 area with liquid caves, which we want to replace
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    ChunkCoordIntPair chunkCoordIntPair = chunkCoordIntPairs[i] = new ChunkCoordIntPair(chunk.getX() + x, chunk.getZ() + z);
                    IChunkAccess oldIChunkAccess = odlChunkAccesses[i] = worldServer.getChunkAt(chunk.getX() + x, chunk.getZ() + z, ChunkStatus.EMPTY);
                    ProtoChunk newChunkAccess = (ProtoChunk) (newChunkAccesses[i] = new ProtoChunk(chunkCoordIntPair, ChunkConverter.a));
                    i++;

                    // coping structure start, structure references and biomes, since we don't need do generated them again

                    // coping structure start
                    newChunkAccess.a(oldIChunkAccess.h());

                    // coping structure reference
                    newChunkAccess.b(oldIChunkAccess.v());

                    // coping biomes
                    newChunkAccess.a(oldIChunkAccess.getBiomeIndex());
                    newChunkAccess.a(ChunkStatus.BIOMES);


                    RegionLimitedWorldAccess worldAccess = new SpawnChunkWorldAccess(worldServer, Arrays.asList(newChunkAccess));

                    // generating noise
                    chunkOverrider.buildNoise(worldAccess, new StructureManager(worldServer, worldServer.worldDataServer.getGeneratorSettings()), newChunkAccess);
                    newChunkAccess.a(ChunkStatus.NOISE);

                    // generating surface
                    chunkOverrider.buildBase(worldAccess, newChunkAccess);
                    newChunkAccess.a(ChunkStatus.SURFACE);

                    // generating normal caves
                    chunkOverrider.doCarving(worldServer.getSeed(), worldServer.d(), newChunkAccess, WorldGenStage.Features.AIR);
                    newChunkAccess.a(ChunkStatus.CARVERS);

                    // generating liquid caves
                    chunkOverrider.doCarving(worldServer.getSeed(), worldServer.d(), newChunkAccess, WorldGenStage.Features.LIQUID);
                    newChunkAccess.a(ChunkStatus.LIQUID_CARVERS);
                }
            }
        }

        // generating feature's
        {
            // first feature chunk is at the position 6
            int i = 6;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    // feature chunks need a one thick chunk buffer
                    IChunkAccess[] buffer = new IChunkAccess[9];
                    int bufferI = 0;
                    int indexXZ = 0;
                    for (int bufferX = -1; bufferX <= 1; bufferX++) {
                        for (int bufferZ = -1; bufferZ <= 1; bufferZ++) {
                            buffer[bufferI] = newChunkAccesses[i - 6 + indexXZ];
                            indexXZ++;
                            bufferI++;
                        }
                        indexXZ += 2;
                    }

                    List<IChunkAccess> bufferList = Arrays.asList(buffer);
                    ((ProtoChunk) newChunkAccesses[i]).a(worldServer.getChunkProvider().getLightEngine());
                    HeightMap.a(newChunkAccesses[i], EnumSet.of(HeightMap.Type.MOTION_BLOCKING, HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, HeightMap.Type.OCEAN_FLOOR, HeightMap.Type.WORLD_SURFACE));
                    RegionLimitedWorldAccess worldAccess = new RegionLimitedWorldAccess(worldServer, bufferList);

                    chunkOverrider.addDecorations(worldAccess, new StructureManager(worldAccess, worldServer.worldDataServer.getGeneratorSettings()));
                    ((ProtoChunk) newChunkAccesses[i]).a(ChunkStatus.FEATURES);
                    i++;
                }
                // we skip 1 chunk on each site which equals 2
                i += 2;
            }
        }

        // generating center chunk full
        ProtoChunk protoChunk = (ProtoChunk) newChunkAccesses[12];

        // generating light
        protoChunk.a(ChunkStatus.LIGHT);
        Bukkit.getScheduler().runTaskLater(plugin, () -> worldServer.getChunkProvider().getLightEngine().b(protoChunk.getPos(), false), 10);

        // spawn mobs
        chunkOverrider.addMobs(new RegionLimitedWorldAccess(worldServer, Collections.singletonList(protoChunk)));
        protoChunk.a(ChunkStatus.HEIGHTMAPS);
        net.minecraft.server.v1_16_R3.Chunk fullChunk = new net.minecraft.server.v1_16_R3.Chunk(worldServer, protoChunk);
        net.minecraft.server.v1_16_R3.Chunk oldChunk = ((net.minecraft.server.v1_16_R3.ProtoChunkExtension) odlChunkAccesses[12]).u();

        // coping old to new data
        ChunkAccessReflectionUtil.copy(oldChunk, fullChunk);

        // removing old height maps
        ChunkAccessReflectionUtil.removeHeightMaps(oldChunk);

        // coping height maps
        fullChunk.f().forEach((entry) -> oldChunk.a(entry.getKey()).a(entry.getValue().a()));

        // coping new chunk data to old chunk data
        {
            int i = 0;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (i != 12) {
                        ProtoChunk oldIChunkAccess = (ProtoChunk) odlChunkAccesses[i];
                        ProtoChunk newChunkAccess = (ProtoChunk) newChunkAccesses[i];

                        ChunkStatus oldStatus = oldIChunkAccess.getChunkStatus();
                        // only coping to old chunk if on right chunk status
                        if (((x == -2 || x == 2 || z == -2 || z == 2) && oldStatus == ChunkStatus.LIQUID_CARVERS) || ((x != -2 && x != 2 && z != -2 && z != 2) && oldStatus == ChunkStatus.FEATURES)) {

                            // coping old to new data
                            ChunkAccessReflectionUtil.copy(oldIChunkAccess, newChunkAccess);

                            // removing old height maps
                            ChunkAccessReflectionUtil.removeHeightMaps(protoChunk);

                            // coping height maps
                            newChunkAccess.f().forEach((entry) -> oldIChunkAccess.a(entry.getKey(), entry.getValue().a()));
                        }
                    }
                    i++;
                }
            }
        }
    }

}
