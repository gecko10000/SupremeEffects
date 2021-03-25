package io.github.levtey;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public class EffectGUI implements InventoryHolder {
	
	private final SupremeEffects plugin;
	private final UUID uuid;
	public final Material displayMaterial;
	public final PotionEffectType type;
	public final int allowedLevels;
	public int selectedLevel;
	private final Inventory inv;
	public static final int SIZE = 18;
	
	public EffectGUI(SupremeEffects plugin, Player player, Material displayMaterial, PotionEffectType type, int allowedLevels) {
		this.plugin = plugin;
		this.uuid = player.getUniqueId();
		this.displayMaterial = displayMaterial;
		this.type = type;
		this.allowedLevels = allowedLevels;
		this.selectedLevel = plugin.effects.get(uuid).getOrDefault(type, -1);
		inv = Bukkit.createInventory(this, SIZE, plugin.makeReadable(
				plugin.getConfig().getString("effectGUI.name")
				.replace("%effect%", plugin.capitalize(type))));
		populateInventory();
		player.openInventory(inv);
	}
	
	private void populateInventory() {
		if (plugin.getConfig().getBoolean("effectGUI.fill")) {
			ItemStack fillerItem = plugin.configItem("effectGUI.fillerItem");
			for (int i = 0; i < inv.getSize(); i++) {
				inv.setItem(i, fillerItem);
			}
		}
		ItemStack offItem = plugin.configItem("effectGUI.offItem");
		if (selectedLevel == -1) {
			ItemMeta offMeta = offItem.getItemMeta();
			offMeta.addEnchant(Enchantment.DURABILITY, 1, true);
			offMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
			offItem.setItemMeta(offMeta);
		}
		inv.setItem(0, offItem);
		
		for (int i = 0; i < allowedLevels && i < 8; i++) {
			ItemStack onItem = new ItemStack(displayMaterial, i+1);
			ItemMeta onMeta = onItem.getItemMeta();
			onMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
			onMeta.setDisplayName(plugin.makeReadable(
					plugin.getConfig().getString("effectGUI.levelNames")
					.replace("%level%", "" + (i+1))));
			if (selectedLevel == i) {
				onMeta.addEnchant(Enchantment.DURABILITY, 1, true);
				onMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			onItem.setItemMeta(onMeta);
			inv.setItem(i + 1, onItem);
		}
		
		inv.setItem(SIZE - 5, plugin.configItem("effectGUI.backItem"));
	}

	public Inventory getInventory() {
		return inv;
	}

}
