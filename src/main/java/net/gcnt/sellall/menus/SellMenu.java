package net.gcnt.sellall.menus;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.itemworths.Item;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SellMenu extends Menu {

    private static final List<Integer> ORANGE_GLASS_SLOTS = Lists.newArrayList(
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
    private static final HashMap<Player, List<ItemStack>> PLAYER_SELLS = new HashMap<>();
    private static final HashMap<Player, Boolean> PLAYER_CANCEL_CLOSE = new HashMap<>();

    public SellMenu(SellAll plugin) {
        super(plugin);
    }

    @Override
    public void openMenu(Player player, boolean openFirst, int page) {
        super.openMenu(player, openFirst, page);
        MenuFile mf = plugin.getMenuFile();

        Inventory gui = Bukkit.createInventory(null, 54, Utils.c(mf.getSellMenuTitle()));

        final ItemStack worthList = createItemStack(mf.getItemWorthOpenMaterial(), Utils.c(mf.getItemWorthOpenDisplayName()), Utils.c(mf.getItemWorthOpenLore()));
        gui.setItem(4, worthList);

        final ItemStack info = createItemStack(mf.getInfoMaterial(), Utils.c(mf.getInfoDisplayName()), Utils.c(mf.getInfoLore()));
        gui.setItem(53, info);

        final ItemStack orangeFill = createItemStack(mf.getFillMaterial(), "§r", new ArrayList<>());
        for (int slot : ORANGE_GLASS_SLOTS) {
            gui.setItem(slot, orangeFill);
        }

        final ItemStack desc = createItemStack(mf.getNoItemAddedMaterial(), Utils.c(mf.getNoItemAddedDisplayName()), Utils.c(mf.getNoItemAddedLore()));
        for (int slot : ITEM_DESCRIPTION_SLOTS) {
            gui.setItem(slot, desc);
        }

        final ItemStack cancel = createItemStack(mf.getCancelMaterial(), Utils.c(mf.getCancelDisplayName()), Utils.c(mf.getCancelLore()));
        gui.setItem(9, cancel);

        List<ItemStack> currentAdded = PLAYER_SELLS.getOrDefault(player, Lists.newArrayList());

        int current = 0;
        for (ItemStack cur : currentAdded) {
            if (current == 13) {
                break;
            }
            gui.setItem(ITEM_SELL_SLOTS.get(current), cur);

            double worth = 0;
            Item ci = Item.fromMaterial(plugin.getItemFile(), cur.getType());
            if (ci != null) {
                worth = ci.getSellWorth() * cur.getAmount();
            }

            final ItemStack filled = createItemStack(mf.getItemAddedMaterial(), Utils.c(mf.getItemAddedDisplayName()), Utils.c(Utils.replace(mf.getItemAddedLore(), "%worth%", worth + "")));
            gui.setItem(ITEM_DESCRIPTION_SLOTS.get(current), filled);

            current++;
        }

        double totalWorth = 0;
        double taxAmount;
        double taxPercentage = plugin.getUtils().getTax(player);
        for (ItemStack item : currentAdded) {
            Item ci = Item.fromMaterial(plugin.getItemFile(), item.getType());
            if (ci != null) {
                totalWorth += (ci.getSellWorth() * item.getAmount());
            }
        }
        taxAmount = (totalWorth * (taxPercentage / 100));

        final ItemStack item = createItemStack(mf.getProceedMaterial(), Utils.c(mf.getProceedDisplayName()), Utils.c(Utils.replace(Utils.replace(Utils.replace(Utils.replace(mf.getProceedLore(), "%net%", String.format("%.2f", (totalWorth - taxAmount))), "%tax_percentage%", taxPercentage + ""), "%tax_amount%", String.format("%.2f", taxAmount)), "%totalworth%", String.format("%.2f", totalWorth))));
        gui.setItem(49, item);

        player.openInventory(gui);
        PLAYER_SELLS.put(player, currentAdded);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null) return;
        if (!PLAYER_SELLS.containsKey(player)) return;
        // player is in the menu
        e.setCancelled(true);

        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            // click top inventory
            switch (e.getSlot()) {
                case 4 -> {
                    PLAYER_CANCEL_CLOSE.put(player, true);
                    for (ItemStack item : PLAYER_SELLS.get(player)) {
                        player.getInventory().addItem(item);
                    }
                    PLAYER_SELLS.remove(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getItemListMenu().openMenu(player, true, 1), 2);

                    PLAYER_CANCEL_CLOSE.remove(player);
                }
                case 9 -> player.closeInventory();
                case 49 -> {
                    if (PLAYER_SELLS.get(player).size() > 0) {
                        double totalWorth = 0;
                        double taxAmount;
                        double taxPercentage = plugin.getUtils().getTax(player);

                        List<ItemStack> sold = Lists.newArrayList();

                        for (ItemStack item : PLAYER_SELLS.get(player)) {
                            Item ci = Item.fromMaterial(plugin.getItemFile(), item.getType());
                            if (ci != null) {
                                totalWorth += (ci.getSellWorth() * item.getAmount());
                                sold.add(item);
                            }
                        }

                        taxAmount = totalWorth * (taxPercentage / 100);
                        double earned = totalWorth - taxAmount;

                        plugin.getLog().log(player, sold, Double.parseDouble(String.format("%.2f", taxAmount)));
                        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), earned);
                        PLAYER_CANCEL_CLOSE.put(player, true);
                        player.closeInventory();
                        PLAYER_CANCEL_CLOSE.remove(player);
                        PLAYER_SELLS.remove(player);

                        player.playSound(player.getLocation(), plugin.getMenuFile().getSellSound(), 1, 1);
                        for (String s :
                                Utils.c(Utils.replace(Utils.replace(Utils.replace(Utils.replace(Utils.replace(plugin.getMenuFile().getSellMessage(), "%net%", String.format("%.2f", earned)), "%tax_amount%", String.format("%.2f", taxAmount)), "%tax_percentage%", taxPercentage + ""), "%totalworth%", String.format("%.2f", totalWorth) + ""), "%player%", player.getName()))) {
                            player.sendMessage(s);
                        }
                    } else {
                        player.playSound(player.getLocation(), plugin.getMenuFile().getInvalidItemSound(), 1, 1);
                    }
                }
                default -> {
                    if (ITEM_SELL_SLOTS.contains(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        List<ItemStack> iis = PLAYER_SELLS.get(player);
                        int index = iis.indexOf(e.getCurrentItem());

                        if (!e.isShiftClick()) {
                            // only add one
                            ItemStack current = e.getCurrentItem();
                            int amount = current.getAmount();
                            if (amount == 1) {
                                iis.remove(index);
                                player.getInventory().addItem(current);
                            } else {
                                current.setAmount(amount - 1);
                                iis.set(index, current);
                                ItemStack it = current.clone();
                                it.setAmount(1);
                                player.getInventory().addItem(it);
                            }
                        } else {
                            iis.remove(index);
                            player.getInventory().addItem(e.getCurrentItem());
                        }

                        PLAYER_SELLS.put(player, iis);
                        player.playSound(player.getLocation(), plugin.getMenuFile().getItemRemoveSound(), 1, 1);
                        PLAYER_CANCEL_CLOSE.put(player, true);
                        openMenu(player, false, 1);
                        PLAYER_CANCEL_CLOSE.remove(player);
                    }
                }
            }

        } else {
            // clicked bottom inventory
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            List<ItemStack> items = PLAYER_SELLS.get(player);

            Item item = Item.fromMaterial(plugin.getItemFile(), e.getCurrentItem().getType());
            if (item == null) {
                player.playSound(player.getLocation(), plugin.getMenuFile().getInvalidItemSound(), 1, 1);
                return;
            }

            if (items.size() < 14) {
                if (!e.isShiftClick()) {
                    // only add one
                    ItemStack current = e.getCurrentItem();
                    int amount = current.getAmount();
                    boolean yes = false;

                    for (ItemStack it : items) {
                        if (it.getType() == current.getType() && it.getAmount() < it.getMaxStackSize()) {
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
                            items.add(current);
                        }
                        e.setCurrentItem(null);
                    } else {
                        current.setAmount(amount - 1);
                        e.setCurrentItem(current);
                        current.setAmount(1);
                    }

                    if (!yes) {
                        items.add(current);
                    }

                } else {
                    boolean yes = false;
                    ItemStack current = e.getCurrentItem();
                    int amountLeft = current.getAmount();
                    for (ItemStack it : items) {
                        if (it.getType() == current.getType() && it.getAmount() < it.getMaxStackSize()) {
                            int index = items.indexOf(it);
                            ItemStack i = items.get(index);
                            if (i.getAmount() + amountLeft <= it.getMaxStackSize()) {
                                i.setAmount(i.getAmount() + amountLeft);
                                amountLeft = 0;
                                items.set(index, i);
                                yes = true;
                                break;
                            } else {
                                int min = i.getMaxStackSize() - i.getAmount();
                                i.setAmount(i.getMaxStackSize());
                                items.set(index, i);
                                amountLeft = amountLeft - min;
                            }
                            break;
                        }
                    }

                    if (!yes) {
                        current.setAmount(amountLeft);
                        items.add(current);
                    }
                    e.setCurrentItem(null);
                }

                PLAYER_SELLS.put(player, items);
                PLAYER_CANCEL_CLOSE.put(player, true);
                player.playSound(player.getLocation(), plugin.getMenuFile().getItemAddSound(), 1, 1);
                openMenu(player, false, 1);
                PLAYER_CANCEL_CLOSE.remove(player);
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (e.getView().getTitle().equals(Utils.c(plugin.getMenuFile().getSellMenuTitle())) && !PLAYER_CANCEL_CLOSE.getOrDefault(player, false)) {
            if (!PLAYER_SELLS.containsKey(player)) return;

            for (ItemStack item : PLAYER_SELLS.get(player)) {
                player.getInventory().addItem(item);
            }
            PLAYER_CANCEL_CLOSE.remove(player);

            if (PLAYER_SELLS.get(player).size() > 0) {
                List<String> cm = plugin.getMenuFile().getCancelMessage();
                for (String s : Utils.c(cm)) {
                    player.sendMessage(s);
                }
            }
            PLAYER_SELLS.remove(player);
            player.playSound(player.getLocation(), plugin.getMenuFile().getCancelSound(), 1, 1);
        }
    }
}
