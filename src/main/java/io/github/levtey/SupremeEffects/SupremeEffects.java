package io.github.levtey.SupremeEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;

public class SupremeEffects extends JavaPlugin {

	public void onEnable() {
		saveDefaultConfig();
		new EffectsCommand(this);
		new Listeners(this);
	}
	
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			InventoryHolder openHolder = player.getOpenInventory().getTopInventory().getHolder();
			if (openHolder instanceof EffectGUI || openHolder instanceof SelectionGUI) {
				player.closeInventory();
			}
		}
	}
	
	public void resetEffects(Player player) {
		player.getActivePotionEffects().stream()
		.map(PotionEffect::getType)
		.forEach(player::removePotionEffect);
	}
	
	public void setEffect(Player player, PotionEffectType type, int level) {
		player.removePotionEffect(type);
		if (level != -1) {
			PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, level, false, false);
			player.addPotionEffect(effect);
		}
	}
	
	public NamespacedKey key(String key) {
		return new NamespacedKey(this, key);
	}
	
	public ItemStack configItem(String path) {
		return configItem(path, new HashMap<>());
	}
	
	public ItemStack configItem(String path, Map<String, String> placeholders) {
		return configItem(path, placeholders, getConfig().getInt(path + ".material", 1));
	}
	
	public ItemStack configItem(String path, Map<String, String> placeholders, int amount) {
		ItemStack item = new ItemStack(Material.getMaterial(getConfig().getString(path + ".material").toUpperCase()));
		item.setAmount(amount);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(makeReadable(getConfig().getString(path + ".name")));
		meta.setLore(
				getConfig().getStringList(path + ".lore").stream().map(this::makeReadable).collect(Collectors.toList())
		);
		item.setItemMeta(meta);
		return item;
	}
	
	public Map<PotionEffectType, Integer> getAllowedLevels(Player player) {
		Map<PotionEffectType, Integer> allowed = new HashMap<>();
		for (PotionEffectType type : PotionEffectType.values()) {
			for (int i = 8; i >= 1; i--) {
				if (player.hasPermission("supremeeffects." + type.getName().toLowerCase() + "." + i)) {
					allowed.put(type, i);
					break;
				}
			}
		}
		return allowed;
	}
	
	public String makeReadable(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}
	
	public String capitalize(PotionEffectType type) {
		String[] nameArray = type.getName().split("_");
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 0; i < nameArray.length; i++) {
			joiner.add(nameArray[i].substring(0, 1) + nameArray[i].substring(1).toLowerCase());
		}
		return joiner.toString();
	}
	
}
