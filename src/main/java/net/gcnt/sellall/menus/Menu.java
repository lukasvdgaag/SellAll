package net.gcnt.sellall.menus;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Menu {

    protected final SellAll plugin;
    protected final MenuFile menuFile;
    protected final Inventory inventory;
    protected final ActiveMenuViewer menuViewer;
    protected int page;

    public Menu(SellAll plugin, MenuFile menuFile, Inventory inventory, ActiveMenuViewer menuViewer) {
        this.plugin = plugin;
        this.menuFile = menuFile;
        this.inventory = inventory;
        this.menuViewer = menuViewer;
        this.page = 1;
    }

    public void openMenu(boolean openFirst, int page) {
        this.page = page;
        if (openFirst) {
            menuViewer.getPlayer().playSound(menuViewer.getPlayer().getLocation(), menuFile.getMenuOpenSound(), 1, 1);
        }
    }

    protected ItemStack createItemStack(Material material, String displayName, List<String> lore) {
        return createItemStack(material, displayName, lore, null, true);
    }
    protected ItemStack createItemStack(Material material, String displayName, List<String> lore, HashMap<String, String> placeholders, boolean colorize) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) meta.setDisplayName(colorize ? Utils.c(displayName) : displayName);

            List<String> newLore = new ArrayList<>(lore);
            if (placeholders == null || placeholders.isEmpty()) {
                newLore.replaceAll(s -> colorize ? Utils.c(s) : s);
            } else {
                for (String placeholder : placeholders.keySet()) {
                    newLore.replaceAll(s -> colorize ? Utils.c(s.replace(placeholder, placeholders.get(placeholder))) : s.replace(placeholder, placeholders.get(placeholder)));
                }
            }
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }
        return item;
    }
    protected <K,V> HashMap<K,V> createHashMap(K[] key, V[] value) {
        HashMap<K,V> map = new HashMap<>();
        for (int i = 0; i < key.length; i++) {
            map.put(key[i], value[i]);
        }
        return map;
    }

    public void onClick(InventoryClickEvent e) {};

    public void onClose(InventoryCloseEvent e) {};

    public String getMenuId() {
        return menuFile.getMenuId();
    }

    public Player getPlayer() {
        return menuViewer.getPlayer();
    }

    public Inventory getInventory() {
        return inventory;
    }
}
