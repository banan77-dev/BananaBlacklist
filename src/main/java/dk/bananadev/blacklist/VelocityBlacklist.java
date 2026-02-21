package dk.ditnavn.blacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Plugin(
        id = "velocityblacklist",
        name = "Velocity Blacklist",
        version = "1.0",
        authors = {"DitNavn"}
)
public class VelocityBlacklist {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public ConfigData configData;

    @Inject
    public VelocityBlacklist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();
        
        // Registrer kommandoen
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("blacklist", new BlacklistCommand(this));
        
        logger.info("Velocity Blacklist er startet!");
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        String playerName = event.getPlayer().getUsername().toLowerCase();
        
        if (configData.blacklistedPlayers.contains(playerName)) {
            // Kick spilleren med den custom besked fra config
            event.setResult(ResultedEvent.ComponentResult.denied(
                    MiniMessage.miniMessage().deserialize(configData.kickMessage)
            ));
        }
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            File configFile = new File(dataDirectory.toFile(), "config.json");
            
            if (!configFile.exists()) {
                configData = new ConfigData();
                saveConfig();
            } else {
                try (Reader reader = new FileReader(configFile)) {
                    configData = gson.fromJson(reader, ConfigData.class);
                }
            }
        } catch (IOException e) {
            logger.error("Kunne ikke loade config!", e);
        }
    }

    public void saveConfig() {
        File configFile = new File(dataDirectory.toFile(), "config.json");
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(configData, writer);
        } catch (IOException e) {
            logger.error("Kunne ikke gemme config!", e);
        }
    }

    // Klasse til at holde vores data
    public static class ConfigData {
        public String kickMessage = "<red><bold>BANNED!</bold></red><br><gray>Du er blevet blacklisted fra dette netværk.</gray><br><yellow>Kontakt support hvis du mener dette er en fejl.</yellow>";
        public Set<String> blacklistedPlayers = new HashSet<>();
    }
}

