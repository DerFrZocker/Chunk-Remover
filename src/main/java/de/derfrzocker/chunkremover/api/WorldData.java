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

import org.jetbrains.annotations.NotNull;

public interface WorldData {

    /**
     * When this method returns true, than the portal room should get
     * generated. Even if the chunk the portal room is in is a empty / removed chunk.
     * <p>
     * This only affects worlds, which can generated the portal room
     *
     * @return true if the portal room should get generated, otherwise false
     */
    boolean shouldGeneratePortalRoom();

    /**
     * This only works in a dimension which can spawn the ender dragon fight.
     * <p>
     * With this option set to true, the plugin should attempt to fix the
     * exit portal in the end. The exit portal spawns at P(0|0|0) if the chunk
     * with the position P(0|0) is empty / removed. This leads to an exit portal
     * which is not functional. In order to fix this, the height of the surrounding
     * terrain gets used. If this fails, for example because there is no surrounding
     * terrain the value from {@link #getFallbackExitPortalHeight()} is used
     *
     * @return true if the exit portal should be fixed or false if not
     */
    boolean shouldFixExitPortal();

    /**
     * This value is used if {@link #shouldFixExitPortal()} returns true
     * and no suitable position is found.
     * <p>
     * For further details see {@link #shouldFixExitPortal()}
     *
     * @return y-fallback value for the exit portal
     */
    int getFallbackExitPortalHeight();

    /**
     * When this method returns true the end spikes in the end gets generated,
     * even if the chunk is a removed chunk
     *
     * @return true if the end spike should get generated otherwise false
     */
    boolean shouldGenerateEndSpike();

    /**
     * Normally the plugin does not affect spawn chunks. With this option set to true
     * however, the plugin attempts to also affect spawn chunks.
     *
     * @return true if the plugin should affect spawn chunk otherwise false
     */
    boolean shouldAffectSpawnChunks();

    /**
     * @return the name of the chunk validator which should get used
     */
    @NotNull
    String getChunkValidatorName();

    /**
     * @return the validator data of this world data
     */
    @NotNull
    ValidatorData getValidatorData();

}
