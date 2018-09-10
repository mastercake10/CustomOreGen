package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class HookuSkyBlock implements SkyblockAPIHook {

	private uSkyBlockAPI api;

	public HookuSkyBlock() {
		api = (uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("uSkyBlock");
	}

	@Override
	public int getIslandLevel(UUID uuid, String world) {
		return (int) Math.floor(api.getIslandLevel(Bukkit.getPlayer(uuid)));
	}

	@Override
	public UUID getIslandOwner(Location loc) {
		String player = api.getIslandInfo(loc).getLeader();
		if ((Bukkit.getPlayer(player) != null) && (Bukkit.getPlayer(player).getUniqueId() != null)) {
			return Bukkit.getOfflinePlayer(player).getUniqueId();
		} else {
			return null;
		}
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		api.getConfig().getString("options.general.worldName");
		return new String[] { api.getConfig().getString("options.general.worldName") };
	}

}
