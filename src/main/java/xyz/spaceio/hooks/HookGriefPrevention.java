package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class HookGriefPrevention implements SkyblockAPIHook {

	private GriefPrevention griefPrevention;

	public HookGriefPrevention() {
		griefPrevention = GriefPrevention.instance;
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
		if (claim != null && claim.ownerID != null) {
			return Optional.of(claim.ownerID);
		}
		return Optional.empty();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return Bukkit.getWorlds().stream()
				.filter(world -> griefPrevention.claimsEnabledForWorld(world))
				.map(World::getName)
				.toArray(String[]::new);
	}

	@Override
	public void sendBlockAcknowledge(Block block) {
	}
}
