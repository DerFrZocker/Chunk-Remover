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

import com.mojang.serialization.Codec;
import de.derfrzocker.chunkremover.api.ChunkPosition;
import de.derfrzocker.chunkremover.api.ChunkRemoverService;
import de.derfrzocker.chunkremover.api.Dimension;
import de.derfrzocker.chunkremover.api.WorldData;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ChunkOverrider extends ChunkGenerator {

    private final static Method a;

    static {
        try {
            a = ChunkGenerator.class.getDeclaredMethod("a");
            a.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unexpected Error while get Method");
        }
    }

    @NotNull
    private final ChunkGenerator parent;
    @NotNull
    private final org.bukkit.World world;
    @NotNull
    private final Dimension dimension;
    @NotNull
    private final Supplier<ChunkRemoverService> serviceSupplier;

    public ChunkOverrider(@NotNull ChunkGenerator parent, @NotNull org.bukkit.World world, @NotNull Dimension dimension, @NotNull Supplier<ChunkRemoverService> serviceSupplier) throws IllegalAccessException {
        super(null, null);
        Validate.notNull(parent, "Parent ChunkGenerator cannot be null");
        Validate.notNull(world, "World cannot be null");
        Validate.notNull(dimension, "Dimension cannot be null");
        Validate.notNull(serviceSupplier, "Service supplier cannot be null");

        this.parent = parent;
        this.world = world;
        this.dimension = dimension;
        this.serviceSupplier = serviceSupplier;
    }

    @Override
    public void buildBase(RegionLimitedWorldAccess regionLimitedWorldAccess, IChunkAccess iChunkAccess) {
        if (serviceSupplier.get().shouldGenerate(world, new ChunkPosition(iChunkAccess.getPos().x, iChunkAccess.getPos().z))) {
            parent.buildBase(regionLimitedWorldAccess, iChunkAccess);
        }
    }

    @Override
    public void buildNoise(GeneratorAccess generatorAccess, StructureManager structureManager, IChunkAccess iChunkAccess) {
        if (serviceSupplier.get().shouldGenerate(world, new ChunkPosition(iChunkAccess.getPos().x, iChunkAccess.getPos().z))) {
            parent.buildNoise(generatorAccess, structureManager, iChunkAccess);
        }
    }

    @Override
    public void doCarving(long i, BiomeManager biomemanager, IChunkAccess iChunkAccess, WorldGenStage.Features worldgenstage_features) {
        if (serviceSupplier.get().shouldGenerate(world, new ChunkPosition(iChunkAccess.getPos().x, iChunkAccess.getPos().z))) {
            parent.doCarving(i, biomemanager, iChunkAccess, worldgenstage_features);
        }
    }

    @Override
    public void addDecorations(RegionLimitedWorldAccess regionLimitedWorldAccess, StructureManager structuremanager) {
        ChunkRemoverService service = serviceSupplier.get();
        ChunkPosition chunkPosition = new ChunkPosition(regionLimitedWorldAccess.a(), regionLimitedWorldAccess.b());

        if (service.shouldGenerate(world, chunkPosition)) {
            parent.addDecorations(regionLimitedWorldAccess, structuremanager);
            return;
        }

        // chunk should not get generated
        WorldData worldData = service.getWorldData(world.getName());

        if (worldData == null) {
            // no world data -> nothing we can do
            return;
        }

        final int chunkX = chunkPosition.getX();
        final int chunkZ = chunkPosition.getZ();
        final int x = chunkX << 4;
        final int z = chunkZ << 4;

        if (worldData.shouldGeneratePortalRoom()) {
            // generating Stronghold room

            SeededRandom random = new SeededRandom();
            structuremanager.a(SectionPosition.a(new ChunkCoordIntPair(regionLimitedWorldAccess.a(), regionLimitedWorldAccess.b()), 0), StructureGenerator.STRONGHOLD).forEach((structureStart) -> {
                int i = 0;
                for (StructurePiece structurePiece : structureStart.d()) {
                    i++;
                    if (structurePiece instanceof WorldGenStrongholdPieces.WorldGenStrongholdPortalRoom) {
                        // TODO maybe look for the real random
                        random.a(world.getSeed() + i, chunkX, chunkZ);
                        structurePiece.a(regionLimitedWorldAccess, structuremanager, this, random, new StructureBoundingBox(x, z, x + 15, z + 15), new ChunkCoordIntPair(chunkX, chunkZ), null);
                    }
                }
            });
        }

        // the end exit portal will always spawn at P(0|y|0) which is chunk P(0|0)
        if (worldData.shouldFixExitPortal() && dimension == Dimension.THE_END && chunkX == 0 && chunkZ == 0) {
            /**
             We check the corner value of P(0|y|0) which are in a new chunk
             This means we are checking the Positions
             - A(-1|y|0)
             - B(0|y|-1)
             - C(-1|y|-1)

             When one of those returns a value as highest block which is > 1
             (1 because the highest block returns highestBlock + 1) we use this value and
             set a block at P(0|y|0). This will result into the exit portal generating at this height
             and also blends in with the terrain which is nearby

             If A, B and C all return <= 1, we use the value from {@link WorldData#getFallbackExitPortalHeight()} was
             */
            int y;

            if ((y = regionLimitedWorldAccess.a(HeightMap.Type.MOTION_BLOCKING, -1, 0)) <= 1 &&
                    (y = regionLimitedWorldAccess.a(HeightMap.Type.MOTION_BLOCKING, 0, -1)) <= 1 &&
                    (y = regionLimitedWorldAccess.a(HeightMap.Type.MOTION_BLOCKING, -1, -1)) <= 1) {
                y = worldData.getFallbackExitPortalHeight();
            }

            regionLimitedWorldAccess.setTypeAndData(new BlockPosition(0, y, 0), Blocks.END_STONE.getBlockData(), 3);
        }

        if (worldData.shouldGenerateEndSpike()) {
            BlockPosition blockposition = new BlockPosition(x, 0, z);
            BiomeBase biomebase = regionLimitedWorldAccess.getBiome((chunkX << 2) + 2, 2, (chunkZ << 2) + 2);
            SeededRandom seededrandom = new SeededRandom();
            long chunkSeeded = seededrandom.a(regionLimitedWorldAccess.getSeed(), x, z);


            List<List<Supplier<WorldGenFeatureConfigured<?, ?>>>> features = biomebase.e().c();
            int decorationLength = WorldGenStage.Decoration.values().length;

            for (int i = 0; i < decorationLength; ++i) {
                int counter = 0;

                if (features.size() > i) {
                    for (Iterator<Supplier<WorldGenFeatureConfigured<?, ?>>> iterator = features.get(i).iterator(); iterator.hasNext(); ++counter) {
                        Supplier<WorldGenFeatureConfigured<?, ?>> supplier = iterator.next();
                        WorldGenFeatureConfigured<?, ?> configuration = supplier.get();

                        IRegistryWritable<WorldGenFeatureConfigured<?, ?>> registry = getRegistry().b(IRegistry.au);
                        WorldGenFeatureConfigured<?, ?> configured = RegistryGeneration.e.get(registry.getKey(configuration));

                        if (configured == BiomeDecoratorGroups.END_SPIKE) {
                            seededrandom.b(chunkSeeded, counter, i);
                            try {
                                configuration.a(regionLimitedWorldAccess, this, seededrandom, blockposition);
                            } catch (Exception e) {
                                CrashReport crashReport = CrashReport.a(e, "Feature placement");
                                crashReport.a("Feature").a("Id", IRegistry.FEATURE.getKey(configuration.e)).a("Config", configuration.f).a("Description", configuration.e::toString);
                                throw new ReportedException(crashReport);
                            }
                        }
                    }
                }
            }

        }

    }

    @NotNull
    private IRegistryCustom getRegistry() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();

        return server.getCustomRegistry();
    }

    @Override
    protected Codec<? extends ChunkGenerator> a() {
        try {
            return (Codec<? extends ChunkGenerator>) a.invoke(parent);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected Error while invoke method getCarvingBiome", e);
        }
    }

    @Override
    public int getBaseHeight(int i, int i1, HeightMap.Type type) {
        return parent.getBaseHeight(i, i1, type);
    }

    @Override
    public IBlockAccess a(int i, int i1) {
        return parent.a(i, i1);
    }

    @Nullable
    @Override
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?>
            structuregenerator, BlockPosition blockposition, int i, boolean flag) {
        return parent.findNearestMapFeature(worldserver, structuregenerator, blockposition, i, flag);
    }

    @Override
    public boolean a(ChunkCoordIntPair chunkcoordintpair) {
        return parent.a(chunkcoordintpair);
    }

    @Override
    public int b(int i, int j, HeightMap.Type heightmap_type) {
        return parent.b(i, j, heightmap_type);
    }

    @Override
    public int c(int i, int j, HeightMap.Type heightmap_type) {
        return parent.c(i, j, heightmap_type);
    }

    @Override
    public int getGenerationDepth() {
        return parent.getGenerationDepth();
    }

    @Override
    public int getSeaLevel() {
        return parent.getSeaLevel();
    }

    @Override
    public int getSpawnHeight() {
        return parent.getSpawnHeight();
    }

    @Override
    public List<BiomeSettingsMobs.c> getMobsFor(BiomeBase biomebase, StructureManager
            structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        return parent.getMobsFor(biomebase, structuremanager, enumcreaturetype, blockposition);
    }

    @Override
    public StructureSettings getSettings() {
        return parent.getSettings();
    }

    @Override
    public void addMobs(RegionLimitedWorldAccess regionlimitedworldaccess) {
        parent.addMobs(regionlimitedworldaccess);
    }

    @Override
    public WorldChunkManager getWorldChunkManager() {
        return parent.getWorldChunkManager();
    }

    @Override
    public void createBiomes(IRegistry<BiomeBase> iregistry, IChunkAccess iChunkAccess) {
        parent.createBiomes(iregistry, iChunkAccess);
    }

    @Override
    public void createStructures(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess
            iChunkAccess, DefinedStructureManager definedstructuremanager, long i) {
        parent.createStructures(iregistrycustom, structuremanager, iChunkAccess, definedstructuremanager, i);
    }

    @Override
    public void storeStructures(GeneratorAccessSeed generatoraccessseed, StructureManager
            structuremanager, IChunkAccess iChunkAccess) {
        parent.storeStructures(generatoraccessseed, structuremanager, iChunkAccess);
    }

}