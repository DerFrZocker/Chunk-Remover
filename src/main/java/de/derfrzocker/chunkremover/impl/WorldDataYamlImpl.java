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

import de.derfrzocker.chunkremover.api.ValidatorData;
import de.derfrzocker.chunkremover.api.WorldData;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class WorldDataYamlImpl implements WorldData {

    private final boolean generatePortalRoom;
    private final boolean fixExitPortal;
    private final int fallbackExitPortalHeight;
    private final String chunkValidatorName;
    private final ValidatorDataYamlImpl validatorData;

    public WorldDataYamlImpl(@NotNull ConfigurationSection section) {
        generatePortalRoom = section.getBoolean("generate-portal-room", false);
        fixExitPortal = section.getBoolean("fix-exit-portal", true);
        fallbackExitPortalHeight = section.getInt("fallback-exit-portal-height", 64);
        chunkValidatorName = section.getString("chunk-validator.name", "always-true");
        validatorData = new ValidatorDataYamlImpl(section.getConfigurationSection("chunk-validator.data"));
    }

    @Override
    public boolean shouldGeneratePortalRoom() {
        return generatePortalRoom;
    }

    @Override
    public boolean shouldFixExitPortal() {
        return fixExitPortal;
    }

    @Override
    public int getFallbackExitPortalHeight() {
        return fallbackExitPortalHeight;
    }

    @NotNull
    @Override
    public String getChunkValidatorName() {
        return chunkValidatorName;
    }

    @NotNull
    @Override
    public ValidatorData getValidatorData() {
        return validatorData;
    }

}
