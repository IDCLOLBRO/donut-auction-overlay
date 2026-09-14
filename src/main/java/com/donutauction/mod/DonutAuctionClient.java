package com.donutauction.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

public class DonutAuctionClient implements ClientModInitializer {
    public static final AuctionState STATE = new AuctionState();
    public static AuctionConfig CONFIG;

    private static KeyBinding toggleKey;
    private static KeyBinding settingsKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.MISC;

    @Override
    public void onInitializeClient() {
        CONFIG = AuctionConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));

        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            STATE.tick();

            if (STATE.isExpired()) endAuction(client, "Time is up!");

            while (toggleKey.wasPressed()) handleToggle(client);

            while (settingsKey.wasPressed()) {
                if (client.currentScreen instanceof AuctionSettingsScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new AuctionSettingsScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (CONFIG.overlayVisible) AuctionHud.render(context, STATE);
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && STATE.active) handleIncomingMessage(message.getString());
        });
    }

    private static void handleToggle(MinecraftClient client) {
        if (client.player == null) return;

        if (STATE.active) {
            endAuction(client, "Auction ended!");
            return;
        }

        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) {
            sendClientMessage(client, "Hold an item first, then press R.", Formatting.RED);
            return;
        }

        int time = CONFIG.auctionTimeSeconds;
        STATE.start(held, time);
        sendClientMessage(client,
                "Started " + held.getName().getString() + " for "
                        + AuctionHud.formatTime(time) + "!",
                Formatting.GOLD);
    }

    private static void endAuction(MinecraftClient client, String reason) {
        if (!STATE.active) return;

        String message = STATE.highestBidder == null
                ? reason + " No bids."
                : reason + " Highest bid: $" + fmt(STATE.highestBid) + " by " + STATE.highestBidder;

        STATE.stop();
        sendClientMessage(client, message, Formatting.GOLD);
    }

    public static void handleIncomingMessage(String text) {
        PaymentParser.Result result = PaymentParser.tryParse(text, CONFIG.regex);
        if (result == null || !STATE.registerBid(result.name, result.amount)) return;

        sendClientMessage(MinecraftClient.getInstance(),
                "New highest bid: $" + fmt(result.amount) + " by " + result.name,
                Formatting.GREEN);
    }

    private static void sendClientMessage(MinecraftClient client, String message, Formatting formatting) {
        if (client.player != null)
            client.player.sendMessage(Text.literal("[Auction] " + message).formatted(formatting), false);
    }

    private static String fmt(double amount) {
        return amount == Math.floor(amount)
                ? String.format(Locale.US, "%,.0f", amount)
                : String.format(Locale.US, "%,.2f", amount);
    }
}