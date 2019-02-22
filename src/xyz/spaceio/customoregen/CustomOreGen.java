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
import java.util.Optional;
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
import xyz.spaceio.config.ConfigHandler;
import xyz.spaceio.config.JSONConfig;
import xyz.spaceio.hooks.HookASkyBlock;
import xyz.spaceio.hooks.HookAcidIsland;
import xyz.spaceio.hooks.HookBentoBox;
import xyz.spaceio.hooks.HookIslandWorld;
import xyz.spaceio.hooks.HookPlotSquared;
import xyz.spaceio.hooks.HookSkyblockEarth;
import xyz.spaceio.hooks.HookSpaceSkyblock;
import xyz.spaceio.hooks.HookSuperiorSkyblock;
import xyz.spaceio.hooks.SkyblockAPIHook;
import xyz.spaceio.misc.NamePlaceholder;
import xyz.spaceio.hooks.HookuSkyBlock;

public class CustomOreGen extends JavaPlugin {

	/*
	 * Configurations for all generators (defined in the config.yml)
	 */
	private List<GeneratorConfig> generatorConfigs = new ArrayList<GeneratorConfig>();

	/*
	 * Disabled world blacklist
	 */
	private List<String> disabledWorlds = new ArrayList<String>();

	public List<GeneratorConfig> getGeneratorConfigs() {
		return generatorConfigs;
	}

	public void setGeneratorConfigs(List<GeneratorConfig> generatorConfigs) {
		this.generatorConfigs = generatorConfigs;
	}

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
	 * Object that handles the loading process of the config.yml file
	 */
	private ConfigHandler configHandler = new ConfigHandler(this, "plugins/CustomOreGen/config.yml");;

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
			configHandler.loadConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cachedOregenJsonConfig = new JSONConfig(cachedOregenConfigs, new TypeToken<HashMap<UUID, Integer>>() {
		}.getType(), this);

		cachedOregenConfigs = (HashMap<UUID, Integer>) cachedOregenJsonConfig.get();

		if (cachedOregenConfigs == null) {
			cachedOregenConfigs = new HashMap<UUID, Integer>();
		}
		disabledWorlds = getConfig().getStringList("disabled-worlds");
		
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
        	new NamePlaceholder(this, "oregen").hook();
        }
		
		new Metrics(this);
	}

	/**
	 * creates a new api hook instance for the used skyblock plugin
	 */
	private void loadHook() {
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("ASkyBlock")) {
			skyblockAPI = new HookASkyBlock();
			sendConsole("&aUsing ASkyBlock as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("AcidIsland")) {
			skyblockAPI = new HookAcidIsland();
			sendConsole("&aUsing AcidIsland as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("uSkyBlock")) {
			skyblockAPI = new HookuSkyBlock();
			sendConsole("&aUsing uSkyBlock as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("BentoBox")) {
			skyblockAPI = new HookBentoBox();
			sendConsole("&aUsing BentoBox as SkyBlock-Plugin");
		//} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("SkyBlock")) {
			//skyblockAPI = new HookSkyblockEarth();
			//sendConsole("&aUsing SkyblockEarth as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlotSquared")) {
			skyblockAPI = new HookPlotSquared();
			sendConsole("&aUsing PlotSquared as SkyBlock-Plugin");
		} else if (Bukkit.getServer().getPluginManager().isPluginEnabled("IslandWorld")) {
			skyblockAPI = new HookIslandWorld();
			sendConsole("&aUsing IslandWorld as SkyBlock-Plugin");
		}else if (Bukkit.getServer().getPluginManager().isPluginEnabled("SuperiorSkyblock2")) {
			skyblockAPI = new HookSuperiorSkyblock();
			sendConsole("&aUsing SuperiorSkyblock2 as SkyBlock-Plugin");
		}else if (Bukkit.getServer().getPluginManager().isPluginEnabled("SpaceSkyblock")) {
			skyblockAPI = new HookSpaceSkyblock();
			sendConsole("&aUsing SpaceSkyblock as SkyBlock-Plugin");
		} else {
			sendConsole("§cYou are not using any skyblock plugin! This plugin only works with a listed skyblock plugin! (check documentations)");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		cachedOregenJsonConfig.saveToDisk(cachedOregenConfigs);
	}

	public List<World> getActiveWorlds() {
		return Arrays.stream(skyblockAPI.getSkyBlockWorldNames()).map(v -> Bukkit.getWorld(v))
				.collect(Collectors.toList());
	}

	public int getLevel(UUID uuid, String world) {
		return skyblockAPI.getIslandLevel(uuid, world);
	}

	public OfflinePlayer getOwner(Location loc) {
		if (skyblockAPI.getIslandOwner(loc) == null) {
			return null;
		}
		Optional<UUID> uuid = skyblockAPI.getIslandOwner(loc);
		if (!uuid.isPresent()) {
			return null;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid.get());

		return p;
	}

	public void reload() throws IOException {
		reloadConfig();
		configHandler.loadConfig();
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
						if ((realP.hasPermission(gc2.permission) || gc2.permission.length() == 0)
								&& islandLevel >= gc2.unlock_islandLevel) {
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
