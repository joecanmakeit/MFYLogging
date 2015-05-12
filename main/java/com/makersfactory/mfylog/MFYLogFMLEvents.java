package com.makersfactory.mfylog;

import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

public class MFYLogFMLEvents {
	
	public class Coords {
		int x = 0;
		int y = 0;
		int z = 0;
		Coords(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	HashMap<String, Coords> playerCoordRegistry = new HashMap<String, Coords>();
	
	@SubscribeEvent
	public void onPlayerConnect(ServerConnectionFromClientEvent e) {
		String ip = e.manager.getSocketAddress().toString();
		MFYLog.log(MFYLog.tagConnect, ip);
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent e) {
		if (!e.player.worldObj.isRemote) {
			EntityPlayer p = e.player;
			String location = (int)Math.floor(e.player.posX)+" "+(int)Math.floor(e.player.posY)+" "+(int)Math.floor(e.player.posZ);
			MFYLog.log(MFYLog.tagLogin,e.player.getCommandSenderName()+" "+location);
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent e) {
		if (!e.player.worldObj.isRemote) {
			EntityPlayer p = e.player;
			String location = (int)Math.floor(e.player.posX)+" "+(int)Math.floor(e.player.posY)+" "+(int)Math.floor(e.player.posZ);
			MFYLog.log(MFYLog.tagLogout,e.player.getCommandSenderName()+" "+location);
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		EntityPlayer player = e.player;
		if (!e.player.worldObj.isRemote && e.phase == e.phase.END) {
			String pName = player.getCommandSenderName();
			Coords c = new Coords((int)Math.floor(player.posX),(int)Math.floor(player.posY),(int)Math.floor(player.posZ));
			if (!playerCoordRegistry.containsKey(pName)) {
				playerCoordRegistry.put(pName, c);
			}
			else {
				Coords old = playerCoordRegistry.get(pName);
				if (c.x != old.x || c.y != old.y || c.z != old.z) {
					MFYLog.log(MFYLog.tagMove, pName+" "+c.x+" "+c.y+" "+c.z);
					playerCoordRegistry.put(pName, c);
				}
			}
		}
	}
	
}
