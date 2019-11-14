package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;

import com.intellectualcrafters.plot.api.PlotAPI;

public class HookPlotSquaredLegacy implements SkyblockAPIHook {

	private PlotAPI api;

	public HookPlotSquaredLegacy() {
		api = new PlotAPI();
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
		if(api.getPlot(loc) != null) {
			optional = Optional.of(api.getPlot(loc).getOwners().iterator().next());
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getPlotWorlds();
	}
}