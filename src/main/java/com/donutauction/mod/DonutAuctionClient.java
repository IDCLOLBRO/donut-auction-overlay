package com.donutauction.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
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
    private static KeyBinding moveKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.MISC;

    private static int pendingTimeLimitSeconds = 0;

    @Override
    public void onInitializeClient() {
        CONFIG = AuctionConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));

        moveKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.move", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            STATE.tick();

            if (STATE.isExpired()) {
                endAuction(client, "Time is up!");
            }

            while (toggleKey.wasPressed()) handleToggle(client);
            while (moveKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new AuctionMoveScreen());
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> AuctionHud.render(context, STATE));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && STATE.active) handleIncomingMessage(message.getString());
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.toLowerCase(Locale.ROOT).startsWith("/auctiontime")) {
                handleTimeCommand(message);
                return false;
            }
            return true;
        });
    }

    private static void handleTimeCommand(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        String[] parts = message.trim().split("\\s+", 2);

        if (parts.length < 2) {
            sendClientMessage(client, "Usage: /auctiontime 5m  |  /auctiontime 90s  |  /auctiontime 2:30", Formatting.RED);
            return;
        }

        int seconds = parseTime(parts[1]);
        if (seconds <= 0) {
            sendClientMessage(client, "Invalid time. Use examples: 5m, 90s, or 2:30", Formatting.RED);
            return;
        }

        pendingTimeLimitSeconds = seconds;
        sendClientMessage(client, "Auction time limit set to " + AuctionHud.formatTime(seconds)
                + ". Now hold your item and press R.", Formatting.GREEN);
    }

    private static int parseTime(String input) {
        String value = input.trim().toLowerCase(Locale.ROOT);

        try {
            if (value.matches("\\d+:[0-5]\\d")) {
                String[] split = value.split(":");
                return Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1]);
            }
            if (value.endsWith("m")) return Integer.parseInt(value.substring(0, value.length() - 1)) * 60;
            if (value.endsWith("s")) return Integer.parseInt(value.substring(0, value.length() - 1));
            if (value.matches("\\d+")) return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}

        return 0;
    }

    private static void handleToggle(MinecraftClient client) {
        if (client.player == null) return;

        if (STATE.active) {
            endAuction(client, "Auction ended!");
            return;
        }

        if (pendingTimeLimitSeconds <= 0) {
            sendClientMessage(client,
                    "Set a time first: /auctiontime 5m (or 90s / 2:30)",
                    Formatting.RED);
            return;
        }

        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) {
            sendClientMessage(client, "Hold an item first, then press R.", Formatting.RED);
            return;
        }

        STATE.start(held, pendingTimeLimitSeconds);
        sendClientMessage(client,
                "Started " + held.getName().getString() + " for "
                        + AuctionHud.formatTime(pendingTimeLimitSeconds) + "!",
                Formatting.GOLD);
        pendingTimeLimitSeconds = 0;
    }

    private static void endAuction(MinecraftClient client, String reason) {
        if (!STATE.active) return;

        String message = STATE.highestBidder == null
                ? reason + " No bids."
                : reason + " Highest bid: $" + fmt(STATE.highestBid)
                + " by " + STATE.highestBidder;

        STATE.stop();
        sendClientMessage(client, message, Formatting.GOLD);
    }

    public static void handleIncomingMessage(String text) {
        PaymentParser.Result result = PaymentParser.tryParse(text, CONFIG.regex);
        if (result == null || !STATE.registerBid(result.name, result.amount)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        sendClientMessage(client,
                "New highest bid: $" + fmt(result.amount) + " by " + result.name,
                Formatting.GREEN);
    }

    private static void sendClientMessage(MinecraftClient client, String message, Formatting formatting) {
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Auction] " + message).formatted(formatting), false);
        }
    }

    private static String fmt(double amount) {
        return amount == Math.floor(amount)
                ? String.format(Locale.US, "%,.0f", amount)
                : String.format(Locale.US, "%,.2f", amount);
    }
}