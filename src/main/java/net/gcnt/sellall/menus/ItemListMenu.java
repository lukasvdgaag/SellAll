package net.gcnt.sellall.menus;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.ItemFile;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.items.Item;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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

    private final ItemFile itemFile;

    public ItemListMenu(SellAll plugin, MenuFile menuFile, ItemFile itemFile, ActiveMenuViewer menuViewer) {
        super(plugin, menuFile, Bukkit.createInventory(null, 54, Utils.c(menuFile.getItemListMenuTitle())), menuViewer);
        this.itemFile = itemFile;
    }


    @Override
    public void openMenu(boolean openFirst, int page) {
        super.openMenu(openFirst, page);
        getPlayer().openInventory(this.inventory);
        loadItems();
    }

    public void loadItems() {
        this.inventory.clear();
        final ItemStack book = createItemStack(menuFile.getItemListMaterial(), Utils.c(menuFile.getItemListDisplayName()), Utils.c(menuFile.getItemListLore()));
        this.inventory.setItem(4, book);

        final ItemStack orangeFill = createItemStack(menuFile.getItemListFillMaterial(), "§r", new ArrayList<>());
        for (int slot : FILL_SLOTS) {
            this.inventory.setItem(slot, orangeFill);
        }

        List<Item> addedItems = itemFile.getItems();
        int start = addedItems.size() < 28 ? 0 : (page - 1) * 28;
        int end = start + 28;
        if (end > addedItems.size() - 1) {
            end = addedItems.size();
        }

        List<Item> items = addedItems.subList(start, end);

        if (page > 1) {
            // display previous page item
            final ItemStack item = createItemStack(menuFile.getPreviousPageMaterial(), Utils.c(menuFile.getPreviousPageDisplayName()), Utils.c(Utils.replace(menuFile.getPreviousPageLore(), "%page%", (page - 1) + "")));
            this.inventory.setItem(48, item);
        }
        if (addedItems.size() - 1 >= end) {
            // display next page item
            final ItemStack item = createItemStack(menuFile.getNextPageMaterial(), Utils.c(menuFile.getNextPageDisplayName()), Utils.c(Utils.replace(menuFile.getNextPageLore(), "%page%", (page + 1) + "")));
            this.inventory.setItem(50, item);
        }

        int current = 0;
        for (Item customItem : items) {
            final ItemStack item = customItem.getExample();
            if (item != null && item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(Utils.c(Utils.replace(menuFile.getItemListItemLore(), "%worth%", customItem.getSellWorth() + "")));
                item.setItemMeta(meta);
            }

            this.inventory.setItem(EMPTY_SLOTS.get(current), item);
            current++;
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory() == null) return;

        Player player = (Player) e.getWhoClicked();
        if (!e.getInventory().equals(e.getView().getTopInventory())) return;

        switch (e.getSlot()) {
            // back to main menu slot
            case 4 -> plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getMenuManager().openSellMenu(player, getMenuId()), 1);
            case 48 -> {
                // previous page slot
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    page = page - 1;
                    loadItems();
                    player.playSound(player.getLocation(), this.menuFile.getPreviousPageSound(), 1, 1);
                }
            }
            case 50 -> {
                // next page slot
                if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    page = page + 1;
                    loadItems();
                    player.playSound(player.getLocation(), this.menuFile.getNextPageSound(), 1, 1);
                }
            }
        }
    }

}
