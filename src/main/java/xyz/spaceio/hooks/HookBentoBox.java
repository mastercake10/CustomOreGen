package xyz.spaceio.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.level.Level;

public class HookBentoBox implements SkyblockAPIHook{
	
	private BentoBox api;
	
	public HookBentoBox() {
		api = (BentoBox) Bukkit.getPluginManager().getPlugin("BentoBox");
	}

	@Override
	public int getIslandLevel(UUID uuid, String onWorld) {
		if(api.getAddonsManager().getAddonByName("Level").isPresent()) {
			Level levelAddon = (Level) api.getAddonsManager().getAddonByName("Level").get();

			return (int) levelAddon.getIslandLevel(Bukkit.getWorld(onWorld), uuid);
		}
		return 0;
	}

	@Override
	public Optional<UUID> getIslandOwner(Location loc) {
		Optional<Island> optIsland = api.getIslands().getIslandAt(loc);
		Optional<UUID> optional = Optional.empty();
		
		if(optIsland.isPresent() && optIsland.get().getOwner() != null) {
			optional = Optional.of(optIsland.get().getOwner());
		}
		return optional;
	}

	@Override
	public String[] getSkyBlockWorldNames() {
		return api.getIWM().getOverWorlds().stream().map(w -> new String[]{w.getName(), w.getName() + "_nether", w.getName() + "_the_end"}).flatMap(s -> Arrays.stream(s)).toArray(String[]::new);
	}
	
	@Override
	public void sendBlockAcknowledge(Block block) {
		// TODO Auto-generated method stub
		
	}
	
}
