package de.Linus122.customoregen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.reflect.TypeToken;


public class Main extends JavaPlugin {
	public static List<GeneratorConfig> generatorConfigs = new ArrayList<GeneratorConfig>();
	public static List<String> disabledWorlds = new ArrayList<String>();

	public static World activeInWorld;

	public static ConsoleCommandSender clogger;
	
	private static HashMap<UUID, Integer> cachedOregenConfigs = new HashMap<UUID, Integer>();
	private static JSONConfig cachedOregenJsonConfig;
	
	public void onEnable() {
		clogger = getServer().getConsoleSender();
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new Events(), this);
		Bukkit.getPluginCommand("customoregen").setExecutor(new Cmd(this));
		try{
			loadConfig();
		}catch(IOException e) {
			e.printStackTrace();
		}
		cachedOregenJsonConfig = new JSONConfig(cachedOregenConfigs, new TypeToken<HashMap<UUID, Integer>>() { }.getType(), this);
		cachedOregenConfigs = (HashMap<UUID, Integer>) cachedOregenJsonConfig.get();
		if(cachedOregenConfigs == null){
			cachedOregenConfigs = new HashMap<UUID, Integer>();
		}
		disabledWorlds = getConfig().getStringList("disabled-worlds");
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			activeInWorld = com.wasteofplastic.askyblock.ASkyBlock.getIslandWorld();
			clogger.sendMessage("§6[CustomOreGen] §aUsing ASkyBlock as SkyBlock-Plugin");
		}else if(Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			activeInWorld = com.wasteofplastic.acidisland.ASkyBlock.getIslandWorld();
			clogger.sendMessage("§6[CustomOreGen] §aUsing AcidIsland as SkyBlock-Plugin");
		}else if(Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			us.talabrek.ultimateskyblock.api.uSkyBlockAPI api = (us.talabrek.ultimateskyblock.api.uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("uSkyBlock");
			api.getConfig().getString("options.general.worldName");
			activeInWorld = Bukkit.getWorld(api.getConfig().getString("options.general.worldName"));

			clogger.sendMessage("§6[CustomOreGen] §aUsing uSkyBlock as SkyBlock-Plugin");
		}
		new Metrics(this);
	}

	public void onDisable() {
		cachedOregenJsonConfig.saveToDisk(cachedOregenConfigs);
	}

	public static int getLevel(UUID uuid) {
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			return com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().getIslandLevel(uuid);
		}
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			return com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance().getIslandLevel(uuid);
		}
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			if(Bukkit.getPlayer(uuid) != null){
				Player p = Bukkit.getPlayer(uuid);
				return (int) Math.floor(us.talabrek.ultimateskyblock.uSkyBlock.getInstance().getIslandLevel(p));
			}
			// Note: The API for getIslandInfo seems to be broken
			return (int) Math.floor(us.talabrek.ultimateskyblock.uSkyBlock.getInstance().getIslandInfo(us.talabrek.ultimateskyblock.uSkyBlock.getInstance().getPlayerInfo(uuid)).getLevel());
		}
		return 0;
	}
	
	public static OfflinePlayer getOwner(Location loc) {
		Set<Location> set = new HashSet<Location>();
		set.add(loc);

		UUID uuid = null;
		if(Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			uuid = com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance()
					.getOwner(com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().locationIsOnIsland(set, loc));
		}else if(Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			uuid = com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance()
					.getOwner(com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance().locationIsOnIsland(set, loc));
		}else if(Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			String player = us.talabrek.ultimateskyblock.uSkyBlock.getInstance().getIslandInfo(loc).getLeader();
			if((Bukkit.getPlayer(player) != null) && (Bukkit.getPlayer(player).getUniqueId() != null)) {
				uuid = Bukkit.getOfflinePlayer(player).getUniqueId();
			}
		}

		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

		return p;
	}

	public void reload() throws IOException {
		reloadConfig();
		loadConfig();
	}

	public void loadConfig() throws IOException {
		// Writing default config to data directory
		File cfg = new File("plugins/CustomOreGen/config.yml");
		File dir = new File("plugins/CustomOreGen/");
		if(!dir.exists()) dir.mkdirs();
		if(!cfg.exists()){
			FileOutputStream writer = new FileOutputStream(new File(getDataFolder() + "/config.yml"));
			InputStream out = this.getClassLoader().getResourceAsStream("config.yml");
			byte[] linebuffer = new byte[4096];
			int lineLength = 0;
			while((lineLength = out.read(linebuffer)) > 0)
			{
			   writer.write(linebuffer, 0, lineLength);
			}
			writer.close();	
		}
		 
		this.reloadConfig();
		generatorConfigs = new ArrayList<GeneratorConfig>();
		for(String key : this.getConfig().getConfigurationSection("generators").getKeys(false)){
			GeneratorConfig gc = new GeneratorConfig();
			gc.permission = this.getConfig().getString("generators." + key + ".permission");
			gc.unlock_islandLevel = this.getConfig().getInt("generators." + key + ".unlock_islandLevel");
			for(String raw : this.getConfig().getStringList("generators." + key + ".blocks")){
				try{
					if(!raw.contains("!")){
						String material = raw.split(":")[0];
						double percent = Double.parseDouble(raw.split(":")[1]);
						gc.itemList.add(new GeneratorItem(material, (byte) 0, percent));
					}else{
						String material = raw.split("!")[0];
						double percent = Double.parseDouble(raw.split(":")[1]);
						int damage = Integer.parseInt(raw.split("!")[1].split(":")[0]);
						gc.itemList.add(new GeneratorItem(material, (byte) damage, percent));
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			generatorConfigs.add(gc);

			
		}
		//this.saveConfig();
		clogger.sendMessage("§6[CustomOreGen] §aLoaded §c" + generatorConfigs.size() + " §agenerators");
	}
	public static GeneratorConfig getGeneratorConfigForPlayer(OfflinePlayer p){
		GeneratorConfig gc = null;
		int id = 0;
		if (p == null) {
			gc = Main.generatorConfigs.get(0);
			cacheOreGen(p.getUniqueId(), id);
		} else {
			
			int islandLevel = Main.getLevel(p.getUniqueId());

			if(p.isOnline()){
				Player realP = p.getPlayer();
				if (Main.activeInWorld.getName().equals(realP.getWorld().getName())) {
					for (GeneratorConfig gc2 : Main.generatorConfigs) {
						if (gc2 == null) {
							continue;
						}
						if ((realP.hasPermission(gc2.permission) || gc2.permission.length() == 0) && islandLevel >= gc2.unlock_islandLevel) {
							// Weiter
							gc = gc2;
							id++;
						}

					}
				}	
			}else{
				gc = getCachedGeneratorConfig(p.getUniqueId());
			}
		}
		if(id > 0){
			cacheOreGen(p.getUniqueId(), id - 1);
		}
		return gc;
	}
	public static GeneratorConfig getCachedGeneratorConfig(UUID uuid){
		if(cachedOregenConfigs.containsKey(uuid)){
			return Main.generatorConfigs.get(cachedOregenConfigs.get(uuid));
		}
		return null;
	}
	public static void cacheOreGen(UUID uuid, int configID){
		cachedOregenConfigs.put(uuid, configID);
	}
}
