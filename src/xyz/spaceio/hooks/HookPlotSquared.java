package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

import com.github.intellectualsites.plotsquared.api.PlotAPI;


public class HookPlotSquared implements SkyblockAPIHook {

	private PlotAPI api;

	public HookPlotSquared() {
		api = new PlotAPI();
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
	
		
		if(api.getPlotSquared().getApplicablePlotArea(getPSLocation(loc)).getPlotCount() > 0) {
			UUID owner = api.getPlotSquared().getApplicablePlotArea(getPSLocation(loc)).getPlots().iterator().next().getOwner();
			optional = Optional.of(owner);
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getPlotSquared().worlds.getConfigurationSection("worlds").getKeys(false).stream().toArray(String[]::new);
	}
	
	private com.github.intellectualsites.plotsquared.plot.object.Location getPSLocation(Location bukkitLoc) {
		com.github.intellectualsites.plotsquared.plot.object.Location loc = new com.github.intellectualsites.plotsquared.plot.object.Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(), bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
		
		return loc;
	}
}
