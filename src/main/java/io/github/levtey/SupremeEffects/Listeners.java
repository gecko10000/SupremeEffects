package io.github.levtey.SupremeEffects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

public class Listeners implements Listener {

	private SupremeEffects plugin;
	
	public Listeners(SupremeEffects plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onSelectionClick(InventoryClickEvent evt) {
		InventoryHolder holder = evt.getInventory().getHolder();
		if (!(holder instanceof SelectionGUI)) return;
		HumanEntity ent = evt.getWhoClicked();
		if (!(ent instanceof Player)) return;
		evt.setCancelled(true);
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null || clickedInv instanceof PlayerInventory) return;
		int slot = evt.getSlot();
		SelectionGUI gui = (SelectionGUI) holder;
		PotionEffectType type = gui.effectSlots.get(slot);
		Player player = (Player) ent;
		if (type == null) {
			if (slot == gui.size - 5) {
				plugin.resetEffects(player);
				new SelectionGUI(plugin, player);
			}
			return;
		}
		new EffectGUI(plugin, player, gui.getInventory().getItem(slot).getType(), type, plugin.getAllowedLevels(player).get(type));
	}
	
	@EventHandler
	public void onEffectClick(InventoryClickEvent evt) {
		InventoryHolder holder = evt.getInventory().getHolder();
		if (!(holder instanceof EffectGUI)) return;
		HumanEntity ent = evt.getWhoClicked();
		if (!(ent instanceof Player)) return;
		evt.setCancelled(true);
		Inventory clickedInv = evt.getClickedInventory();
		if (clickedInv == null || clickedInv instanceof PlayerInventory) return;
		int slot = evt.getSlot();
		EffectGUI gui = (EffectGUI) holder;
		Player player = (Player) ent;
		if (gui.allowedLevels >= slot && slot < 9) {
			plugin.setEffect(player, gui.type, slot - 1);
			new EffectGUI(plugin, player, gui.displayMaterial, gui.type, gui.allowedLevels);
		} else if (slot == EffectGUI.SIZE - 5) {
			new SelectionGUI(plugin, player);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent evt) {
		Player player = evt.getPlayer();
		UUID uuid = player.getUniqueId();
		plugin.effects.put(uuid, plugin.load(player));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent evt) {
		Player player = evt.getPlayer();
		plugin.save(player);
		plugin.effects.remove(player.getUniqueId());
	}
	
}
