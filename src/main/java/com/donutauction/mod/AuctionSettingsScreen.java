package com.donutauction.mod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AuctionSettingsScreen extends Screen {
    private boolean editMode = false;
    private ButtonWidget editButton;

    public AuctionSettingsScreen() { super(Text.literal("Auction Settings")); }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 90;

        addDrawableChild(ButtonWidget.builder(Text.literal("- 1 min"), b -> changeTime(-60)).dimensions(cx - 155, y + 105, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("- 10 sec"), b -> changeTime(-10)).dimensions(cx - 75, y + 80, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+ 10 sec"), b -> changeTime(10)).dimensions(cx + 5, y + 80, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+ 1 min"), b -> changeTime(60)).dimensions(cx + 85, y + 80, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(overlayButtonText(), b -> {
            DonutAuctionClient.CONFIG.overlayVisible = !DonutAuctionClient.CONFIG.overlayVisible;
            DonutAuctionClient.CONFIG.save();
            b.setMessage(overlayButtonText());
        }).dimensions(cx - 100, y + 135, 200, 20).build());

        editButton = addDrawableChild(ButtonWidget.builder(editButtonText(), b -> {
            editMode = !editMode;
            b.setMessage(editButtonText());
        }).dimensions(cx - 100, y + 162, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(cx - 100, y + 189, 200, 20).build());
    }

    private void changeTime(int amount) {
        DonutAuctionClient.CONFIG.auctionTimeSeconds = Math.max(10, Math.min(86400, DonutAuctionClient.CONFIG.auctionTimeSeconds + amount));
        DonutAuctionClient.CONFIG.save();
    }

    private Text overlayButtonText() {
        return Text.literal("Overlay: " + (DonutAuctionClient.CONFIG.overlayVisible ? "SHOWN" : "HIDDEN"));
    }

    private Text editButtonText() {
        return Text.literal(editMode ? "EDIT OVERLAY: ON" : "MOVE / RESIZE OVERLAY");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xA0000000);
        int panelW = 360, panelH = 270, x = (width-panelW)/2, y = (height-panelH)/2-15;
        context.fill(x,y,x+panelW,y+panelH,0xEE151515);
        context.fill(x,y,x+panelW,y+2,0xFFFF2020);
        context.fill(x,y+panelH-2,x+panelW,y+panelH,0xFFFFFFFF);
        context.fill(x,y,x+2,y+panelH,0xFFFF2020);
        context.fill(x+panelW-2,y,x+panelW,y+panelH,0xFFFFFFFF);

        context.drawCenteredTextWithShadow(textRenderer,"AUCTION SETTINGS",width/2,y+15,0xFFFFFF);
        String time = AuctionHud.formatTime(DonutAuctionClient.CONFIG.auctionTimeSeconds);
        context.drawCenteredTextWithShadow(textRenderer,"NEXT AUCTION TIME: " + time,width/2,y+42,0xFF5555);
        context.drawCenteredTextWithShadow(textRenderer,"This is how long your next auction will run",width/2,y+62,0xAAAAAA);

        if (editMode) {
            handleEditKeys();
            context.drawCenteredTextWithShadow(textRenderer,"EDIT MODE: Arrow keys move | +/- resize | M resets position",width/2,y+240,0x55FFFF);
        }
        super.render(context,mouseX,mouseY,delta);
    }

    private void handleEditKeys() {
        if (client == null) return;
        long now = System.currentTimeMillis();
        if (now - lastEdit < 80) return;
        long window = client.getWindow().getHandle();
        boolean changed=false;
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT)) { DonutAuctionClient.CONFIG.hudX=Math.max(0,AuctionHud.getX()-4); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT)) { DonutAuctionClient.CONFIG.hudX=Math.min(width-AuctionHud.getWidth(),AuctionHud.getX()+4); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_UP)) { DonutAuctionClient.CONFIG.hudY=Math.max(0,AuctionHud.getY()-4); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN)) { DonutAuctionClient.CONFIG.hudY=Math.min(height-AuctionHud.getHeight(),AuctionHud.getY()+4); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL)) { DonutAuctionClient.CONFIG.hudWidth=Math.min(500,AuctionHud.getWidth()+4); DonutAuctionClient.CONFIG.hudHeight=Math.min(250,AuctionHud.getHeight()+2); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS)) { DonutAuctionClient.CONFIG.hudWidth=Math.max(AuctionHud.MIN_WIDTH,AuctionHud.getWidth()-4); DonutAuctionClient.CONFIG.hudHeight=Math.max(AuctionHud.MIN_HEIGHT,AuctionHud.getHeight()-2); changed=true; }
        if (net.minecraft.client.util.InputUtil.isKeyPressed(client.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_M)) { DonutAuctionClient.CONFIG.hudX=-1; DonutAuctionClient.CONFIG.hudY=12; changed=true; }
        if (changed) { DonutAuctionClient.CONFIG.save(); lastEdit=now; }
    }
    private long lastEdit=0;

    @Override public boolean shouldPause() { return false; }
}