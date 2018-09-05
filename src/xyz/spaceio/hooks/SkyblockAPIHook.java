package xyz.spaceio.hooks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public interface SkyblockAPIHook {
	/**
	 * Returns the island level for a defined player uuid
	 * 
	 * @param uuid UUID of the island owner
	 * @return island level
	 */
	public int getIslandLevel(UUID uuid);
	
	/**
	 * Gets the owner of an island on a certain location
	 * 
	 * @param loc location to check for island
	 * @return island owner UUID
	 */
	public UUID getIslandOwner(Location loc);
	
	/**
	 * Obtains the name of the skyblock world
	 * 
	 * @return the name of the skyblock world
	 */
	public String getSkyBlockWorldName();
}
