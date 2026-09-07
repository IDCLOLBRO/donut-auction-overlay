package com.donutauction.mod;

import net.minecraft.item.ItemStack;

public class AuctionState {
    public boolean active = false;
    public ItemStack item = ItemStack.EMPTY;
    public double highestBid = 0.0;
    public String highestBidder = null;
    public int flashTicksRemaining = 0;

    public void start(ItemStack heldItem) {
        active = true; item = heldItem.copy(); highestBid = 0.0;
        highestBidder = null; flashTicksRemaining = 0;
    }
    public void stop() { active = false; }
    public boolean registerBid(String bidderName, double amount) {
        if (amount > highestBid) {
            highestBid = amount; highestBidder = bidderName; flashTicksRemaining = 30; return true;
        }
        return false;
    }
    public void tick() { if (flashTicksRemaining > 0) flashTicksRemaining--; }
}