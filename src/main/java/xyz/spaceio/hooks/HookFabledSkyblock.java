package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;


public class HookFabledSkyblock implements SkyblockAPIHook{
	
	private SkyBlock api;
	
	public HookFabledSkyblock() {
		api = (SkyBlock) Bukkit.getPluginManager().getPlugin("FabledSkyBlock");
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		// create an offline player object because this plugin (unlike the others) does not have a method to obtain an island by UUID.
		OfflinePlayer skyblockPlayer = Bukkit.getOfflinePlayer(uuid);
		
		// api does not provide any optional, so doing a few checks here
		if(api.getIslandManager() != null) {
			if(api.getIslandManager().getIsland(skyblockPlayer) != null) {
				Island is =  api.getIslandManager().getIsland(skyblockPlayer);
				if(is.getLevel() != null) {
					return (int) is.getLevel().getLevel();
				}
			}
		}
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<UUID> optional = Optional.empty();
		// some more classic null checks...
		if(api.getIslandManager() != null) {
			if(api.getIslandManager().getIslandAtLocation(loc) != null) {
				if(api.getIslandManager().getIslandAtLocation(loc).getOwnerUUID() != null) {
					optional = Optional.of(api.getIslandManager().getIslandAtLocation(loc).getOwnerUUID());
				}
			}
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		if(api.getWorldManager() != null) {
			// a method for getting the islands is missing, so using what we have
			return Bukkit.getWorlds().stream().filter(w -> api.getWorldManager().isIslandWorld(w)).map(w -> w.getName()).toArray(String[]::new);
		}
		return null;
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		if(block != null && api.getIslandManager().getIslandAtLocation(block.getLocation()) != null) {
			api.getLevellingManager().updateLevel(api.getIslandManager().getIslandAtLocation(block.getLocation()), block.getLocation());
		}
	}
	
}
