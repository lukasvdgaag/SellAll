package net.gcnt.sellall.menus;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.logs.PlayerLog;
import net.gcnt.sellall.items.Item;
import net.gcnt.sellall.items.ItemType;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SellMenu extends Menu {

    private static final List<Integer> GLASS_SLOTS = Lists.newArrayList(
            0, 1, 2, 3, 5, 6, 7, 8,
            17,
            18, 26,
            27, 35,
            36, 44,
            45, 46, 47, 48, 50, 51, 52
    );
    private static final List<Integer> ITEM_SELL_SLOTS = Lists.newArrayList(
            10, 11, 12, 13, 14, 15, 16,
            28, 29, 30, 31, 32, 33, 34
    );
    private static final List<Integer> ITEM_DESCRIPTION_SLOTS = Lists.newArrayList(
            19, 20, 21, 22, 23, 24, 25,
            37, 38, 39, 40, 41, 42, 43
    );

    public SellMenu(SellAll plugin, MenuFile menuFile, ActiveMenuViewer menuViewer) {
        super(plugin, menuFile, Bukkit.createInventory(null, 54, Utils.c(menuFile.getSellMenuTitle())), menuViewer);
    }

    @Override
    public void openMenu(boolean openFirst, int page) {
        super.openMenu(openFirst, page);

        getPlayer().openInventory(this.inventory);
        loadInventoryItems();
        // loading player sells
        PlayerLog.getPlayerLog(plugin, getPlayer().getUniqueId());
    }

    /**
     * Loads the inventory items and updates existing ones.
     * Used to asynchronously load the inventory items.
     */
    public void loadInventoryItems() {
        PlayerLog log = PlayerLog.getPlayerLog(plugin, getPlayer().getUniqueId());

        final ItemStack worthList = createItemStack(this.menuFile.getItemWorthOpenMaterial(), Utils.c(this.menuFile.getItemWorthOpenDisplayName()), Utils.c(this.menuFile.getItemWorthOpenLore()));
        this.inventory.setItem(4, worthList);

        final ItemStack info = createItemStack(this.menuFile.getInfoMaterial(), Utils.c(this.menuFile.getInfoDisplayName()), Utils.c(this.menuFile.getInfoLore()));
        this.inventory.setItem(53, info);

        final ItemStack glassFill = createItemStack(this.menuFile.getFillMaterial(), "§r", new ArrayList<>());
        for (int slot : GLASS_SLOTS) {
            this.inventory.setItem(slot, glassFill);
        }

        final HashMap<Item, Integer> soldItems = new HashMap<>();

        // clearing the item slots if not present.
        List<ItemStack> sellingItems = menuViewer.getSellingItems();

        int curSize = 0;
        final int size = sellingItems.size();
        for (int slot : ITEM_SELL_SLOTS) {
            if (curSize >= size) this.inventory.setItem(slot, null);
            curSize++;
        }

        final ItemStack desc = createItemStack(this.menuFile.getNoItemAddedMaterial(), Utils.c(this.menuFile.getNoItemAddedDisplayName()), Utils.c(this.menuFile.getNoItemAddedLore()));
        for (int slot : ITEM_DESCRIPTION_SLOTS) {
            this.inventory.setItem(slot, desc);
        }

        final ItemStack cancel = createItemStack(this.menuFile.getCancelMaterial(), Utils.c(this.menuFile.getCancelDisplayName()), Utils.c(this.menuFile.getCancelLore()));
        this.inventory.setItem(9, cancel);

        double totalWorth = 0;

        int current = 0;
        for (ItemStack cur : sellingItems) {
            if (current == 13) {
                break;
            }
            Item ci = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), cur);
            if (ci == null) continue; // highly unlikely

            this.inventory.setItem(ITEM_SELL_SLOTS.get(current), cur);

            final Integer currentSells = soldItems.getOrDefault(ci, log.getSellCount(ci));
            final double worth = getWorth(ci, cur.getAmount(), currentSells);

            soldItems.put(ci, currentSells + cur.getAmount());

            ItemStack filled = createItemStack(
                    this.menuFile.getItemAddedMaterial(),
                    Utils.c(this.menuFile.getItemAddedDisplayName()),
                    Utils.c(Utils.replace(
                            this.menuFile.getItemAddedLore(),
                            "%worth%",
                            String.format("%.2f", worth))
                    )
            );
            this.inventory.setItem(ITEM_DESCRIPTION_SLOTS.get(current), filled);

            totalWorth += worth;
            current++;
        }

        double taxAmount;
        double taxPercentage = plugin.getUtils().getTax(getPlayer());
        taxAmount = (totalWorth * (taxPercentage / 100));

        final HashMap<String, String> replacements = createHashMap(new String[]{
                "%net%", "%tax_percentage%", "%tax_amount%", "%totalworth%"
        }, new String[]{
                String.format("%.2f", (totalWorth - taxAmount)),
                taxPercentage + "",
                String.format("%.2f", taxAmount),
                String.format("%.2f", totalWorth)
        });

        final ItemStack item = createItemStack(this.menuFile.getProceedMaterial(),
                menuFile.getProceedDisplayName(),
                menuFile.getProceedLore(),
                replacements, true);
        this.inventory.setItem(49, item);
    }

    private void handleTopInventory(InventoryClickEvent e) {
        Player player = menuViewer.getPlayer();
        switch (e.getSlot()) {
            // clicking the worth list
            case 4 -> {
                menuViewer.setIgnoreClose(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getMenuManager().openItemsMenu(player, getMenuId());
                    menuViewer.setIgnoreClose(false);
                }, 1);
            }
            // clicking the cancel button
            case 9 -> {
                final Location location = player.getLocation().getBlock().getLocation();
                player.getInventory().addItem(menuViewer.getSellingItems().toArray(new ItemStack[]{}))
                        .forEach((integer, itemStack) ->
                                player.getWorld().dropItem(location.add(0.5, 0.25, 0.5), itemStack)
                        );
                plugin.getMenuManager().removeActiveMenuViewer(player.getUniqueId());
                player.closeInventory();
            }
            // clicking the submit button
            case 49 -> {
                if (menuViewer.getSellingItems().size() > 0) {
                    sellItems();
                } else {
                    player.playSound(player.getLocation(), this.menuFile.getInvalidItemSound(), 1, 1);
                }
            }
            // clicking anything else, checking if the slot belongs to an item that is being sold
            default -> {
                if (ITEM_SELL_SLOTS.contains(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    removeSellItems(e.isShiftClick(), e.getCurrentItem());
                    loadInventoryItems();
                }
            }
        }
    }

    private void handleBottomInventory(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Item item = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), e.getCurrentItem());
        if (item == null) {
            getPlayer().playSound(getPlayer().getLocation(), this.menuFile.getInvalidItemSound(), 1, 1);
            return;
        }

        if (menuViewer.getSellingItems().size() < ITEM_SELL_SLOTS.size()) {
            addSellItem(e.isShiftClick(), e.getCurrentItem(), menuViewer.getSellingItems(), e);
            loadInventoryItems();
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getRawSlot() < e.getView().getTopInventory().getSize()) {
            // click top inventory
            handleTopInventory(e);
        } else {
            // clicked bottom inventory
            handleBottomInventory(e);
        }
    }

    private void sellItems() {
        double totalWorth = 0;
        double taxAmount;
        final Player player = getPlayer();
        double taxPercentage = plugin.getUtils().getTax(player);

        List<ItemStack> sold = Lists.newArrayList();

        PlayerLog log = PlayerLog.getPlayerLog(plugin, player.getUniqueId());
        final HashMap<Item, Integer> soldItems = new HashMap<>();

        for (ItemStack item : menuViewer.getSellingItems()) {
            Item ci = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), item);
            if (ci != null) {
                final Integer currentSells = soldItems.getOrDefault(ci, log.getSellCount(ci));
                final double itemWorth = getWorth(ci, item.getAmount(), currentSells);
                soldItems.put(ci, currentSells + item.getAmount());

                totalWorth += itemWorth;
                sold.add(item);

                String material = ci.getType() == ItemType.BUKKIT ? item.getType().toString() : ci.getExternalId();

                plugin.getMySQLLog().logSell(log, ci.getType(), getMenuId(), material, item.getAmount(), ci.getSellWorth(), taxPercentage);
                log.setSellCount(ci, log.getSellCount(ci) + item.getAmount());
            }
        }

        taxAmount = totalWorth * (taxPercentage / 100);
        double earned = totalWorth - taxAmount;
        final double tax = (int) (taxAmount * 100) / 100d;

        plugin.getLog().log(player, sold, tax);
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), earned);

        plugin.getMenuManager().removeActiveMenuViewer(player.getUniqueId());
        player.closeInventory();

        player.playSound(player.getLocation(), this.menuFile.getSellSound(), 1, 1);
        for (String s : menuFile.getSellMessage()) {
            s = s.replace("%net%", String.format("%.2f", earned))
                    .replace("%tax_amount%", tax + "")
                    .replace("%tax_percentage%", taxPercentage + "")
                    .replace("%totalworth%", String.format("%.2f", totalWorth) + "")
                    .replace("%player%", player.getName());
            player.sendMessage(Utils.c(s));
        }
    }

    private void removeSellItems(boolean shiftClick, ItemStack currentItem) {
        final Player player = getPlayer();
        List<ItemStack> iis = menuViewer.getSellingItems();
        int index = iis.indexOf(currentItem);

        if (!shiftClick) {
            // only add one
            int amount = currentItem.getAmount();
            if (amount == 1) {
                iis.remove(index);
                player.getInventory().addItem(currentItem);
            } else {
                currentItem.setAmount(amount - 1);
                iis.set(index, currentItem);
                ItemStack it = currentItem.clone();
                it.setAmount(1);
                player.getInventory().addItem(it);
            }
        } else {
            iis.remove(index);
            player.getInventory().addItem(currentItem);
        }

        player.playSound(player.getLocation(), this.menuFile.getItemRemoveSound(), 1, 1);
    }

    private int getCurrentSellingItemAmount(Item item) {
        int amount = 0;
        for (ItemStack itemStack : menuViewer.getSellingItems()) {
            Item ci = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), itemStack);
            if (ci == null || !ci.getId().equals(item.getId())) continue;

            amount += itemStack.getAmount();
        }
        return amount;
    }

    private void addItem(Item current, ItemStack currentItem, int amount, List<ItemStack> items, InventoryClickEvent e) {
        int amountLeft = currentItem.getAmount() - amount;

        boolean yes = false;
        for (ItemStack it : items) {
            Item item = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), it);
            if (item != null && item.equals(current) && it.getAmount() < it.getMaxStackSize()) {
                if (it.getAmount() + amount <= it.getMaxStackSize()) {
                    it.setAmount(it.getAmount() + amount);
                    yes = true;
                    break;
                } else {
                    int min = it.getMaxStackSize() - it.getAmount();
                    it.setAmount(it.getMaxStackSize());
                    amount = amount - min;
                }
            }
        }

        if (!yes) {
            final ItemStack currentItem1 = currentItem.clone();
            currentItem1.setAmount(amount);
            items.add(currentItem1);
        }
        currentItem.setAmount(amountLeft);

        if (currentItem.getAmount() == 0) {
            e.setCurrentItem(null);
        }
    }

    private int getTierIndex(int amountSold) {
        int tierIndex = 0;
        final Integer[] tiers = plugin.getCfg().getWorthTiers().keySet().toArray(new Integer[0]);
        for (int i = 0; i < tiers.length; i++) {
            if (amountSold >= tiers[i]) {
                tierIndex = i;
            }
        }
        return tierIndex;
    }

    private double getWorth(Item item, int amount, int amountSold) {
        double worth = 0;

        int tierIndex = getTierIndex(amountSold);
        final Integer[] tiers = plugin.getCfg().getWorthTiers().keySet().toArray(new Integer[0]);

        for (int i = tierIndex; i < tiers.length; i++) {
            if (amount <= 0) break;

            int currentTier = tiers[i];
            int nextTier = i >= tiers.length - 1 ? currentTier : tiers[i + 1];
            double tierWorth = plugin.getCfg().getWorthTiers().get(currentTier);

            final int total = amountSold + amount;
            if (i == nextTier || total <= nextTier) {
                // reached the end of the tiers
                worth += amount * item.getSellWorth() * tierWorth;
                break;
            } else {
                // amount sold + amount is greater than the next tier
                int toSell = nextTier - amountSold;

                worth += toSell * item.getSellWorth() * tierWorth;
                amount -= toSell;
                amountSold += toSell;
            }
        }

        return worth;
    }

    private void addSellItem(boolean shiftClick, ItemStack currentItem, List<ItemStack> items, InventoryClickEvent e) {
        Item current = Item.fromMaterial(plugin.getMenuManager().getItemFile(getMenuId()), currentItem);

        if (current == null) return;
        int selling = shiftClick ? currentItem.getAmount() : 1;

        final Player player = getPlayer();
        PlayerLog log = PlayerLog.getPlayerLog(plugin, player.getUniqueId());
        if (current.getMaxDailySells() > -1) {
            int sellsLeft = log.getSellsLeft(current);
            int alreadyAdded = getCurrentSellingItemAmount(current);

            if (sellsLeft == -1) {
                sellsLeft = currentItem.getAmount();
                alreadyAdded = 0;
            }

            int maxToSell = Math.max(0, sellsLeft - alreadyAdded);

            final int min = Math.min(maxToSell, selling);

            if (min == 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            selling = min;
        }

        player.playSound(player.getLocation(), this.menuFile.getItemAddSound(), 1, 1);
        addItem(current, currentItem, selling, items, e);
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        returnAddedItems();

        final Player player = getPlayer();
        if (menuViewer.getSellingItems().size() > 0) {
            List<String> cm = this.menuFile.getCancelMessage();
            for (String s : Utils.c(cm)) {
                player.sendMessage(s);
            }
        }
        player.playSound(player.getLocation(), this.menuFile.getCancelSound(), 1, 1);
    }

    private void returnAddedItems() {
        final Inventory playerInv = getPlayer().getInventory();
        for (ItemStack item : menuViewer.getSellingItems()) {
            playerInv.addItem(item);
        }
    }

}
