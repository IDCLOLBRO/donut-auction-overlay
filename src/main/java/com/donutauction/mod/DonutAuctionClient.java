package com.donutauction.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.*; import net.minecraft.client.option.KeyBinding; import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil; import net.minecraft.item.ItemStack; import net.minecraft.sound.SoundEvents; import net.minecraft.text.Text; import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW; import java.util.Locale;

public class DonutAuctionClient implements ClientModInitializer {
 public static final AuctionState STATE=new AuctionState(); public static AuctionConfig CONFIG; private static KeyBinding toggleKey;
 public void onInitializeClient(){
  CONFIG=AuctionConfig.load();
  toggleKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.donutauction.toggle",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_R,"category.donutauction"));
  ClientTickEvents.END_CLIENT_TICK.register(c->{STATE.tick();while(toggleKey.wasPressed())handleToggle(c);});
  HudRenderCallback.EVENT.register((ctx,t)->AuctionHud.render(ctx,STATE));
  ClientReceiveMessageEvents.GAME.register((message,overlay)->{if(!overlay&&STATE.active)handleIncomingMessage(message.getString());});
 }
 private static void handleToggle(MinecraftClient c){
  if(c.player==null)return;
  if(STATE.active){String s=STATE.highestBidder==null?"Auction ended with no bids.":"Auction ended! Highest bid: $"+fmt(STATE.highestBid)+" by "+STATE.highestBidder;STATE.stop();c.player.sendMessage(Text.literal("[Auction] "+s).formatted(Formatting.GOLD),false);return;}
  ItemStack held=c.player.getMainHandStack();if(held.isEmpty()){c.player.sendMessage(Text.literal("[Auction] Hold an item first, then press R.").formatted(Formatting.RED),false);return;}
  STATE.start(held);c.player.sendMessage(Text.literal("[Auction] Started for "+held.getName().getString()+"! Waiting for bids...").formatted(Formatting.GOLD),false);
 }
 public static void handleIncomingMessage(String text){
  PaymentParser.Result r=PaymentParser.tryParse(text,CONFIG.regex);if(r==null||!STATE.registerBid(r.name,r.amount))return;
  MinecraftClient c=MinecraftClient.getInstance();if(c.player!=null)c.player.sendMessage(Text.literal("[Auction] New highest bid: $"+fmt(r.amount)+" by "+r.name).formatted(Formatting.GREEN),false);
  if(CONFIG.soundEnabled)c.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,1f));
 }
 private static String fmt(double a){return a==Math.floor(a)?String.format(Locale.US,"%,.0f",a):String.format(Locale.US,"%,.2f",a);}
}