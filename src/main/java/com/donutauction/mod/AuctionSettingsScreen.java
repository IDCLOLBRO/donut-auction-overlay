package com.donutauction.mod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AuctionSettingsScreen extends Screen {
    private ButtonWidget minusMinute;
    private ButtonWidget minusTen;
    private ButtonWidget plusTen;
    private ButtonWidget plusMinute;
    private ButtonWidget overlayButton;

    public AuctionSettingsScreen() {
        super(Text.literal("Auction Settings"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 55;

        minusMinute = addDrawableChild(ButtonWidget.builder(Text.literal("- 1 min"), b -> changeTime(-60))
                .dimensions(cx - 155, y + 55, 75, 20).build());
        minusTen = addDrawableChild(ButtonWidget.builder(Text.literal("- 10 sec"), b -> changeTime(-10))
                .dimensions(cx - 75, y + 55, 75, 20).build());
        plusTen = addDrawableChild(ButtonWidget.builder(Text.literal("+ 10 sec"), b -> changeTime(10))
                .dimensions(cx + 5, y + 55, 75, 20).build());
        plusMinute = addDrawableChild(ButtonWidget.builder(Text.literal("+ 1 min"), b -> changeTime(60))
                .dimensions(cx + 85, y + 55, 75, 20).build());

        overlayButton = addDrawableChild(ButtonWidget.builder(overlayButtonText(), b -> {
                    DonutAuctionClient.CONFIG.overlayVisible = !DonutAuctionClient.CONFIG.overlayVisible;
                    DonutAuctionClient.CONFIG.save();
                    overlayButton.setMessage(overlayButtonText());
                })
                .dimensions(cx - 100, y + 85, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 100, y + 112, 200, 20).build());
    }

    private void changeTime(int amount) {
        DonutAuctionClient.CONFIG.auctionTimeSeconds =
                Math.max(10, Math.min(86400,
                        DonutAuctionClient.CONFIG.auctionTimeSeconds + amount));
        DonutAuctionClient.CONFIG.save();
    }

    private Text overlayButtonText() {
        return Text.literal("Overlay: "
                + (DonutAuctionClient.CONFIG.overlayVisible ? "SHOWN" : "HIDDEN"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Explicitly draw the entire settings panel so it is visible on 1.21.11.
        context.fill(0, 0, width, height, 0xA0000000);

        int panelW = 360;
        int panelH = 190;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2 - 15;

        context.fill(x, y, x + panelW, y + panelH, 0xEE151515);
        context.fill(x, y, x + panelW, y + 2, 0xFFFF2020);
        context.fill(x, y + panelH - 2, x + panelW, y + panelH, 0xFFFFFFFF);
        context.fill(x, y, x + 2, y + panelH, 0xFFFF2020);
        context.fill(x + panelW - 2, y, x + panelW, y + panelH, 0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,
                "AUCTION SETTINGS", width / 2, y + 15, 0xFFFFFF);

        String time = "Auction Time: "
                + AuctionHud.formatTime(DonutAuctionClient.CONFIG.auctionTimeSeconds);
        context.drawCenteredTextWithShadow(textRenderer,
                time, width / 2, y + 42, 0xFF5555);

        context.drawCenteredTextWithShadow(textRenderer,
                "Adjust the time before starting your auction",
                width / 2, y + 67, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}