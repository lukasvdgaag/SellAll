package net.gcnt.sellall.menus;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.items.Item;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SellMenu extends Menu implements Listener {

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

    private final HashMap<UUID, List<ItemStack>> playerSells;
    public final List<UUID> playerCancelClose;

    public SellMenu(SellAll plugin) {
        super(plugin);
        playerCancelClose = new ArrayList<>();
        playerSells = new HashMap<>();
    }

    @Override
    public void openMenu(Player player, boolean openFirst, int page) {
        super.openMenu(player, openFirst, page);
        MenuFile mf = plugin.getMenuFile();

        Inventory gui = Bukkit.createInventory(null, 54, Utils.c(mf.getSellMenuTitle()));

        player.openInventory(gui);
        playerCancelClose.remove(player.getUniqueId());
        loadItems(player, gui, mf);
    }

    public void loadItems(Player player, Inventory gui, MenuFile mf) {
        final ItemStack worthList = createItemStack(mf.getItemWorthOpenMaterial(), Utils.c(mf.getItemWorthOpenDisplayName()), Utils.c(mf.getItemWorthOpenLore()));
        gui.setItem(4, worthList);

        final ItemStack info = createItemStack(mf.getInfoMaterial(), Utils.c(mf.getInfoDisplayName()), Utils.c(mf.getInfoLore()));
        gui.setItem(53, info);

        final ItemStack glassFill = createItemStack(mf.getFillMaterial(), "§r", new ArrayList<>());
        for (int slot : GLASS_SLOTS) {
            gui.setItem(slot, glassFill);
        }

        // clearing the item slots if not present.
        int curSize = 0;
        final int size = playerSells.getOrDefault(player.getUniqueId(), new ArrayList<>()).size();
        for (int slot : ITEM_SELL_SLOTS) {
            if (curSize >= size) gui.setItem(slot, null);
            curSize++;
        }

        final ItemStack desc = createItemStack(mf.getNoItemAddedMaterial(), Utils.c(mf.getNoItemAddedDisplayName()), Utils.c(mf.getNoItemAddedLore()));
        for (int slot : ITEM_DESCRIPTION_SLOTS) {
            gui.setItem(slot, desc);
        }

        final ItemStack cancel = createItemStack(mf.getCancelMaterial(), Utils.c(mf.getCancelDisplayName()), Utils.c(mf.getCancelLore()));
        gui.setItem(9, cancel);

        List<ItemStack> currentAdded = playerSells.getOrDefault(player.getUniqueId(), Lists.newArrayList());

        int current = 0;
        for (ItemStack cur : currentAdded) {
            if (current == 13) {
                break;
            }
            gui.setItem(ITEM_SELL_SLOTS.get(current), cur);

            double worth = 0;
            Item ci = Item.fromMaterial(plugin.getItemFile(), cur);

            if (ci != null) {
                worth = ci.getSellWorth() * cur.getAmount();
            }
            ItemStack filled = createItemStack(mf.getItemAddedMaterial(), Utils.c(mf.getItemAddedDisplayName()), Utils.c(Utils.replace(mf.getItemAddedLore(), "%worth%", worth + "")));
            gui.setItem(ITEM_DESCRIPTION_SLOTS.get(current), filled);

            current++;
        }

        double totalWorth = 0;
        double taxAmount;
        double taxPercentage = plugin.getUtils().getTax(player);
        for (ItemStack item : currentAdded) {
            Item ci = Item.fromMaterial(plugin.getItemFile(), item);
            if (ci != null) {
                totalWorth += (ci.getSellWorth() * item.getAmount());
            }
        }
        taxAmount = (totalWorth * (taxPercentage / 100));

        final ItemStack item = createItemStack(mf.getProceedMaterial(), Utils.c(mf.getProceedDisplayName()), Utils.c(Utils.replace(Utils.replace(Utils.replace(Utils.replace(mf.getProceedLore(), "%net%", String.format("%.2f", (totalWorth - taxAmount))), "%tax_percentage%", taxPercentage + ""), "%tax_amount%", String.format("%.2f", taxAmount)), "%totalworth%", String.format("%.2f", totalWorth))));
        gui.setItem(49, item);

        playerSells.put(player.getUniqueId(), currentAdded);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null) return;
        if (!playerSells.containsKey(player.getUniqueId())) return;
        // player is in the menu
        e.setCancelled(true);
        if (playerCancelClose.contains(player.getUniqueId())) return;

        if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
            // click top inventory
            switch (e.getSlot()) {
                case 4 -> {
                    playerCancelClose.add(player.getUniqueId());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        plugin.getItemListMenu().openMenu(player, true, 1);
                        playerCancelClose.remove(player.getUniqueId());
                    }, 2);

                }
                case 9 -> {
                    final Location location = player.getLocation().getBlock().getLocation();
                    player.getInventory().addItem(playerSells.get(player.getUniqueId()).toArray(new ItemStack[]{}))
                            .forEach((integer, itemStack) ->
                                    player.getWorld().dropItem(location.add(0.5, 0.25, 0.5), itemStack)
                            );
                    playerSells.remove(player.getUniqueId());
                    player.closeInventory();
                }
                case 49 -> {
                    if (playerSells.get(player.getUniqueId()).size() > 0) {
                        sellItems(player);
                    } else {
                        player.playSound(player.getLocation(), plugin.getMenuFile().getInvalidItemSound(), 1, 1);
                    }
                }
                default -> {
                    if (ITEM_SELL_SLOTS.contains(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        removeSellItems(player, e.isShiftClick(), e.getCurrentItem());
                        loadItems(player, e.getView().getTopInventory(), plugin.getMenuFile());
                    }
                }
            }

        } else {
            // clicked bottom inventory
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            List<ItemStack> items = playerSells.get(player.getUniqueId());

            Item item = Item.fromMaterial(plugin.getItemFile(), e.getCurrentItem());
            if (item == null) {
                player.playSound(player.getLocation(), plugin.getMenuFile().getInvalidItemSound(), 1, 1);
                return;
            }

            if (items.size() < 14) {
                addSellItem(e.isShiftClick(), e.getCurrentItem(), items, e);

                playerSells.put(player.getUniqueId(), items);
                playerCancelClose.add(player.getUniqueId());
                player.playSound(player.getLocation(), plugin.getMenuFile().getItemAddSound(), 1, 1);
                loadItems(player, e.getView().getTopInventory(), plugin.getMenuFile());
                playerCancelClose.remove(player.getUniqueId());
            }
        }
    }

    private void sellItems(Player player) {
        double totalWorth = 0;
        double taxAmount;
        double taxPercentage = plugin.getUtils().getTax(player);

        List<ItemStack> sold = Lists.newArrayList();

        for (ItemStack item : playerSells.get(player.getUniqueId())) {
            Item ci = Item.fromMaterial(plugin.getItemFile(), item);
            if (ci != null) {
                totalWorth += (ci.getSellWorth() * item.getAmount());
                sold.add(item);
            }
        }

        taxAmount = totalWorth * (taxPercentage / 100);
        double earned = totalWorth - taxAmount;
        final double tax = (int) (taxAmount * 100) / 100d;
        plugin.getLog().log(player, sold, tax);
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), earned);
        playerCancelClose.add(player.getUniqueId());
        player.closeInventory();
        playerCancelClose.remove(player.getUniqueId());
        playerSells.remove(player.getUniqueId());

        player.playSound(player.getLocation(), plugin.getMenuFile().getSellSound(), 1, 1);
        for (String s :
                Utils.c(Utils.replace(Utils.replace(Utils.replace(Utils.replace(Utils.replace(plugin.getMenuFile().getSellMessage(), "%net%", String.format("%.2f", earned)), "%tax_amount%", tax + ""), "%tax_percentage%", taxPercentage + ""), "%totalworth%", String.format("%.2f", totalWorth) + ""), "%player%", player.getName()))) {
            player.sendMessage(s);
        }
    }

    private void removeSellItems(Player player, boolean shiftClick, ItemStack currentItem) {
        List<ItemStack> iis = playerSells.get(player.getUniqueId());
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

        playerSells.put(player.getUniqueId(), iis);
        player.playSound(player.getLocation(), plugin.getMenuFile().getItemRemoveSound(), 1, 1);
    }

    private void addSellItem(boolean shiftClick, ItemStack currentItem, List<ItemStack> items, InventoryClickEvent e) {
        int amount = currentItem.getAmount();
        boolean yes = false;
        if (!shiftClick) {
            // only add one

            for (ItemStack it : items) {
                if (it.getType() == currentItem.getType() && it.getAmount() < it.getMaxStackSize()) {
                    int index = items.indexOf(it);
                    ItemStack i = items.get(index);
                    i.setAmount(i.getAmount() + 1);
                    items.set(index, i);
                    yes = true;
                    break;
                }
            }

            if (amount == 1) {
                if (!yes) {
                    items.add(currentItem);
                }
                e.setCurrentItem(null);
            } else {
                currentItem.setAmount(amount - 1);
                e.setCurrentItem(currentItem);
                currentItem.setAmount(1);
            }

            if (!yes) {
                items.add(currentItem);
            }

        } else {
            for (ItemStack it : items) {
                if (it.getType() == currentItem.getType() && it.getAmount() < it.getMaxStackSize()) {
                    int index = items.indexOf(it);
                    ItemStack i = items.get(index);
                    if (i.getAmount() + amount <= it.getMaxStackSize()) {
                        i.setAmount(i.getAmount() + amount);
                        amount = 0;
                        items.set(index, i);
                        yes = true;
                        break;
                    } else {
                        int min = i.getMaxStackSize() - i.getAmount();
                        i.setAmount(i.getMaxStackSize());
                        items.set(index, i);
                        amount = amount - min;
                    }
                    break;
                }
            }

            if (!yes) {
                currentItem.setAmount(amount);
                items.add(currentItem);
            }
            e.setCurrentItem(null);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (e.getView().getTitle().equals(Utils.c(plugin.getMenuFile().getSellMenuTitle())) && !playerCancelClose.contains(player.getUniqueId())) {
            if (!playerSells.containsKey(player.getUniqueId())) return;

            for (ItemStack item : playerSells.get(player.getUniqueId())) {
                player.getInventory().addItem(item);
            }
            playerCancelClose.remove(player.getUniqueId());

            if (playerSells.get(player.getUniqueId()).size() > 0) {
                List<String> cm = plugin.getMenuFile().getCancelMessage();
                for (String s : Utils.c(cm)) {
                    player.sendMessage(s);
                }
            }
            playerSells.remove(player.getUniqueId());
            player.playSound(player.getLocation(), plugin.getMenuFile().getCancelSound(), 1, 1);
        }
    }
}
