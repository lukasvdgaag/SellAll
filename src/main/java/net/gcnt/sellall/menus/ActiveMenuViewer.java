package net.gcnt.sellall.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ActiveMenuViewer {

    private final Player player;
    private final List<ItemStack> sellingItems;
    private boolean lookingAtItems;
    private Menu activeMenu;
    private int itemsPage;
    private boolean ignoreClose;

    public ActiveMenuViewer(Player player) {
        this.player = player;
        this.sellingItems = new ArrayList<>();
        this.lookingAtItems = false;
        this.itemsPage = 1;
        this.ignoreClose = false;
    }

    public Player getPlayer() {
        return player;
    }

    public void setActiveMenu(Menu activeMenu) {
        this.activeMenu = activeMenu;
    }

    public void setIgnoreClose(boolean ignoreClose) {
        this.ignoreClose = ignoreClose;
    }

    public boolean isIgnoreClose() {
        return ignoreClose;
    }

    public Menu getActiveMenu() {
        return activeMenu;
    }

    public List<ItemStack> getSellingItems() {
        return sellingItems;
    }

    public boolean isLookingAtItems() {
        return lookingAtItems;
    }

    public void setLookingAtItems(boolean lookingAtItems) {
        this.lookingAtItems = lookingAtItems;
    }

    public int getItemsPage() {
        return itemsPage;
    }

    public void setItemsPage(int itemsPage) {
        this.itemsPage = itemsPage;
    }
}
