package xyz.spaceio.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.bukkit.Material;

import xyz.spaceio.customoregen.CustomOreGen;
import xyz.spaceio.customoregen.GeneratorConfig;
import xyz.spaceio.customoregen.GeneratorItem;

public class ConfigHandler {
	
	private File configFile;
	private CustomOreGen plugin;
	
	public ConfigHandler(CustomOreGen plugin, String configFilePath) {
		this.plugin = plugin;
		this.configFile = new File(configFilePath);
		
		// create plugin directory if not exist
		if(this.configFile.getParentFile().exists()) {
			this.configFile.getParentFile().mkdirs();
		}
	}

	/**
	 * Just a method that sorts out stupid configuration mistakes made by kids who
	 * always give 1-star-reviews on Spigot.
	 */
	public void loadConfig() throws IOException {
		// Writing default config to data directory
		
		if (!this.configFile.exists()) {
			FileOutputStream writer = new FileOutputStream(configFile);
			InputStream out = CustomOreGen.class.getClassLoader().getResourceAsStream("config.yml");
			byte[] linebuffer = new byte[4096];
			int lineLength = 0;
			while ((lineLength = out.read(linebuffer)) > 0) {
				writer.write(linebuffer, 0, lineLength);
			}
			writer.close();
		}

		plugin.reloadConfig();
		plugin.setGeneratorConfigs(new ArrayList<GeneratorConfig>());
		for (String key : plugin.getConfig().getConfigurationSection("generators").getKeys(false)) {
			double totalChance = 0d;
			GeneratorConfig gc = new GeneratorConfig();
			gc.permission = plugin.getConfig().getString("generators." + key + ".permission");
			gc.unlock_islandLevel = plugin.getConfig().getInt("generators." + key + ".unlock_islandLevel");
			if (gc.permission == null) {
				plugin.sendConsole(String.format("&cConfig error: generator %s does not have a valid permission entry!", key));
			}
			if (gc.unlock_islandLevel > 0 && gc.permission.length() > 1) {
				plugin.sendConsole(String.format("&cConfig error: generator %s has both a permission and level setup! Be sure to choose one of them!", key));
			}

			for (String raw : plugin.getConfig().getStringList("generators." + key + ".blocks")) {
				try {
					if (!raw.contains("!")) {
						String material = raw.split(":")[0];
						if (Material.getMaterial(material.toUpperCase()) == null) {
							plugin.sendConsole(String.format("&cConfig error: generator %s has an unrecognized material: %s", key, material));
						}
						double percent = Double.parseDouble(raw.split(":")[1]);
						totalChance += percent;
						gc.itemList.add(new GeneratorItem(material, (byte) 0, percent));
					} else {
						String material = raw.split("!")[0];
						if (Material.getMaterial(material.toUpperCase()) == null) {
							plugin.sendConsole(String.format("&cConfig error: generator %s has an unrecognized material: %s", key, material));
						}
						double percent = Double.parseDouble(raw.split(":")[1]);
						totalChance += percent;
						int damage = Integer.parseInt(raw.split("!")[1].split(":")[0]);
						gc.itemList.add(new GeneratorItem(material, (byte) damage, percent));
					}
				} catch (Exception e) {
					e.printStackTrace();
					plugin.sendConsole("&cConfig error: general configuration error. Please check you config.yml");
				}
			}
			if (totalChance != 100.0) {
				plugin.sendConsole(String.format("&cConfig error: generator %s does not have a total chance of 100.0! Total chance is: %f", key, totalChance));
			}
			plugin.getGeneratorConfigs().add(gc);

		}

		plugin.sendConsole(String.format("&aLoaded &c%d &agenerators!", plugin.getGeneratorConfigs().size()));
	}
}
