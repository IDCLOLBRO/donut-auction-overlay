package com.donutauction.mod;

import net.minecraft.client.*; import net.minecraft.client.font.TextRenderer; import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack; import net.minecraft.text.Text; import net.minecraft.util.Formatting;
import java.util.Locale;

public class AuctionHud {
    public static void render(DrawContext context, AuctionState state) {
        if (!state.active) return;
        MinecraftClient client=MinecraftClient.getInstance(); if(client.player==null)return;
        TextRenderer tr=client.textRenderer; int sw=client.getWindow().getScaledWidth();
        int w=220,h=76,x=(sw-w)/2,y=12;
        int border=state.flashTicksRemaining>0?0xFFFFD54A:0xFF555555;
        context.fill(x,y,x+w,y+h,0xB0101010);
        context.fill(x,y,x+w,y+1,border); context.fill(x,y+h-1,x+w,y+h,border);
        context.fill(x,y,x+1,y+h,border); context.fill(x+w-1,y,x+w,y+h,border);
        Text title=Text.literal("AUCTION").formatted(Formatting.GOLD,Formatting.BOLD);
        context.drawText(tr,title,x+(w-tr.getWidth(title))/2,y+5,0xFFFFFF,true);
        ItemStack stack=state.item; int ix=x+8,iy=y+27;
        if(!stack.isEmpty()){ String name=stack.getName().getString(); while(tr.getWidth(name)>w-16&&name.length()>3)name=name.substring(0,name.length()-4)+"...";
            context.drawText(tr,Text.literal(name).formatted(Formatting.WHITE,Formatting.BOLD),x+(w-tr.getWidth(name))/2,y+17,0xFFFFFF,true); context.drawItem(stack,ix,iy); }
        Text bid=Text.literal("Highest: $"+format(state.highestBid)).formatted(state.highestBidder==null?Formatting.GRAY:Formatting.GREEN,Formatting.BOLD);
        context.drawText(tr,bid,ix+22,iy,0xFFFFFF,true);
        Text bidder=Text.literal(state.highestBidder==null?"Bidder: No bids yet":"Bidder: "+state.highestBidder).formatted(Formatting.YELLOW);
        context.drawText(tr,bidder,ix+22,iy+12,0xFFFFFF,true);
    }
    private static String format(double a){return a==Math.floor(a)?String.format(Locale.US,"%,.0f",a):String.format(Locale.US,"%,.2f",a);}
}