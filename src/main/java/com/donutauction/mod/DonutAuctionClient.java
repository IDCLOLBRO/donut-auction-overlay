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
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

public class DonutAuctionClient implements ClientModInitializer {
    public static final AuctionState STATE = new AuctionState();
    public static AuctionConfig CONFIG;
    private static KeyBinding toggleKey;
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.register(Identifier.of("donutauction", "auction"));

    @Override
    public void onInitializeClient() {
        CONFIG = AuctionConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.donutauction.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            STATE.tick();
            while (toggleKey.wasPressed()) handleToggle(client);
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> AuctionHud.render(context, STATE));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && STATE.active) handleIncomingMessage(message.getString());
        });
    }

    private static void handleToggle(MinecraftClient client) {
        if (client.player == null) return;

        if (STATE.active) {
            String message = STATE.highestBidder == null
                    ? "Auction ended with no bids."
                    : "Auction ended! Highest bid: $" + fmt(STATE.highestBid)
                    + " by " + STATE.highestBidder;
            STATE.stop();
            client.player.sendMessage(
                    Text.literal("[Auction] " + message).formatted(Formatting.GOLD), false);
            return;
        }

        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) {
            client.player.sendMessage(
                    Text.literal("[Auction] Hold an item first, then press R.")
                            .formatted(Formatting.RED), false);
            return;
        }

        STATE.start(held);
        client.player.sendMessage(
                Text.literal("[Auction] Started for " + held.getName().getString()
                        + "! Waiting for bids...").formatted(Formatting.GOLD), false);
    }

    public static void handleIncomingMessage(String text) {
        PaymentParser.Result result = PaymentParser.tryParse(text, CONFIG.regex);
        if (result == null || !STATE.registerBid(result.name, result.amount)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Auction] New highest bid: $" + fmt(result.amount)
                            + " by " + result.name).formatted(Formatting.GREEN), false);
        }
    }

    private static String fmt(double amount) {
        return amount == Math.floor(amount)
                ? String.format(Locale.US, "%,.0f", amount)
                : String.format(Locale.US, "%,.2f", amount);
    }
}