package xyz.spaceio.hooks;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public interface SkyblockAPIHook {
	/**
	 * Returns the island level for a defined player uuid
	 * 
	 * @param uuid UUID of the island owner
	 * @param in World world of the island
	 * @return island level
	 */
	public int getIslandLevel(UUID uuid, String inWorld);
	
	/**
	 * Gets the owner of an island on a certain location
	 * 
	 * @param loc location to check for island
	 * @return island owner UUID
	 */
	public Optional<UUID> getIslandOwner(Location loc);
	
	/**
	 * Obtains the names of the skyblock worlds
	 * 
	 * @return the names of the skyblock worlds
	 */
	public String[] getSkyBlockWorldNames();
}
