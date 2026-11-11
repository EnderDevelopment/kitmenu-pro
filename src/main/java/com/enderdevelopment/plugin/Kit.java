package com.enderdevelopment.plugin;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public final class Kit {
    private final String name;
    private final Map<Integer, ItemStack> contents = new HashMap<>();

    public Kit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, ItemStack> getContents() {
        return contents;
    }
}
