package com.donutauction.mod;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AuctionSettingsScreen extends Screen {
    // Settings GUI for auction duration and overlay visibility.
    public AuctionSettingsScreen() {
        super(Text.literal("Auction Settings"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = height / 2 - 70;

        addDrawableChild(ButtonWidget.builder(Text.literal("- 1 min"), button -> changeTime(-60))
                .dimensions(centerX - 155, y + 35, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("- 10 sec"), button -> changeTime(-10))
                .dimensions(centerX - 75, y + 35, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+ 10 sec"), button -> changeTime(10))
                .dimensions(centerX + 5, y + 35, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+ 1 min"), button -> changeTime(60))
                .dimensions(centerX + 85, y + 35, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(overlayButtonText(), button -> {
                    DonutAuctionClient.CONFIG.overlayVisible = !DonutAuctionClient.CONFIG.overlayVisible;
                    DonutAuctionClient.CONFIG.save();
                    button.setMessage(overlayButtonText());
                })
                .dimensions(centerX - 100, y + 65, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
                .dimensions(centerX - 100, y + 95, 200, 20).build());
    }

    private void changeTime(int amount) {
        DonutAuctionClient.CONFIG.auctionTimeSeconds =
                Math.max(10, Math.min(24 * 60 * 60,
                        DonutAuctionClient.CONFIG.auctionTimeSeconds + amount));
        DonutAuctionClient.CONFIG.save();
    }

    private Text overlayButtonText() {
        return Text.literal("Overlay: "
                + (DonutAuctionClient.CONFIG.overlayVisible ? "SHOWN" : "HIDDEN"));
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        String title = "AUCTION SETTINGS";
        String time = "Auction time: "
                + AuctionHud.formatTime(DonutAuctionClient.CONFIG.auctionTimeSeconds);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 62, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, time, width / 2, height / 2 - 35, 0xFF5555);
        context.drawCenteredTextWithShadow(textRenderer,
                "Use the buttons below to change the auction duration",
                width / 2, height / 2 - 15, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}