package com.donutauction.mod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.Locale;

public class AuctionHud {
    public static final int WIDTH = 240;
    public static final int HEIGHT = 92;

    public static void render(DrawContext context, AuctionState state) {
        if (!state.active) return;
        renderBox(context, state, false);
    }

    public static void renderEditorPreview(DrawContext context) {
        renderBox(context, DonutAuctionClient.STATE, true);
    }

    public static int getX() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return 0;
        return DonutAuctionClient.CONFIG.hudX < 0
                ? (client.getWindow().getScaledWidth() - WIDTH) / 2
                : DonutAuctionClient.CONFIG.hudX;
    }

    public static int getY() {
        return DonutAuctionClient.CONFIG.hudY;
    }

    private static void renderBox(DrawContext context, AuctionState state, boolean preview) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null && !preview) return;

        TextRenderer tr = client.textRenderer;
        int x = getX();
        int y = getY();

        // Red/white border alternates every half second.
        boolean redPhase = (System.currentTimeMillis() / 500L) % 2L == 0L;
        int border = redPhase ? 0xFFFF2020 : 0xFFFFFFFF;

        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xC0101010);
        context.fill(x, y, x + WIDTH, y + 2, border);
        context.fill(x, y + HEIGHT - 2, x + WIDTH, y + HEIGHT, border);
        context.fill(x, y, x + 2, y + HEIGHT, border);
        context.fill(x + WIDTH - 2, y, x + WIDTH, y + HEIGHT, border);

        Text title = Text.literal("AUCTION").formatted(Formatting.BOLD);
        context.drawText(tr, title, x + (WIDTH - tr.getWidth(title)) / 2, y + 6, 0xFFFFFFFF, true);

        String timerText;
        if (state.active) {
            timerText = "TIME LEFT: " + formatTime(state.getRemainingSeconds());
        } else {
            timerText = "TIME LIMIT: Set with /auctiontime";
        }
        context.drawText(tr, Text.literal(timerText).formatted(Formatting.RED, Formatting.BOLD),
                x + (WIDTH - tr.getWidth(timerText)) / 2, y + 20, 0xFFFFFFFF, true);

        ItemStack stack = state.item;
        String itemName = (!stack.isEmpty() ? stack.getName().getString() : "Hold an item to auction");
        while (tr.getWidth(itemName) > WIDTH - 18 && itemName.length() > 4) {
            itemName = itemName.substring(0, itemName.length() - 4) + "...";
        }
        context.drawText(tr, Text.literal(itemName).formatted(Formatting.WHITE, Formatting.BOLD),
                x + (WIDTH - tr.getWidth(itemName)) / 2, y + 34, 0xFFFFFFFF, true);

        int lineY = y + 54;
        if (!stack.isEmpty()) context.drawItem(stack, x + 8, lineY - 2);

        Text bid = Text.literal("Highest: $" + format(state.highestBid))
                .formatted(state.highestBidder == null ? Formatting.GRAY : Formatting.GREEN, Formatting.BOLD);
        context.drawText(tr, bid, x + 30, lineY, 0xFFFFFFFF, true);

        Text bidder = Text.literal(state.highestBidder == null
                        ? "Bidder: No bids yet"
                        : "Bidder: " + state.highestBidder)
                .formatted(Formatting.YELLOW);
        context.drawText(tr, bidder, x + 30, lineY + 15, 0xFFFFFFFF, true);

        if (preview) {
            Text hint = Text.literal("DRAG THIS BOX - ESC TO SAVE").formatted(Formatting.AQUA);
            context.drawText(tr, hint, x + (WIDTH - tr.getWidth(hint)) / 2, y + HEIGHT + 4, 0xFFFFFFFF, true);
        }
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private static String format(double amount) {
        return amount == Math.floor(amount)
                ? String.format(Locale.US, "%,.0f", amount)
                : String.format(Locale.US, "%,.2f", amount);
    }
}