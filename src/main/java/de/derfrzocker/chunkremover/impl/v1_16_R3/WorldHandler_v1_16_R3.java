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
import net.minecraft.server.v1_16_R3.ChunkGenerator;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class WorldHandler_v1_16_R3 implements Listener {

    @NotNull
    private final Supplier<ChunkRemoverService> serviceSupplier;

    public WorldHandler_v1_16_R3(@NotNull Supplier<ChunkRemoverService> serviceSupplier) {
        Validate.notNull(serviceSupplier, "Service supplier cannot be null");

        this.serviceSupplier = serviceSupplier;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInitEvent(WorldInitEvent event) {
        inject(event.getWorld());
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
            final ChunkOverrider overrider = new ChunkOverrider(chunkGenerator, bukkitWorld, serviceSupplier);

            // set the ChunkOverrider to the PlayerChunkMap
            ChunkGeneratorField.set(playerChunkMap, overrider);

        } catch (final Exception e) {
            throw new RuntimeException("Unexpected error while hook into world " + world.getName(), e);
        }
    }

}
