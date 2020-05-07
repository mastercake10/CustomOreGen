package xyz.spaceio.hooks;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HookVanilla implements SkyblockAPIHook{

	public HookVanilla() {

	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
		
		List<Player> list = loc.getWorld().getPlayers().stream()
			.sorted(Comparator.comparingDouble(e -> e.getLocation().distance(loc)))
			.collect(Collectors.toList());
		
		
		if(list.size() > 0) {
			optional = Optional.of(((Player) list.get(0)).getUniqueId());
		}
		
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return Bukkit.getWorlds().stream().map(w -> w.getName()).toArray(String[]::new);
		
	}
}
