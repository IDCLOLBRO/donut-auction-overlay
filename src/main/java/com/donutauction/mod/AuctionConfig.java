package com.donutauction.mod;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class AuctionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("donutauction.json");

    public String regex = "(?i)(?<name>[A-Za-z0-9_]{1,16})\\s+(?:has\\s+)?paid you\\s*\\$?\\s*(?<amount>[0-9][0-9,.]*[kKmMbB]?)";
    public boolean soundEnabled = true;

    // -1 means centered horizontally.
    public int hudX = -1;
    public int hudY = 12;

    public static AuctionConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                AuctionConfig config = GSON.fromJson(reader, AuctionConfig.class);
                if (config != null && config.regex != null) return config;
            } catch (IOException | JsonSyntaxException ignored) {}
        }

        AuctionConfig config = new AuctionConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {}
    }
}