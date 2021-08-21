package io.github.levtey.SupremeEffects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class EffectsCommand implements CommandExecutor {
	
	private SupremeEffects plugin;
	private static final String NO_PERMS = "&cYou don't have permission!";
	private static final String NOT_FROM_CONSOLE = "&cYou can't run this from console!";
	private static final String INVALID_INPUT = "&cInvalid input!";
	
	public EffectsCommand(SupremeEffects plugin) {
		this.plugin = plugin;
		plugin.getCommand("effects").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender.hasPermission("supremeeffects.gui"))) return commandResponse(sender, NO_PERMS);
			if (!(sender instanceof Player)) return commandResponse(sender, NOT_FROM_CONSOLE);
			new SelectionGUI(plugin, (Player) sender);
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!(sender.hasPermission("supremeeffects.reload"))) return commandResponse(sender, NO_PERMS);
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
		} else if (args[0].equalsIgnoreCase("reset")) {
			if (!(sender.hasPermission("supremeeffects.reset"))) return commandResponse(sender, NO_PERMS);
			Player targetPlayer = null;
			if (args.length > 1) targetPlayer = Bukkit.getPlayer(args[1]);
			if (targetPlayer == null && sender instanceof Player) targetPlayer = (Player) sender;
			if (targetPlayer == null) return commandResponse(sender, INVALID_INPUT);
			plugin.resetEffects(targetPlayer);
		}
		return true;
	}
	
	private boolean commandResponse(CommandSender sender, String input) {
		sender.sendMessage(plugin.makeReadable(input));
		return true;
	}

}
