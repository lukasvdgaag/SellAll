package net.gcnt.sellall.utils;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public record Utils(SellAll plugin) {

    private static final List<String> itemNBTTags = Lists.newArrayList(
            "Enchantments",
            "display",
            "Name",
            "Lore",
            "Damage",
            "color",
            "Unbreakable",
            "RepairCost",
            "CanDestroy",
            "CanPlaceOn",
            "HideFlags"
    );

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> c(List<String> l) {
        List<String> a = Lists.newArrayList();
        for (String s : l) {
            a.add(c(s));
        }
        return a;
    }

    public static List<String> replace(List<String> list, String search, String replacement) {
        list.replaceAll((s) -> s.replace(search, replacement));
        return list;
    }

    public static boolean hasNBTTag(ItemStack item) {
        net.minecraft.world.item.ItemStack itemstack = CraftItemStack.asNMSCopy(item);
        if (itemstack.t()) {
            NBTTagCompound b = itemstack.u();

            if (b != null) {
                for (String key : b.e()) {
                    if (!itemNBTTags.contains(key)) return true;
                }
            }
        }
        return false;
    }

    public int getTax(Player player) {
        HashMap<String, Integer> taxes = plugin.getCfg().getTaxes();

        for (String s : taxes.keySet()) {
            if (player.hasPermission(s)) {
                return taxes.get(s);
            }
        }

        return 0;
    }

}
