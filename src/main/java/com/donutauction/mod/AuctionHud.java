package com.donutauction.mod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.Locale;

public class AuctionHud {
    public static final int MIN_WIDTH = 160;
    public static final int MIN_HEIGHT = 70;
    public static void render(DrawContext context, AuctionState state) { if (state.active) renderBox(context, state); }
    public static int getX() { MinecraftClient c=MinecraftClient.getInstance(); return DonutAuctionClient.CONFIG.hudX<0?(c.getWindow().getScaledWidth()-getWidth())/2:DonutAuctionClient.CONFIG.hudX; }
    public static int getY(){return DonutAuctionClient.CONFIG.hudY;}
    public static int getWidth(){return DonutAuctionClient.CONFIG.hudWidth;}
    public static int getHeight(){return DonutAuctionClient.CONFIG.hudHeight;}
    private static void renderBox(DrawContext context,AuctionState state){
        MinecraftClient client=MinecraftClient.getInstance(); if(client.player==null)return;
        TextRenderer tr=client.textRenderer; int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        float scale=Math.max(.55f,Math.min((float)w/240f,(float)h/92f)); int bw=Math.max(2,(int)(2*scale));
        int border=(System.currentTimeMillis()/500L)%2L==0L?0xFFFF2020:0xFFFFFFFF;
        context.fill(x,y,x+w,y+h,0xC0101010); context.fill(x,y,x+w,y+bw,border); context.fill(x,y+h-bw,x+w,y+h,border); context.fill(x,y,x+bw,y+h,border); context.fill(x+w-bw,y,x+w,y+h,border);
        context.getMatrices().pushMatrix(); context.getMatrices().translate(x,y); context.getMatrices().scale(scale,scale); int baseW=Math.max(1,(int)(w/scale));
        Text title=Text.literal("AUCTION").formatted(Formatting.BOLD); context.drawText(tr,title,(baseW-tr.getWidth(title))/2,6,0xFFFFFFFF,true);
        String timer="TIME LEFT: "+formatTime(state.getRemainingSeconds()); context.drawText(tr,Text.literal(timer).formatted(Formatting.RED,Formatting.BOLD),(baseW-tr.getWidth(timer))/2,20,0xFFFFFFFF,true);
        ItemStack stack=state.item; String itemName=!stack.isEmpty()?stack.getName().getString():"Hold an item to auction";
        while(tr.getWidth(itemName)>baseW-18&&itemName.length()>4)itemName=itemName.substring(0,itemName.length()-4)+"...";
        context.drawText(tr,Text.literal(itemName).formatted(Formatting.WHITE,Formatting.BOLD),(baseW-tr.getWidth(itemName))/2,34,0xFFFFFFFF,true);
        int lineY=54;
        if(!stack.isEmpty()){context.getMatrices().pushMatrix();context.getMatrices().translate(8,lineY-5);context.getMatrices().scale(1.25f,1.25f);context.drawItem(stack,0,0);context.getMatrices().popMatrix();}
        Text bid=Text.literal("Highest: $"+format(state.highestBid)).formatted(state.highestBidder==null?Formatting.GRAY:Formatting.GREEN,Formatting.BOLD); context.drawText(tr,bid,34,lineY,0xFFFFFFFF,true);
        Text bidder=Text.literal(state.highestBidder==null?"Bidder: No bids yet":"Bidder: "+state.highestBidder).formatted(Formatting.YELLOW); context.drawText(tr,bidder,34,lineY+15,0xFFFFFFFF,true);
        context.getMatrices().popMatrix();
    }
    public static String formatTime(int totalSeconds){return String.format(Locale.US,"%02d:%02d",totalSeconds/60,totalSeconds%60);}
    private static String format(double amount){return amount==Math.floor(amount)?String.format(Locale.US,"%,.0f",amount):String.format(Locale.US,"%,.2f",amount);}
}