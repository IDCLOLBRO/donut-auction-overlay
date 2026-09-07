package com.donutauction.mod;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*; import java.nio.charset.StandardCharsets; import java.nio.file.*;

public class AuctionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("donutauction.json");
    public String regex = "(?i)(?<name>[A-Za-z0-9_]{1,16})\\s+(?:has\\s+)?paid you\\s*\\$?\\s*(?<amount>[0-9][0-9,.]*[kKmMbB]?)";
    public boolean soundEnabled = true;
    public static AuctionConfig load() {
        if (Files.exists(PATH)) try (Reader r=Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            AuctionConfig c=GSON.fromJson(r,AuctionConfig.class); if(c!=null&&c.regex!=null)return c;
        } catch(IOException|JsonSyntaxException ignored){}
        AuctionConfig c=new AuctionConfig(); c.save(); return c;
    }
    public void save() {
        try { Files.createDirectories(PATH.getParent()); try(Writer w=Files.newBufferedWriter(PATH,StandardCharsets.UTF_8)){GSON.toJson(this,w);} }
        catch(IOException ignored){}
    }
}