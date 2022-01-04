package net.gcnt.sellall.utils;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public record Utils(SellAll plugin) {

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
