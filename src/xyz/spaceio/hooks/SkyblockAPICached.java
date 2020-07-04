package xyz.spaceio.hooks;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

public class SkyblockAPICached {
	private SkyblockAPIHook hook;
	
	public SkyblockAPICached(SkyblockAPIHook hook) {
		this.hook = hook;
	}
	
	LoadingCache<Entry<UUID, String>, Integer> cachedIslandLevel = CacheBuilder.newBuilder()
			   .maximumSize(1000)
			   .expireAfterWrite(10, TimeUnit.SECONDS)
			   .build(
			       new CacheLoader<Entry<UUID, String>, Integer>() {
			         public Integer load(Entry<UUID, String> key) {
			           return hook.getIslandLevel(key.getKey(), key.getValue());
			         }
			       });
	
	LoadingCache<Location, Optional<UUID>> cachedIslandOwner = CacheBuilder.newBuilder()
			   .maximumSize(1000)
			   .expireAfterWrite(100, TimeUnit.SECONDS)
			   .build(
			       new CacheLoader<Location, Optional<UUID>>() {
			         public Optional<UUID> load(Location key) {
			           return hook.getIslandOwner(key);
			         }
			       });
	
	
	public int getIslandLevel(UUID owner, String world) {
		Entry<UUID, String> entry = Maps.immutableEntry(owner, world);
		
		try {
			return cachedIslandLevel.get(entry);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public Optional<UUID> getIslandOwner(Location loc){
		try {
			return cachedIslandOwner.get(loc);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] getSkyBlockWorldNames() {
		return hook.getSkyBlockWorldNames();
	}
	
	public void sendBlockAcknowledge(Block block) {
		hook.sendBlockAcknowledge(block);
	}
}
