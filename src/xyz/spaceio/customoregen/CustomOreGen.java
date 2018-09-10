package xyz.spaceio.customoregen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.reflect.TypeToken;

import de.Linus122.SpaceIOMetrics.Metrics;
import xyz.spaceio.hooks.ASkyBlockHook;
import xyz.spaceio.hooks.AcidIslandHook;
import xyz.spaceio.hooks.BentoBoxHook;
import xyz.spaceio.hooks.SkyblockAPIHook;
import xyz.spaceio.hooks.uSkyBlockHook;

public class CustomOreGen extends JavaPlugin {
	
	/*
	 * Configurations for all generators (defined in the config.yml)
	 */
	private List<GeneratorConfig> generatorConfigs = new ArrayList<GeneratorConfig>();
	
	/*
	 * Disabled world blacklist
	 */
	private List<String> disabledWorlds = new ArrayList<String>();

	/*
	 * Our logger
	 */
	private ConsoleCommandSender clogger;
	
	/*
	 * Cache for GeneratorConfig ID's for each player
	 */
	private HashMap<UUID, Integer> cachedOregenConfigs = new HashMap<UUID, Integer>();
	private JSONConfig cachedOregenJsonConfig;

	/*
	 * API Hook for the corresponding SkyBlock plugin
	 */
	private SkyblockAPIHook skyblockAPI;
	
	/*
	 * Prefix for the clogger
	 */
	private final String PREFIX = "§6[CustomOreGen] "; 

	@Override
	public void onEnable() {
		clogger = getServer().getConsoleSender();
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new Events(this), this);

		this.loadHook();

		Bukkit.getPluginCommand("customoregen").setExecutor(new Cmd(this));

		try {
			loadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cachedOregenJsonConfig = new JSONConfig(cachedOregenConfigs, new TypeToken<HashMap<UUID, Integer>>() {
		}.getType(), this);
		cachedOregenConfigs = (HashMap<UUID, Integer>) cachedOregenJsonConfig.get();
		if (cachedOregenConfigs == null) {
			cachedOregenConfigs = new HashMap<UUID, Integer>();
		}
		disabledWorlds = getConfig().getStringList("disabled-worlds");

		new Metrics(this);
	}

