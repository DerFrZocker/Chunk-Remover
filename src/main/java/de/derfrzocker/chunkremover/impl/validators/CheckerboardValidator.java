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

package de.derfrzocker.chunkremover.impl.validators;

import de.derfrzocker.chunkremover.api.ChunkPosition;
import de.derfrzocker.chunkremover.api.ChunkValidator;
import de.derfrzocker.chunkremover.api.ValidatorData;
import de.derfrzocker.chunkremover.api.WorldInfo;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

// config values:
//   'inverted': [true, false] removed chunks become solid and solid removed
//   'tile-length.x/z': integer the length of one tile
public class CheckerboardValidator implements ChunkValidator {

    @Override
    public boolean shouldGenerate(@NotNull WorldInfo worldInfo, @NotNull ChunkPosition chunkPosition) {
        ValidatorData validatorData = worldInfo.getWorldData().getValidatorData();
        int tileLengthX = NumberConversions.toInt(validatorData.get("tile-length.x", 2));
        int tileLengthZ = NumberConversions.toInt(validatorData.get("tile-length.z", 2));
        boolean inverted = validatorData.getBoolean("inverted", false);

        int x = chunkPosition.getX();
        int z = chunkPosition.getZ();

        if (x < 0) {
            x++;
            inverted = !inverted;
        }

        if (z < 0) {
            z++;
            inverted = !inverted;
        }

        int tileX = x / tileLengthX;
        int tileZ = z / tileLengthZ;
        boolean resultX = (tileX & 1) == 0;
        boolean resultZ = (tileZ & 1) == 0;

        if ((resultX && resultZ) || (!resultX && !resultZ)) {
            return !inverted;
        }

        return inverted;
    }

}
