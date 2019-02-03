package xyz.spaceio.misc;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import xyz.spaceio.customoregen.CustomOreGen;
import xyz.spaceio.customoregen.GeneratorConfig;

public class NamePlaceholder extends EZPlaceholderHook {
	CustomOreGen cog;
	
	public NamePlaceholder(Plugin plugin, String placeholderName) {
		super(plugin, placeholderName);
		
		this.cog = (CustomOreGen) plugin;
	}

	@Override
	public String onPlaceholderRequest(Player player, String label) {
		if(!label.startsWith("generator.")) {
			return null;
		}
		
		GeneratorConfig gc = cog.getCachedGeneratorConfig(player.getUniqueId());
		if(gc == null) {
			gc = cog.getGeneratorConfigs().get(0);
		}
		
		switch(label.split("\\.")[1]) {
			case "label":
				return gc.label;
			
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
