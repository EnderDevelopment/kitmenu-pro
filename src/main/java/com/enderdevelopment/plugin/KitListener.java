package com.enderdevelopment.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public final class KitListener implements Listener {
    private final Main plugin;

    public KitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(event.getWhoClicked() instanceof Player player) || !(top.getHolder() instanceof KitMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != top) return;
        int slot = event.getSlot();
        if (slot == 45) {
            plugin.openKitMenu(player, holder.getPage() - 1);
        } else if (slot == 53) {
            plugin.openKitMenu(player, holder.getPage() + 1);
        } else if (slot >= 0 && slot < 45) {
            plugin.selectKit(player, holder.getPage(), slot);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof KitMenuHolder) {
            event.setCancelled(true);
        }
    }
}
