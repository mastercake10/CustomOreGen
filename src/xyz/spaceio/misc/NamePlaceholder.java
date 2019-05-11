package xyz.spaceio.misc;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import xyz.spaceio.customoregen.CustomOreGen;
import xyz.spaceio.customoregen.GeneratorConfig;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class NamePlaceholder extends PlaceholderExpansion {

	CustomOreGen cog;

	public NamePlaceholder(CustomOreGen plugin) {
		this.cog = plugin;
	}

	// This tells PlaceholderAPI to not unregister your expansion on reloads since it is provided by the dependency
	// Introduced in PlaceholderAPI 2.8.5
	@Override
	public boolean persist() {
		return true;
	}

	// Our placeholders will be %oregen_<params>%
	@Override
	public String getIdentifier() {
		return "oregen";
	}

	// the author
	@Override
	public String getAuthor() {
		return "Linus122";
	}

	// This is the version
	@Override
	public String getVersion() {
		return cog.getDescription().getVersion();
	}

	@Override
	public String onRequest(OfflinePlayer player, String label) {
		if(!label.startsWith("generator.")) {
			return null;
		}
		
		GeneratorConfig gc = cog.getCachedGeneratorConfig(player.getUniqueId());
		if(gc == null) {
			gc = cog.getGeneratorConfigs().get(0);
		}
		
		switch(label.split("\\.")[1]) {
			case "label":
			case "name":
				return gc.label;

			case "level":
				return String.valueOf(gc.unlock_islandLevel);
				
			case "permission":
				return gc.permission;
		}
		return null;
	}
}
