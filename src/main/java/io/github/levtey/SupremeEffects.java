package io.github.levtey;

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
	
	public Map<UUID, Map<PotionEffectType, Integer>> effects = new HashMap<>();
	private BukkitTask effectTask;

	public void onEnable() {
		saveDefaultConfig();
		reloadEffectTask();
		for (Player player : Bukkit.getOnlinePlayers()) {
			effects.put(player.getUniqueId(), load(player));
		}
		new EffectsCommand(this);
		new Listeners(this);
	}
	
	public void onDisable() {
		effectTask.cancel();
		for (Player player : Bukkit.getOnlinePlayers()) {
			InventoryHolder openHolder = player.getOpenInventory().getTopInventory().getHolder();
			if (openHolder instanceof EffectGUI || openHolder instanceof SelectionGUI) {
				player.closeInventory();
			}
			save(player);
		}
	}
	
	public void resetEffects(Player player) {
		for (PotionEffectType type : PotionEffectType.values()) {
			setEffect(player, type, -1);
		}
	}
	
	public void setEffect(Player player, PotionEffectType type, int level) {
		UUID uuid = player.getUniqueId();
		if (level < 0) {
			effects.get(uuid).remove(type);
		} else {
			effects.get(uuid).put(type, level);
		}
	}
	
	public void reloadEffectTask() {
		if (effectTask != null) effectTask.cancel();
		effectTask = effectTask();
	}
	
	private BukkitTask effectTask() {
		long effectAddInterval = getConfig().getLong("effectAddInterval");
		long extraEffectTime = getConfig().getLong("extraEffectTime");
		return Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			for (UUID uuid : effects.keySet()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) continue;
				Map<PotionEffectType, Integer> playerEffects = effects.get(uuid);
				for (PotionEffectType type : playerEffects.keySet()) {
					Bukkit.getScheduler().runTask(this, () -> {
						player.addPotionEffect(type.createEffect((int) (effectAddInterval + extraEffectTime), playerEffects.get(type)));
					});
				}
			}
		}, 0L, effectAddInterval);
	}
	
	public Map<PotionEffectType, Integer> load(Player player) {
		PersistentDataContainer pdc = player.getPersistentDataContainer();
		Map<PotionEffectType, Integer> savedEffects = new HashMap<>();
		Map<PotionEffectType, Integer> allowedEffects = getAllowedLevels(player);
		for (PotionEffectType type : PotionEffectType.values()) {
			if (pdc.has(key(type.getName()), PersistentDataType.INTEGER)) {
				int savedLevel = pdc.get(key(type.getName()), PersistentDataType.INTEGER);
				if (allowedEffects.getOrDefault(type, -1) >= savedLevel) {
					savedEffects.put(type, savedLevel);
				}
			}
		}
		return savedEffects;
	}
	
	public void save(Player player) {
		PersistentDataContainer pdc = player.getPersistentDataContainer();
		for (PotionEffectType type : PotionEffectType.values()) {
			pdc.remove(key(type.getName()));
		}
		Map<PotionEffectType, Integer> currentEffects = effects.getOrDefault(player.getUniqueId(), new HashMap<PotionEffectType, Integer>());
		for (PotionEffectType type : currentEffects.keySet()) {
			pdc.set(key(type.getName()), PersistentDataType.INTEGER, currentEffects.getOrDefault(type, 0));
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
