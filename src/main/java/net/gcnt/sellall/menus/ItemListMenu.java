package net.gcnt.sellall.menus;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.itemworths.Item;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemListMenu extends Menu {

    private static final List<Integer> FILL_SLOTS = Lists.newArrayList(
            0, 1, 2, 3, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53
    );
    private static final List<Integer> EMPTY_SLOTS = Lists.newArrayList(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );
    private static final HashMap<Player, Integer> IN_MENUS = new HashMap<>();

    public ItemListMenu(SellAll plugin) {
        super(plugin);
    }

    @Override
    public void openMenu(Player player, boolean openFirst, int page) {
        super.openMenu(player, openFirst, page);

        MenuFile mf = plugin.getMenuFile();
        Inventory gui = Bukkit.createInventory(null, 54, Utils.c(mf.getItemListMenuTitle()));

        final ItemStack book = createItemStack(mf.getItemListMaterial(), Utils.c(mf.getItemListDisplayName()), Utils.c(mf.getItemListLore()));
        gui.setItem(4, book);

        final ItemStack orangeFill = createItemStack(mf.getItemListFillMaterial(), "§r", new ArrayList<>());
        for (int slot : FILL_SLOTS) {
            gui.setItem(slot, orangeFill);
        }

        List<Item> addedItems = plugin.getItemFile().getItems();
        int start = addedItems.size() < 28 ? 0 : (page - 1) * 28;
        int end = start + 28;
        if (end > addedItems.size() - 1) {
            end = addedItems.size();
        }

        List<Item> items = addedItems.subList(start, end);

        if (page > 1) {
            // display previous page item
            final ItemStack item = createItemStack(mf.getPreviousPageMaterial(), Utils.c(mf.getPreviousPageDisplayName()), Utils.c(Utils.replace(mf.getPreviousPageLore(), "%page%", (page - 1) + "")));
            gui.setItem(48, item);
        }
        if (addedItems.size() - 1 >= end) {
            // display next page item
            final ItemStack item = createItemStack(mf.getNextPageMaterial(), Utils.c(mf.getNextPageDisplayName()), Utils.c(Utils.replace(mf.getNextPageLore(), "%page%", (page + 1) + "")));
            gui.setItem(50, item);
        }


        int current = 0;
        for (Item customItem : items) {
            final ItemStack item = createItemStack(customItem.getItem(), null, Utils.c(Utils.replace(mf.getItemListItemLore(), "%worth%", customItem.getSellWorth() + "")));
            gui.setItem(EMPTY_SLOTS.get(current), item);
            current++;
        }

        player.openInventory(gui);
        IN_MENUS.put(player, page);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        Player player = (Player) e.getWhoClicked();
        if (!IN_MENUS.containsKey(player)) return;

        e.setCancelled(true);

        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        switch (e.getSlot()) {
            case 4 -> new SellMenu(this.plugin).openMenu(player, true, 1);
            case 48 -> {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == plugin.getMenuFile().getPreviousPageMaterial()) {
                    openMenu(player, false, IN_MENUS.get(player) - 1);
                    player.playSound(player.getLocation(), plugin.getMenuFile().getPreviousPageSound(), 1, 1);
                }
            }
            case 50 -> {
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() == plugin.getMenuFile().getNextPageMaterial()) {
                    openMenu(player, false, IN_MENUS.get(player) + 1);
                    player.playSound(player.getLocation(), plugin.getMenuFile().getNextPageSound(), 1, 1);
                }
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        IN_MENUS.remove((Player) e.getPlayer());
    }

}
