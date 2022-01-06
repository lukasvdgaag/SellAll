package net.gcnt.sellall.menus;

import net.gcnt.sellall.SellAll;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class Menu {

    protected final SellAll plugin;

    public Menu(SellAll plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, boolean openFirst, int page) {
        if (openFirst) {
            player.playSound(player.getLocation(), plugin.getMenuFile().getMenuOpenSound(), 1, 1);
        }
    }

    protected ItemStack createItemStack(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

}
