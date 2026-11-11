package com.enderdevelopment.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<UUID, Map<String, Map<Integer, Integer>>> layouts = new HashMap<>();
    private final Map<UUID, String> selectedKits = new HashMap<>();
    private File kitsFile;
    private File layoutsFile;
    private YamlConfiguration kitsConfig;
    private YamlConfiguration layoutsConfig;

    @Override
    public void onEnable() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        layoutsFile = new File(getDataFolder(), "layouts.yml");
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadData();
        KitCommand command = new KitCommand(this);
        getCommand("kit").setExecutor(command);
        getCommand("kit").setTabCompleter(command);
        Bukkit.getPluginManager().registerEvents(new KitListener(this), this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    private void loadData() {
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        layoutsConfig = YamlConfiguration.loadConfiguration(layoutsFile);
        ConfigurationSection kitSection = kitsConfig.getConfigurationSection("kits");
        if (kitSection != null) {
            for (String key : kitSection.getKeys(false)) {
                Kit kit = new Kit(key);
                ConfigurationSection items = kitSection.getConfigurationSection(key + ".items");
                if (items != null) for (String slot : items.getKeys(false)) {
                    try {
                        int index = Integer.parseInt(slot);
                        ItemStack item = items.getItemStack(slot);
                        if (index >= 0 && index < 41 && item != null && !item.getType().isAir()) kit.getContents().put(index, item);
                    } catch (NumberFormatException ignored) {
                        getLogger().warning("Ignoring invalid slot in kit " + key + ": " + slot);
                    }
                }
                kits.put(key, kit);
            }
        }
        ConfigurationSection players = layoutsConfig.getConfigurationSection("players");
        if (players == null) return;
        for (String uuidText : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidText);
                Map<String, Map<Integer, Integer>> playerLayouts = new HashMap<>();
                ConfigurationSection playerKits = players.getConfigurationSection(uuidText);
                if (playerKits == null) continue;
                for (String kitName : playerKits.getKeys(false)) {
                    Map<Integer, Integer> mapping = new HashMap<>();
                    ConfigurationSection slots = playerKits.getConfigurationSection(kitName);
                    if (slots != null) for (String source : slots.getKeys(false)) {
                        try {
                            int sourceSlot = Integer.parseInt(source);
                            int destination = slots.getInt(source, sourceSlot);
                            if (sourceSlot >= 0 && sourceSlot < 41 && destination >= 0 && destination < 41) mapping.put(sourceSlot, destination);
                        } catch (NumberFormatException ignored) {
                            getLogger().warning("Ignoring invalid saved layout slot: " + source);
                        }
                    }
                    playerLayouts.put(kitName, mapping);
                }
                layouts.put(uuid, playerLayouts);
            } catch (IllegalArgumentException ignored) {
                getLogger().warning("Ignoring invalid player UUID: " + uuidText);
            }
        }
    }

    public void saveData() {
        kitsConfig = new YamlConfiguration();
        for (Kit kit : kits.values()) for (Map.Entry<Integer, ItemStack> item : kit.getContents().entrySet()) kitsConfig.set("kits." + kit.getName() + ".items." + item.getKey(), item.getValue());
        layoutsConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Map<Integer, Integer>>> player : layouts.entrySet()) for (Map.Entry<String, Map<Integer, Integer>> kit : player.getValue().entrySet()) for (Map.Entry<Integer, Integer> slot : kit.getValue().entrySet()) layoutsConfig.set("players." + player.getKey() + "." + kit.getKey() + "." + slot.getKey(), slot.getValue());
        try {
            kitsConfig.save(kitsFile);
            layoutsConfig.save(layoutsFile);
        } catch (Exception exception) {
            getLogger().severe("Could not save kit data: " + exception.getMessage());
        }
    }

    public void openKitMenu(Player player) {
        openKitMenu(player, 0);
    }

    public void openKitMenu(Player player, int requestedPage) {
        List<String> names = getKitNames();
        int pages = Math.max(1, (names.size() + 44) / 45);
        int page = Math.max(0, Math.min(requestedPage, pages - 1));
        Inventory inventory = Bukkit.createInventory(new KitMenuHolder(page), 54, Component.text("Kits - Page " + (page + 1) + "/" + pages).color(NamedTextColor.GOLD));
        int start = page * 45;
        for (int index = 0; index < 45 && start + index < names.size(); index++) {
            Kit kit = kits.get(names.get(start + index));
            ItemStack icon = kit.getContents().values().stream().findFirst().orElse(new ItemStack(Material.CHEST)).clone();
            icon.setAmount(1);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(kit.getName()).color(NamedTextColor.GREEN));
                icon.setItemMeta(meta);
            }
            inventory.setItem(index, icon);
        }
        if (page > 0) inventory.setItem(45, namedItem(Material.ARROW, "Previous page", NamedTextColor.YELLOW));
        if (page + 1 < pages) inventory.setItem(53, namedItem(Material.ARROW, "Next page", NamedTextColor.YELLOW));
        player.openInventory(inventory);
    }

    private ItemStack namedItem(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(color));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void selectKit(Player player, int page, int menuSlot) {
        List<String> names = getKitNames();
        int index = page * 45 + menuSlot;
        if (menuSlot < 0 || menuSlot >= 45 || index >= names.size()) return;
        String kitName = names.get(index);
        Kit kit = kits.get(kitName);
        selectedKits.put(player.getUniqueId(), kitName);
        player.closeInventory();
        player.getInventory().clear();
        Map<Integer, Integer> mapping = layouts.getOrDefault(player.getUniqueId(), Map.of()).get(kitName);
        for (Map.Entry<Integer, ItemStack> entry : kit.getContents().entrySet()) {
            int destination = mapping == null ? entry.getKey() : mapping.getOrDefault(entry.getKey(), entry.getKey());
            if (destination >= 0 && destination < player.getInventory().getSize()) player.getInventory().setItem(destination, entry.getValue().clone());
        }
        player.sendMessage(Component.text("Kit applied: " + kitName).color(NamedTextColor.GREEN));
    }

    public boolean createKit(String rawName, Player player) {
        String name = rawName.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
        if (name.isBlank() || kits.containsKey(name)) return false;
        Kit kit = new Kit(name);
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && !item.getType().isAir()) kit.getContents().put(slot, item.clone());
        }
        kits.put(name, kit);
        saveData();
        return true;
    }

    public boolean deleteKit(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        boolean removed = kits.remove(normalized) != null;
        if (removed) {
            layouts.values().forEach(playerLayouts -> playerLayouts.remove(normalized));
            selectedKits.values().removeIf(normalized::equals);
            saveData();
        }
        return removed;
    }

    public boolean saveLayout(Player player) {
        String kitName = selectedKits.get(player.getUniqueId());
        Kit kit = kitName == null ? null : kits.get(kitName);
        if (kit == null) return false;
        Map<Integer, Integer> mapping = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> source : kit.getContents().entrySet()) for (int destination = 0; destination < player.getInventory().getSize(); destination++) {
            ItemStack current = player.getInventory().getItem(destination);
            if (current != null && current.isSimilar(source.getValue()) && !mapping.containsValue(destination)) {
                mapping.put(source.getKey(), destination);
                break;
            }
        }
        layouts.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>()).put(kitName, mapping);
        saveData();
        return true;
    }

    public List<String> getKitNames() {
        List<String> names = new ArrayList<>(kits.keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }
}
