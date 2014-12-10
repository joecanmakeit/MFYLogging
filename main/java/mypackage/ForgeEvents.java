package mypackage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class ForgeEvents {
	
	@SubscribeEvent
	public void onPlayerChat(ServerChatEvent e) {
		if (!e.player.worldObj.isRemote) {
			String location = (int)Math.floor(e.player.posX)+" "+(int)Math.floor(e.player.posY)+" "+(int)Math.floor(e.player.posZ);
			MFYLog.log(MFYLog.tagChat, e.username+" "+location+" "+e.message);
		}
	}
	
	@SubscribeEvent
	public void onPlayerCommand(CommandEvent e) {
		if (!e.sender.getEntityWorld().isRemote) {
			String result = e.command.getCommandName();
			for (String p : e.parameters) {
				result += " "+p;
			}
			MFYLog.log(MFYLog.tagCommand, e.sender.getCommandSenderName()+" "+result);
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.entityPlayer.worldObj.isRemote && e.action != e.action.RIGHT_CLICK_AIR) {
			String name = e.entityPlayer.getCommandSenderName();
			String clickType = "";
			if (e.action == e.action.LEFT_CLICK_BLOCK) {
				clickType += "LEFT";
			}
			else if (e.action == e.action.RIGHT_CLICK_BLOCK) {
				clickType += "RIGHT";
			}
			String target = e.x+" "+e.y+" "+e.z;
			String targetType = e.entityPlayer.worldObj.getBlock(e.x, e.y, e.z).getUnlocalizedName().toUpperCase().substring(5);
			String holding = "";
			if (e.entityPlayer.getCurrentEquippedItem() == null) {
				holding += e.entityPlayer.getCurrentEquippedItem();
			}
			else {
				holding += e.entityPlayer.getCurrentEquippedItem().getItem().getUnlocalizedName().toUpperCase().substring(5);
			}
			String result = name+" "+target+" "+targetType+" "+clickType+" "+holding;
			MFYLog.log(MFYLog.tagInteract, result);
		}
	}
	
	@SubscribeEvent
	public void onBreak(BreakEvent e) {
		if (!e.getPlayer().worldObj.isRemote && e.getResult() != Result.DENY) {
			String name = e.getPlayer().getCommandSenderName();
			String target = e.x+" "+e.y+" "+e.z;
			String targetType = e.getPlayer().worldObj.getBlock(e.x, e.y, e.z).getUnlocalizedName().toUpperCase().substring(5);
			String result = name+" "+target+" "+targetType;
			MFYLog.log(MFYLog.tagBreak, result);
		}
	}
	
	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent e) {
		if (	!e.entityLiving.worldObj.isRemote && 
				!e.entityLiving.isDead && 
				!e.entityLiving.isEntityInvulnerable()) {
			String target;
			if (e.entityLiving instanceof EntityPlayer) {
				target = ((EntityPlayer)e.entityLiving).getCommandSenderName();
			}
			else target = e.entityLiving.getClass().getSimpleName();
			String source;
			if (e.source.getEntity() != null) {
				if (e.source.getEntity() instanceof EntityPlayer) {
					source = ((EntityPlayer)e.source.getEntity()).getCommandSenderName();
				}
				else source = e.source.getEntity().getClass().getSimpleName();
			}
			else source = "NAN";
			String dmg = ""+e.ammount;
			String type = e.source.getDamageType();
			String location = (int)Math.floor(e.entityLiving.posX)+" "+(int)Math.floor(e.entityLiving.posY)+" "+(int)Math.floor(e.entityLiving.posZ);
			String result = target+" "+location+" "+dmg+" "+type+" "+source;
			MFYLog.log(MFYLog.tagDamage, result);
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent e) {
		if (!e.entityLiving.worldObj.isRemote) {
			if (e.entityLiving instanceof EntityPlayer) {
				String player = ((EntityPlayer)e.entityLiving).getCommandSenderName();
				String type;
				if (e.source.getEntity() != null) {
					type = e.source.getEntity().getClass().getSimpleName();
				}
				else type = e.source.getDamageType();
				String loc = (int)Math.floor(e.entityLiving.posX)+" "+(int)Math.floor(e.entityLiving.posY)+" "+(int)Math.floor(e.entityLiving.posZ);
				String result = player+" "+loc+" "+type;
				MFYLog.log(MFYLog.tagPlayerDeath, result);
			}
		}
	}
	
}
