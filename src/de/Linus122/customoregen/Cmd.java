package de.Linus122.customoregen;

import java.io.IOException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Cmd implements CommandExecutor {
	Main main;

	public Cmd(Main main) {
		this.main = main;
	}

	public boolean onCommand(CommandSender cs, Command arg1, String arg2, String[] arg3) {
		if (!cs.hasPermission("customoregen.admin")) {
			cs.sendMessage("You dont have permissions.");
		} else {
			try {
				this.main.reload();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cs.sendMessage("§aConfig reloaded!");
		}
		return true;
	}
}
