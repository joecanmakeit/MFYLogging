package mypackage;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

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

public class FMLEvents {
	
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
		if (!e.player.worldObj.isRemote) {
			EntityPlayer p = e.player;
			String pName = p.getCommandSenderName();
			Coords c = new Coords((int)Math.floor(p.posX),(int)Math.floor(p.posY),(int)Math.floor(p.posZ));
			if (!playerCoordRegistry.containsKey(pName)) {
				playerCoordRegistry.put(pName, c);
			}
			else {
				Coords old = playerCoordRegistry.get(pName);
				if (c.x != old.x || c.y != old.y || c.z != old.z) {
					MFYLog.log(MFYLog.tagMove, pName+" "+c.x+" "+c.y+" "+c.z);
					p.worldObj.setBlock(0, 0, 0, Blocks.dirt);
					playerCoordRegistry.put(pName, c);
				}
			}
		}
	}
	
}
