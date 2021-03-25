package io.github.levtey;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class SelectionGUI implements InventoryHolder {

	private final SupremeEffects plugin;
	private final UUID uuid;
	private final Inventory inv;
	public final Map<Integer, PotionEffectType> effectSlots = new HashMap<>();
	public final int size;
	
	public SelectionGUI(SupremeEffects plugin, Player player) {
		this.plugin = plugin;
		this.uuid = player.getUniqueId();
		Map<PotionEffectType, Integer> allowedEffects = plugin.getAllowedLevels(Bukkit.getPlayer(uuid));
		size = ((allowedEffects.size() - 1) / 9 + 2) * 9;
		inv = Bukkit.createInventory(this, size, plugin.makeReadable(plugin.getConfig().getString("selectionGUI.name")));
		populateInventory(allowedEffects);
		player.openInventory(inv);
	}
	
	private void populateInventory(Map<PotionEffectType, Integer> allowedEffects) {
		if (plugin.getConfig().getBoolean("selectionGUI.fill")) {
			ItemStack fillerItem = plugin.configItem("selectionGUI.fillerItem");
			for (int i = 0; i < inv.getSize(); i++) {
				inv.setItem(i, fillerItem);
			}
		}
		int slot = 0;
		Map<PotionEffectType, Material> mappings = materialMappings();
		for (PotionEffectType type : allowedEffects.keySet().stream()
				.sorted((a, b) -> Collator.getInstance().compare(a.getName(), b.getName()))
				.collect(Collectors.toList())) {
			int currentLevel = plugin.effects.get(uuid).getOrDefault(type, -1);
			ItemStack typeItem = new ItemStack(mappings.getOrDefault(type, Material.BARRIER));
			typeItem.setAmount(Math.max(currentLevel + 1, 1));
			ItemMeta typeMeta = typeItem.getItemMeta();
			typeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
			if (currentLevel > -1) {
				typeMeta.addEnchant(Enchantment.DURABILITY, 1, true);
				typeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			typeMeta.setDisplayName(plugin.makeReadable(
					plugin.getConfig().getString("selectionGUI.itemNames")
					.replace("%effect%", plugin.capitalize(type))
					.replace("%current%", "" + (currentLevel + 1)))
					.replace("%max%", "" + (allowedEffects.get(type))));
			typeItem.setItemMeta(typeMeta);
			inv.setItem(slot, typeItem);
			effectSlots.put(slot, type);
			slot++;
			if (slot == size) break;
		}
		
		inv.setItem(size - 5, plugin.configItem("selectionGUI.resetItem"));
	}
	
	public Map<PotionEffectType, Material> materialMappings() {
		Map<PotionEffectType, Material> mappings = new HashMap<>();
		List<String> configMappings = plugin.getConfig().getStringList("effectItems");
		for (String mapping : configMappings) {
			PotionEffectType type = PotionEffectType.getByName(mapping.substring(0, mapping.indexOf(':')).toUpperCase());
			Material material = Material.getMaterial(mapping.substring(mapping.indexOf(':') + 1).toUpperCase());
			if (type == null || material == null) continue;
			mappings.put(type, material);
		}
		return mappings;
	}
	
	@Override
	public Inventory getInventory() {
		return inv;
	}

}
