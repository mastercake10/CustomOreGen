package de.Linus122.customoregen;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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

public class Main extends JavaPlugin {
	public static List<GeneratorConfig> generatorConfigs = new ArrayList<GeneratorConfig>();
	public static List<String> disabledWorlds = new ArrayList<String>();
	
	public static World activeInWorld;
	
	public static ConsoleCommandSender clogger;
	
	public void onEnable() {
		clogger = getServer().getConsoleSender();
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new Events(), this);
		Bukkit.getPluginCommand("customoregen").setExecutor(new Cmd(this));
		try {
			loadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		disabledWorlds = getConfig().getStringList("disabled-worlds");
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			activeInWorld = com.wasteofplastic.askyblock.ASkyBlock.getIslandWorld();
			clogger.sendMessage("§6[CustomOreGen] §aUsing ASkyBlock as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			activeInWorld = com.wasteofplastic.acidisland.ASkyBlock.getIslandWorld();
			clogger.sendMessage("§6[CustomOreGen] §aUsing AcidIsland as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			us.talabrek.ultimateskyblock.api.uSkyBlockAPI api = (us.talabrek.ultimateskyblock.api.uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("uSkyBlock");
			api.getConfig().getString("options.general.worldName");
			activeInWorld = Bukkit.getWorld(api.getConfig().getString("options.general.worldName"));
			
			clogger.sendMessage("§6[CustomOreGen] §aUsing uSkyBlock as SkyBlock-Plugin");
		}
		new Metrics(this);
	}
	
	public void onDisable() {
		
	}
	
	public static int getLevel(Player p) {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			return com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().getIslandLevel(p.getUniqueId());
		}
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			return com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance().getIslandLevel(p.getUniqueId());
		}
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			return (int) Math.floor(us.talabrek.ultimateskyblock.uSkyBlock.getAPI().getIslandLevel(p));
		}
		return 0;
	}
	
	static HashMap<UUID, Player> map = new HashMap<UUID, Player>();
	
	public static Player getOwner(Location loc) {
		Set<Location> set = new HashSet<Location>();
		set.add(loc);
		
		UUID uuid = null;
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			uuid = com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance()
					.getOwner(com.wasteofplastic.askyblock.ASkyBlockAPI.getInstance().locationIsOnIsland(set, loc));
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			uuid = com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance()
					.getOwner(com.wasteofplastic.acidisland.ASkyBlockAPI.getInstance().locationIsOnIsland(set, loc));
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			String player = us.talabrek.ultimateskyblock.uSkyBlock.getInstance().getIslandInfo(loc).getLeader();
			if ((Bukkit.getPlayer(player) != null) && (Bukkit.getPlayer(player).getUniqueId() != null)) {
				uuid = Bukkit.getPlayer(player).getUniqueId();
			}
		}
		
		Player p = Bukkit.getPlayer(uuid);
		
		if (p != null) {
			map.put(uuid, p);
			if (p.isOnline()) {
				activeInWorld = p.getWorld();
			}
		} else if (map.containsKey(uuid)) {
			p = (Player) map.get(uuid);
		}
		
		return p;
	}
	
	public void reload() throws IOException {
		reloadConfig();
		loadConfig();
	}
	
	public void loadConfig() throws IOException {
		File cfg = new File("plugins/CustomOreGen/config.yml");
		File dir = new File("plugins/CustomOreGen/");
		if (!dir.exists()) dir.mkdirs();
		if (!cfg.exists()) {
			FileOutputStream writer = new FileOutputStream(new File(getDataFolder() + "/config.yml"));
			InputStream out = this.getClassLoader().getResourceAsStream("config.yml");
			byte[] linebuffer = new byte[4096];
			int lineLength = 0;
			while ((lineLength = out.read(linebuffer)) > 0) {
				writer.write(linebuffer, 0, lineLength);
			}
			writer.close();
		}
		
		
		generatorConfigs = new ArrayList<GeneratorConfig>();
		int i = 0;
		while (true) {
			i++;
			if (this.getConfig().contains("generators.generator" + i)) {
				GeneratorConfig gc = new GeneratorConfig();
				gc.permission = this.getConfig().getString("generators.generator" + i + ".permission");
				gc.unlock_islandLevel = this.getConfig().getInt("generators.generator" + i + ".unlock_islandLevel");
				for (String raw : this.getConfig().getStringList("generators.generator" + i + ".blocks")) {
					try {
						if (!raw.contains("!")) {
							String material = raw.split(":")[0];
							double percent = Double.parseDouble(raw.split(":")[1]);
							gc.itemList.add(new GeneratorItem(material, (byte) 0, percent));
						} else {
							String material = raw.split("!")[0];
							double percent = Double.parseDouble(raw.split(":")[1]);
							int damage = Integer.parseInt(raw.split("!")[1].split(":")[0]);
							gc.itemList.add(new GeneratorItem(material, (byte) damage, percent));
						}
					} catch (Exception e) {
					}
				}
				generatorConfigs.add(gc);
			} else {
				break;
			}
			
		}
		//this.saveConfig();
		clogger.sendMessage("§6[CustomOreGen] §aLoaded §c" + generatorConfigs.size() + " §agenerators");
	}
}
