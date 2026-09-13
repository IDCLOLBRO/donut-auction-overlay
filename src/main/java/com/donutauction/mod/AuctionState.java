package com.donutauction.mod;

import net.minecraft.item.ItemStack;

public class AuctionState {
    public boolean active = false;
    public ItemStack item = ItemStack.EMPTY;
    public double highestBid = 0.0;
    public String highestBidder = null;
    public int flashTicksRemaining = 0;

    private long endTimeMillis = 0L;
    private int timeLimitSeconds = 0;

    public void start(ItemStack heldItem, int seconds) {
        active = true;
        item = heldItem.copy();
        highestBid = 0.0;
        highestBidder = null;
        flashTicksRemaining = 0;
        timeLimitSeconds = seconds;
        endTimeMillis = System.currentTimeMillis() + seconds * 1000L;
    }

    public void stop() {
        active = false;
        endTimeMillis = 0L;
    }

    public boolean isExpired() {
        return active && getRemainingSeconds() <= 0;
    }

    public int getRemainingSeconds() {
        if (!active || endTimeMillis <= 0L) return 0;
        return Math.max(0, (int) Math.ceil((endTimeMillis - System.currentTimeMillis()) / 1000.0));
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public boolean registerBid(String bidderName, double amount) {
        if (amount > highestBid) {
            highestBid = amount;
            highestBidder = bidderName;
            flashTicksRemaining = 30;
            return true;
        }
        return false;
    }

    public void tick() {
        if (flashTicksRemaining > 0) flashTicksRemaining--;
    }
}