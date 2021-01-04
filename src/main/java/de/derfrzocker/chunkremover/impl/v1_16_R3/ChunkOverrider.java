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
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

public class ChunkOverrider extends ChunkGenerator {

    private final static Method a;
    private final static Field b;
    private final static Field d;

    static {
        try {
            a = ChunkGenerator.class.getDeclaredMethod("a");
            a.setAccessible(true);
            b = ChunkGenerator.class.getDeclaredField("b");
            b.setAccessible(true);
            d = WorldGenCarverWrapper.class.getDeclaredField("d");
            d.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException("Unexpected Error while get Method");
        }
    }

    @NotNull
    private final ChunkGenerator parent;
    @NotNull
    private final org.bukkit.World world;
    @NotNull
    private final Supplier<ChunkRemoverService> serviceSupplier;

    public ChunkOverrider(@NotNull ChunkGenerator parent, @NotNull org.bukkit.World world, @NotNull Supplier<ChunkRemoverService> serviceSupplier) throws IllegalAccessException {
        super(null, null);
        Validate.notNull(parent, "Parent ChunkGenerator cannot be null");
        Validate.notNull(world, "World cannot be null");
        Validate.notNull(serviceSupplier, "Service supplier cannot be null");

        this.parent = parent;
        this.world = world;
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
        if (serviceSupplier.get().shouldGenerate(world, new ChunkPosition(regionLimitedWorldAccess.a(), regionLimitedWorldAccess.b()))) {
            parent.addDecorations(regionLimitedWorldAccess, structuremanager);
        }
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
    public BlockPosition findNearestMapFeature(WorldServer worldserver, StructureGenerator<?> structuregenerator, BlockPosition blockposition, int i, boolean flag) {
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
    public List<BiomeSettingsMobs.c> getMobsFor(BiomeBase biomebase, StructureManager structuremanager, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
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
    public void createBiomes(IRegistry<BiomeBase> iregistry, IChunkAccess ichunkaccess) {
        parent.createBiomes(iregistry, ichunkaccess);
    }

    @Override
    public void createStructures(IRegistryCustom iregistrycustom, StructureManager structuremanager, IChunkAccess ichunkaccess, DefinedStructureManager definedstructuremanager, long i) {
        parent.createStructures(iregistrycustom, structuremanager, ichunkaccess, definedstructuremanager, i);
    }

    @Override
    public void storeStructures(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, IChunkAccess ichunkaccess) {
        parent.storeStructures(generatoraccessseed, structuremanager, ichunkaccess);
    }

}