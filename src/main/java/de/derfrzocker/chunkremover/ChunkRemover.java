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

package de.derfrzocker.chunkremover;

import de.derfrzocker.chunkremover.api.ChunkRemoverService;
import de.derfrzocker.chunkremover.impl.ChunkRemoverServiceImpl;
import de.derfrzocker.chunkremover.impl.WorldDataYamlImpl;
import de.derfrzocker.chunkremover.impl.v1_16_R3.WorldHandler_v1_16_R3;
import de.derfrzocker.chunkremover.impl.validators.RandomValidator;
import de.derfrzocker.spigot.utils.Config;
import de.derfrzocker.spigot.utils.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.function.Supplier;

public class ChunkRemover extends JavaPlugin {

    private Supplier<ChunkRemoverService> serviceSupplier;

    @Override
    public void onEnable() {
        Version version = Version.getServerVersion(getServer());

        // if no suitable version was found, log, disable plugin and return
        if (version != Version.v1_16_R3) {
            getLogger().warning("The Server version which you are running is unsupported, you are running version '" + version + "'");
            getLogger().warning("The plugin supports following versions " + Version.v1_16_R3);
            getLogger().warning("(Spigot / Paper version 1.16.4), if you are running such a Minecraft version, than your bukkit implementation is unsupported, in this case please contact the developer, so he can resolve this Issue");

            if (version == Version.UNKNOWN) {
                getLogger().warning("The Version '" + version + "' can indicate, that you are using a newer Minecraft version than currently supported.");
                getLogger().warning("In this case please update to the newest version of this plugin. If this is the newest Version, than please be patient. It can take some weeks until the plugin is updated");
            }

            new IllegalStateException("Server version is unsupported!").printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        ChunkRemoverService service = new ChunkRemoverServiceImpl();
        serviceSupplier = () -> service;

        reload(service);
        registerValidators(service);

        getServer().getPluginManager().registerEvents(new WorldHandler_v1_16_R3(this, serviceSupplier), this);

        new Metrics(this, 9882);
    }

    private void registerValidators(ChunkRemoverService chunkRemoverService) {
        chunkRemoverService.registerChunkValidator("random", new RandomValidator());
        chunkRemoverService.registerChunkValidator("always-true", (worldInfo, chunkPosition) -> true);
    }

    private void reload(ChunkRemoverService chunkRemoverService) {
        // we don't use #getConfig(), since it loads the default config of the jar as default values
        Config config = new Config(new File(getDataFolder(), "config.yml"));

        ConfigurationSection worldDatas = config.getConfigurationSection("world-datas");
        Set<String> currentWorldNames = chunkRemoverService.getWorldDataNames();

        if (worldDatas != null) {
            worldDatas.getKeys(false).forEach(worldName -> {
                currentWorldNames.remove(worldName);
                chunkRemoverService.setWorldData(worldName, new WorldDataYamlImpl(worldDatas.getConfigurationSection(worldName)));
            });
        }

        currentWorldNames.forEach(chunkRemoverService::removeWorldData);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        reload(serviceSupplier.get());
        sender.sendMessage("Reload complete");

        return true;
    }

}
