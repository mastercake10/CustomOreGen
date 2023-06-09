package xyz.spaceio.customoregen;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd implements CommandExecutor {
	CustomOreGen plugin;

	public Cmd(CustomOreGen main) {
		this.plugin = main;
	}

	public boolean onCommand(CommandSender cs, Command arg1, String arg2, String[] args) {
		if (!cs.hasPermission("customoregen.admin")) {
			cs.sendMessage("You dont have permissions.");
			return true;
		} else {
			if(args.length > 0) {
				switch(args[0].toLowerCase()) {
					case "reload":
						try {
							this.plugin.reload();
							cs.sendMessage("§aConfig reloaded!");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					case "inspect":
						String playerName = args.length > 1 ? args[1] : cs.getName();
						
						Player player = Bukkit.getPlayer(playerName);
						if (player == null) {
							cs.sendMessage("§cInvalid player");
							return true;
						}
						
						cs.sendMessage(String.format("§c====== INFO %s =======", player.getName()));
						cs.sendMessage("§2Skyblock Plugin: §a" + plugin.getHookName());
						cs.sendMessage("§2Island level: §a" + plugin.getLevel(player.getUniqueId(), player.getWorld().getName()));
						cs.sendMessage("§2Island owner: §a" + plugin.getApplicablePlayer(player.getLocation()).getName());
						
						GeneratorConfig gc = plugin.getGeneratorConfigForPlayer(player, player.getWorld().getName());
						cs.sendMessage("§3Applied Generator name: §a" + gc.label);
						cs.sendMessage("§3Generator permission: §a" + gc.permission);
						cs.sendMessage("§3Generator unlock level: §a" + gc.unlock_islandLevel);
						
						return true;
						
					default:
						showHelp(cs);
						return true;
				}
			}else {
				showHelp(cs);
				return true;
			}
		}
		
	}
	private void showHelp(CommandSender cs) {
		cs.sendMessage("§a/customoregen reload §2-Reloads the config.yml");
		cs.sendMessage("§a/customoregen inspect <player> §2-Returns information about the applied generator");
	}
}