	/**
	 * creates a new api hook instance for the used skyblock plugin
	 */
	private void loadHook() {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			skyblockAPI = new ASkyBlockHook();
			sendConsole("&aUsing ASkyBlock as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			skyblockAPI = new AcidIslandHook();
			sendConsole("&aUsing AcidIsland as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			skyblockAPI = new uSkyBlockHook();
			sendConsole("&aUsing uSkyBlock as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("BentoBox")) {
			skyblockAPI = new BentoBoxHook();
			sendConsole("&aUsing BentoBox as SkyBlock-Plugin");
		}
	}

	@Override
	public void onDisable() {
		cachedOregenJsonConfig.saveToDisk(cachedOregenConfigs);
	}

	public List<World> getActiveWorlds() {
		return Arrays.stream(skyblockAPI.getSkyBlockWorldNames()).map(v -> Bukkit.getWorld(v)).collect(Collectors.toList());
	}

	public int getLevel(UUID uuid, String world) {
		return skyblockAPI.getIslandLevel(uuid, world);
	}

	public OfflinePlayer getOwner(Location loc) {
		UUID uuid = skyblockAPI.getIslandOwner(loc);
		if (uuid == null) {
			return null;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

		return p;
	}

	public void reload() throws IOException {
		reloadConfig();
		loadConfig();
	}

	/**
	 * Just a method that sorts out stupid configuration mistakes made by kids who
	 * always give 1-star-reviews on Spigot.
	 */
	public void loadConfig() throws IOException {
		// Writing default config to data directory
		File cfg = new File("plugins/CustomOreGen/config.yml");
		File dir = new File("plugins/CustomOreGen/");
		if (!dir.exists())
			dir.mkdirs();
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

		this.reloadConfig();
		generatorConfigs = new ArrayList<GeneratorConfig>();
		for (String key : this.getConfig().getConfigurationSection("generators").getKeys(false)) {
			double totalChance = 0d;
			GeneratorConfig gc = new GeneratorConfig();
			gc.permission = this.getConfig().getString("generators." + key + ".permission");
			gc.unlock_islandLevel = this.getConfig().getInt("generators." + key + ".unlock_islandLevel");
			if (gc.permission == null) {
				sendConsole(String.format("&cConfig error: generator %s does not have a valid permission entry!", key));
			}
			if (gc.unlock_islandLevel > 0 && gc.permission.length() > 1) {
				sendConsole(String.format("&cConfig error: generator %s has both a permission and level setup! Be sure to choose one of them!", key));
			}

			for (String raw : this.getConfig().getStringList("generators." + key + ".blocks")) {
				try {
					if (!raw.contains("!")) {
						String material = raw.split(":")[0];
						if (Material.getMaterial(material.toUpperCase()) == null) {
							sendConsole(String.format("&cConfig error: generator %s has an unrecognized material: %s", key, material));
						}
						double percent = Double.parseDouble(raw.split(":")[1]);
						totalChance += percent;
						gc.itemList.add(new GeneratorItem(material, (byte) 0, percent));
					} else {
						String material = raw.split("!")[0];
						if (Material.getMaterial(material.toUpperCase()) == null) {
							sendConsole(String.format("&cConfig error: generator %s has an unrecognized material: %s", key, material));
						}
						double percent = Double.parseDouble(raw.split(":")[1]);
						totalChance += percent;
						int damage = Integer.parseInt(raw.split("!")[1].split(":")[0]);
						gc.itemList.add(new GeneratorItem(material, (byte) damage, percent));
					}
				} catch (Exception e) {
					e.printStackTrace();
					sendConsole("&cConfig error: general configuration error. Please check you config.yml");
				}
			}
			if (totalChance != 100.0) {
				sendConsole(String.format("&cConfig error: generator %s does not have a total chance of 100.0! Total chance is: %f", key, totalChance));
			}
			generatorConfigs.add(gc);

		}

		sendConsole(String.format("&aLoaded &c%d &agenerators!", generatorConfigs.size()));
	}

	public GeneratorConfig getGeneratorConfigForPlayer(OfflinePlayer p, String world) {
		GeneratorConfig gc = null;
		int id = 0;
		if (p == null) {
			gc = generatorConfigs.get(0);
			cacheOreGen(p.getUniqueId(), id);
		} else {

			int islandLevel = getLevel(p.getUniqueId(), world);

			if (p.isOnline()) {
				Player realP = p.getPlayer();
				if (this.getActiveWorlds().contains(realP.getWorld())) {
					for (GeneratorConfig gc2 : generatorConfigs) {
						if (gc2 == null) {
							continue;
						}
						if ((realP.hasPermission(gc2.permission) || gc2.permission.length() == 0) && islandLevel >= gc2.unlock_islandLevel) {
							// continue
							gc = gc2;
							id++;
						}

					}
				}
			} else {
				gc = getCachedGeneratorConfig(p.getUniqueId());
			}
		}
		if (id > 0) {
			cacheOreGen(p.getUniqueId(), id - 1);
		}
		return gc;
	}

	public List<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	public void setDisabledWorlds(List<String> disabledWorlds) {
		this.disabledWorlds = disabledWorlds;
	}

	public GeneratorConfig getCachedGeneratorConfig(UUID uuid) {
		if (cachedOregenConfigs.containsKey(uuid)) {
			return generatorConfigs.get(cachedOregenConfigs.get(uuid));
		}
		return null;
	}

	public void cacheOreGen(UUID uuid, int configID) {
		cachedOregenConfigs.put(uuid, configID);
	}
	
	
	public void sendConsole(String msg) {
		clogger.sendMessage(PREFIX + msg.replace("&", "§"));
	}
}
