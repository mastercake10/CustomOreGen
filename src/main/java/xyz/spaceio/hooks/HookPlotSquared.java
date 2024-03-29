package xyz.spaceio.hooks;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.plotsquared.core.plot.PlotArea;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.plotsquared.core.PlotAPI;



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
		PlotArea plotArea = api.getPlotSquared().getPlotAreaManager().getApplicablePlotArea(getPSLocation(loc));
		if(plotArea != null && plotArea.getPlotCount() > 0) {
			Set<UUID> owners = Objects.requireNonNull(api.getPlotSquared().getPlotAreaManager().
					getApplicablePlotArea(getPSLocation(loc))).getPlots().iterator().next().getOwners();
			if(!owners.isEmpty()) {
				return Optional.of(owners.iterator().next());
			}
		}
		return Optional.empty();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getPlotSquared().getWorldConfiguration().getConfigurationSection("worlds").getKeys(false).stream().toArray(String[]::new);
	}
	
	private com.plotsquared.core.location.Location getPSLocation(Location bukkitLoc) {
		return com.plotsquared.core.location.Location.at(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(), bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		// TODO Auto-generated method stub
		
	}
}
