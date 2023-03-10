package net.gcnt.sellall.menus;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.ItemFile;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.YamlFile;
import net.gcnt.sellall.files.logs.PlayerLog;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MenuManager implements Listener {

    private final SellAll plugin;
    private final HashMap<String, ItemFile> itemFiles;
    private final HashMap<String, MenuFile> menuFiles;
    private final HashMap<UUID, ActiveMenuViewer> viewers;

    public MenuManager(SellAll plugin) {
        this.plugin = plugin;
        this.viewers = new HashMap<>();
        this.itemFiles = new HashMap<>();
        this.menuFiles = new HashMap<>();
    }

    public void openSellMenu(Player player, String menuName) {
        ActiveMenuViewer viewer = viewers.get(player.getUniqueId());
        if (viewer == null) {
            viewer = new ActiveMenuViewer(player);
            viewers.put(player.getUniqueId(), viewer);
        }
        // If the player is not viewing anything, or if they are viewing a different menu, initialize a new viewer
        if (viewer.getActiveMenu() == null || !viewer.getActiveMenu().getMenuId().equals(menuName) || viewer.isLookingAtItems()) {
            viewer.setActiveMenu(getNewSellMenu(menuName, viewer));
        }
        viewer.setLookingAtItems(false);
        viewer.getActiveMenu().openMenu(true, 1);
    }

    public void openItemsMenu(Player player, String menuName) {
        openItemsMenu(player, menuName, 1);
    }

    public void openItemsMenu(Player player, String menuName, int page) {
        ActiveMenuViewer viewer = viewers.get(player.getUniqueId());
        if (viewer == null) {
            viewer = new ActiveMenuViewer(player);
            viewers.put(player.getUniqueId(), viewer);
        }
        // If the player is not viewing anything, or if they are viewing a different menu, initialize a new viewer
        if (viewer.getActiveMenu() == null || !viewer.getActiveMenu().getMenuId().equals(menuName) || !viewer.isLookingAtItems()) {
            viewer.setActiveMenu(getNewItemListMenu(menuName, viewer));
        }
        viewer.setLookingAtItems(true);
        viewer.getActiveMenu().openMenu(page == 1, page);
    }

    public void load() {
        HashMap<String, MenuFile> menuFiles = loadFiles(new File(plugin.getDataFolder(), "menus"), MenuFile.class);
        HashMap<String, ItemFile> itemFiles = loadFiles(new File(plugin.getDataFolder(), "items"), ItemFile.class);

        this.menuFiles.clear();
        this.itemFiles.clear();
        menuFiles.forEach((name, file) -> {
            if (itemFiles.containsKey(name)) {
                plugin.getLogger().info("Found menu file with id " + name + ".");
                this.menuFiles.put(name, file);
                this.itemFiles.put(name, itemFiles.get(name));
            } else {
                plugin.getLogger().severe("SellAll failed to load the " + name + " menu because it does not have an associated items file.");
            }
        });

    }

    public <E extends YamlFile> HashMap<String, E> loadFiles(File folder, Class<E> clazz) {
        HashMap<String, E> files = new HashMap<>();
        if (!folder.exists() || !folder.isDirectory()) return files;


        for (File child : folder.listFiles()) {
            if (!child.getName().endsWith(".yml")) continue;

            try {
                final String id = child.getName().replace(".yml", "");
                E file = clazz.getConstructor(SellAll.class, String.class).newInstance(plugin, id);
                files.put(id, file);
            } catch (Exception ignored) {
            }
        }
        return files;
    }

    public SellMenu getNewSellMenu(String menuName, ActiveMenuViewer player) {
        if (!menuFiles.containsKey(menuName)) return null;
        return new SellMenu(plugin, this.menuFiles.get(menuName), player);
    }

    public ItemListMenu getNewItemListMenu(String menuName, ActiveMenuViewer player) {
        if (!menuFiles.containsKey(menuName)) return null;
        return new ItemListMenu(plugin, this.menuFiles.get(menuName), this.itemFiles.get(menuName), player);
    }

    public boolean menuExists(String menuName) {
        return menuFiles.containsKey(menuName);
    }

    public MenuFile getMenuFile(String menuName) {
        return menuFiles.get(menuName);
    }

    public ItemFile getItemFile(String menuName) {
        return itemFiles.get(menuName);
    }

    public void removeActiveMenuViewer(UUID uuid) {
        viewers.remove(uuid);
    }

    public ActiveMenuViewer getActiveMenuViewer(UUID uuid) {
        return viewers.get(uuid);
    }

    public List<String> getMenus() {
        return new ArrayList<>(menuFiles.keySet());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerLog.removePlayerLog(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        final ActiveMenuViewer viewer = getActiveMenuViewer(e.getPlayer().getUniqueId());
        if (viewer == null) return;
        if (viewer.isIgnoreClose()) return;

        removeActiveMenuViewer(e.getPlayer().getUniqueId());
        viewer.getActiveMenu().onClose(e);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        final ActiveMenuViewer viewer = getActiveMenuViewer(e.getWhoClicked().getUniqueId());
        if (viewer == null) return;

        if (e.getInventory() == null) return;
        e.setCancelled(true);

        viewer.getActiveMenu().onClick(e);
    }

}
