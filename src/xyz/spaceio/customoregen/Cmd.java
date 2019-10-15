package xyz.spaceio.customoregen;

import java.io.IOException;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFromToEvent;

public class Cmd implements CommandExecutor {
	CustomOreGen plugin;

	public Cmd(CustomOreGen main) {
		this.plugin = main;
	}

	public boolean onCommand(CommandSender cs, Command arg1, String arg2, String[] arg3) {
		if (!cs.hasPermission("customoregen.admin")) {
			cs.sendMessage("You dont have permissions.");
		} else {
			try {
				this.plugin.reload();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cs.sendMessage("§aConfig reloaded!");
		}
		
		Player p = (Player) cs;
		
		Block from = p.getWorld().getBlockAt(1200,76,1194);
		Block to = p.getWorld().getBlockAt(1200,76,1195);
		
		long before = System.currentTimeMillis();
		
		for(int i = 0; i < 10000; i++) {
			CustomOreGen.eventClass.onFromTo(new BlockFromToEvent(from, to));
		}
		
		p.sendMessage(System.currentTimeMillis() - before + "");
	
		
		return true;
	}
}
