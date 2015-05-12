package com.makersfactory.mfylog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid="MFYLog", name="MFY Log", version="0.0.3", acceptableRemoteVersions="*")
public class MFYLog {

	@Instance(value = "1")
	public static MFYLog instance;
	//@SidedProxy(clientSide="mypackage.client.CommonProxy", serverSide="mypackage.CommonProxy")
	//public static CommonProxy proxy;
	
	static String logFileName;
	static OutputStreamWriter logWriter;
	Date date;
	
	public static final String tagInfo = "IFO";
	public static final String tagMove = "MOV";
	public static final String tagInteract = "INT";
	public static final String tagLogin = "LGI";
	public static final String tagLogout = "LGO";
	public static final String tagChat = "CHT";
	public static final String tagCommand = "CMD";
	public static final String tagBreak = "BRK";
	public static final String tagDamage = "DMG";
	public static final String tagEntityDeath = "DTH";
	public static final String tagConnect = "CON";
	
	public static ImmutableSet<Item> bannedItems;
	public static ImmutableSet<Block> bannedBlocks;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		// RANDOM CONFIG STUFF
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		config.save();
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		//proxy.registerRenderers();
		
		// INITIALIZE LOG WRITER
		initLog();

		// INITIALIZE EVENT LISTENERS
		MinecraftForge.EVENT_BUS.register(new MFYLogForgeEvents());
		FMLCommonHandler.instance().bus().register(new MFYLogFMLEvents());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public void initLog() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String month = String.format("%02d", cal.get(Calendar.MONTH)+1);
		String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
		String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
		String minute = String.format("%02d", cal.get(Calendar.MINUTE));
		logFileName = "MFY_"+year+"-"+month+"-"+day+"_"+hour+minute+".txt";
		boolean success = new File("MFY_logs").mkdir();
		
		try {
			logWriter = new OutputStreamWriter(new FileOutputStream("MFY_logs/"+logFileName), Charset.forName("UTF-8").newEncoder()) ;
		} catch (Exception e) {	
			int a = 0;
		}
		log(tagInfo, "Log file loaded.");
		compressExistingLogs();
	}
	
	public static void writeLineToLog(String message) {
		try {
			logWriter.write(message+"\r\n");
			logWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		try {
			for (byte b : message.getBytes()) {
				logWriter.write(b);
			}
			String linebreak = ;
			for (byte b : linebreak.getBytes()) {
				logWriter.write(b);
			}
		} catch (Exception e) {	
			
		}
		*/
	}
	
	public static String getFormattedTime() {
		Calendar cal = Calendar.getInstance();
		String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
		String minute = String.format("%02d", cal.get(Calendar.MINUTE));
		String second = String.format("%02d", cal.get(Calendar.SECOND));
		String result = hour+minute+second;
		return result;
	}
	
	public static String getFormattedDate() {
		Calendar cal = Calendar.getInstance();
		String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
		String month = String.format("%02d", cal.get(Calendar.MONTH)+1);
		String year = String.format("%04d", cal.get(Calendar.YEAR));
		String result = year+month+day;
		return result;
	}
	
	public static void log(String tag, String message) {
		String result = getFormattedDate()+" "+getFormattedTime()+" "+tag.toUpperCase();
		writeLineToLog(result+" "+message);
	}
	
	static final int BUFFER = 2048;
	
	public static void compressExistingLogs() {
		boolean success = new File("MFY_logs").mkdir();
		File dir = new File("MFY_logs");
		ArrayList<String> toDelete = new ArrayList<String>();
		for (String logName : dir.list()) {
			if (isLogFile(logName) && !logName.startsWith(logFileName)) {
				try {
					String logNameMod = "MFY_logs/"+logName;
					String zipName = logNameMod.replace("txt", "zip");
					if (!new File(zipName).exists()) {
						ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipName)));
						BufferedInputStream orig = new BufferedInputStream(new FileInputStream(new File(logNameMod)), 2048);
						out.putNextEntry(new ZipEntry(logName));
						byte data[] = new byte[BUFFER];
						int count;
						while ((count = orig.read(data, 0, BUFFER)) != -1) out.write(data,0,count);
						orig.close();
						out.close();
					}
					toDelete.add(logNameMod);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		for (String s : toDelete) {
			try {
				Files.delete(new File(s).toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isLogFile(String name) {
		return name.startsWith("MFY_") &&
				name.endsWith(".txt") &&
				name.length() == 23;
	}
}
