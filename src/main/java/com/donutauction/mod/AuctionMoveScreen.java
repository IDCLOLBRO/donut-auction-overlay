package com.donutauction.mod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AuctionMoveScreen extends Screen {
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public AuctionMoveScreen() {
        super(Text.literal("Move Auction Overlay"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x66000000);
        AuctionHud.renderEditorPreview(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = AuctionHud.getX();
            int y = AuctionHud.getY();
            if (mouseX >= x && mouseX <= x + AuctionHud.WIDTH
                    && mouseY >= y && mouseY <= y + AuctionHud.HEIGHT) {
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            DonutAuctionClient.CONFIG.hudX = Math.max(0,
                    Math.min(width - AuctionHud.WIDTH, (int) mouseX - dragOffsetX));
            DonutAuctionClient.CONFIG.hudY = Math.max(0,
                    Math.min(height - AuctionHud.HEIGHT, (int) mouseY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            DonutAuctionClient.CONFIG.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        DonutAuctionClient.CONFIG.save();
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}