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

package de.derfrzocker.chunkremover.utils;

import de.derfrzocker.chunkremover.api.ChunkPosition;
import de.derfrzocker.chunkremover.api.WorldInfo;

import java.util.Random;

public class ValidatorUtil {

    private final static long SALT = -1976948025L;

    public static Random getNameSeededRandom(WorldInfo worldInfo, ChunkPosition chunkPosition) {
        Random random = new Random(worldInfo.getSeed() + worldInfo.getName().hashCode() + SALT);

        seedChunkPosition(random, worldInfo.getSeed(), chunkPosition);

        return random;
    }

    public static Random getSeededRandom(WorldInfo worldInfo, ChunkPosition chunkPosition) {
        Random random = new Random(worldInfo.getSeed() + SALT);

        seedChunkPosition(random, worldInfo.getSeed(), chunkPosition);

        return random;
    }

    private static void seedChunkPosition(Random random, long seed, ChunkPosition chunkPosition) {
        long long1 = random.nextLong();
        long long2 = random.nextLong();
        long newSeed = (long) chunkPosition.getX() * long1 ^ (long) chunkPosition.getZ() * long2 ^ seed;
        random.setSeed(newSeed);
    }

}
