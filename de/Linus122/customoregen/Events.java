package de.Linus122.customoregen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;


public class Events implements Listener {
	@SuppressWarnings({ "deprecation", "unused" })
	@EventHandler
	public void onFromTo(BlockFromToEvent event){
	    int id = event.getBlock().getTypeId();
	    if ((id >= 8) && (id <= 11)){
	    	Block b = event.getToBlock();
	    	int toid = b.getTypeId();
	    	if ((toid == 0) && (generatesCobble(id, b))){
	    		GeneratorConfig gc = null;

				Player p = Main.getOwner(b.getLocation());
				if(p == null){
					gc = Main.generatorConfigs.get(0);	
				}else{
					int islandLevel = Main.getLevel(p);

					if(Main.activeInWorld.getName().equals(b.getWorld().getName())){
							for(GeneratorConfig gc2 : Main.generatorConfigs){
								if(gc2 == null){
									continue;
								}
								
									if(p.hasPermission(gc2.permission) &&
											islandLevel >= 
											gc2.unlock_islandLevel ){
										//Weiter
										gc = gc2;
									}	
							
							}	
					}
				}
				if(gc == null) return;
				if(getObject(gc) == null) return;
				GeneratorItem winning = getObject(gc);
				if(Material.getMaterial(winning.name) == null) return;
				//b.setType(Material.getMaterial(winning));	
				b.setTypeIdAndData(Material.getMaterial(winning.name).getId() , winning.damage, true);
	    	}
		}
	}
	public GeneratorItem getObject(GeneratorConfig gc){
		
		Random random = new Random();
		double d = random.nextDouble() * 100;
		for(GeneratorItem key : gc.itemList){
			if ((d -= key.chance) < 0) return key;
		}
		return new GeneratorItem("COBBLESTONE", (byte) 0, 0); //DEFAULT
	}
	private final BlockFace[] faces = { BlockFace.SELF, BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    
	public boolean generatesCobble(int id, Block b){
	    int mirrorID1 = (id == 8) || (id == 9) ? 10 : 8;
	    int mirrorID2 = (id == 8) || (id == 9) ? 11 : 9;
	    for(BlockFace face : this.faces){
	        Block r = b.getRelative(face, 1);
	        if ((r.getTypeId() == mirrorID1) || (r.getTypeId() == mirrorID2)) {
	            return true;
	       }
	    }
	    return false;
	 }
}
