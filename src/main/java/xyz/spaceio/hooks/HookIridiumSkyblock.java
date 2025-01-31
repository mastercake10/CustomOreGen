package xyz.spaceio.hooks;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumteams.database.IridiumUser;
import com.iridium.iridiumteams.database.Team;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;


public class HookIridiumSkyblock implements SkyblockAPIHook {

	public HookIridiumSkyblock(Plugin plugin) {

	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		if (IridiumSkyblockAPI.getInstance() != null) {
			return IridiumSkyblockAPI.getInstance().getUser(Bukkit.getOfflinePlayer(uuid)).getIsland().map(island -> island.getLevel()).orElse(0);
		}

		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		if (IridiumSkyblockAPI.getInstance() != null) {
			return IridiumSkyblockAPI.getInstance().getIslandViaLocation(loc)
					.flatMap(island -> island.getOwner().map(user -> user.getUuid()));
		}

		return Optional.empty();
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		if (IridiumSkyblockAPI.getInstance() != null) {
			return new String[]{IridiumSkyblockAPI.getInstance().getWorld().toString(),
					IridiumSkyblockAPI.getInstance().getNetherWorld().toString(),
					IridiumSkyblockAPI.getInstance().getEndWorld().toString()};
		}

		return new String[]{};
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		// TODO: Not implemented for IridiumSkyblock?
	}

}
