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

import com.google.common.collect.Maps;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.ProtoChunk;

import java.lang.reflect.Field;

public class ChunkAccessReflectionUtil {

    public static final Field PROTO_HEIGHT_MAPS;
    public static final Field CHUNK_HEIGHT_MAPS;

    static {
        try {
            PROTO_HEIGHT_MAPS = ProtoChunk.class.getDeclaredField("f");
            PROTO_HEIGHT_MAPS.setAccessible(true);
            CHUNK_HEIGHT_MAPS = Chunk.class.getDeclaredField("heightMap");
            CHUNK_HEIGHT_MAPS.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copy(ProtoChunk target, ProtoChunk source) {
        for (Field field : ProtoChunk.class.getDeclaredFields()) {
            String name = field.getName();
            if (name.equals("LOGGER") || name.equals("b") || name.equals("f")) {
                continue;
            }

            field.setAccessible(true);
            try {
                field.set(target, field.get(source));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void removeHeightMaps(ProtoChunk protoChunk) {
        try {
            PROTO_HEIGHT_MAPS.set(protoChunk, Maps.newEnumMap(HeightMap.Type.class));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copy(Chunk target, Chunk source) {
        for (Field field : Chunk.class.getDeclaredFields()) {
            String name = field.getName();
            if (name.equals("LOGGER") || name.equals("heightMap") || name.equals("DATA_TYPE_REGISTRY") || name.equals("loc") || name.equals("a")) {
                continue;
            }

            field.setAccessible(true);
            try {
                field.set(target, field.get(source));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void removeHeightMaps(Chunk chunk) {
        try {
            CHUNK_HEIGHT_MAPS.set(chunk, Maps.newEnumMap(HeightMap.Type.class));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
