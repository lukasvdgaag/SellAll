package net.gcnt.sellall.commands;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.config.ConfigProperties;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public record SellAllCmd(SellAll plugin) implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("sellall.admin")) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.getCfg().setup();
                    plugin.getCfg().loadData();

                    plugin.getItemFile().setup();
                    plugin.getItemFile().loadData();

                    plugin.getMenuFile().setup();
                    plugin.getMenuFile().loadData();

                    sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.RELOAD_MESSAGE)));
                } else {
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player == null) {
                        sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.INVALID_PLAYER_MESSAGE)));
                    } else {
                        plugin.getSellMenu().openMenu(player, true, 1);
                        sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.MENU_OPENED_OTHER_MESSAGE)
                                .replace("%player%", player.getName())));
                    }
                }
                return true;
            }
        }

        if (sender instanceof Player) {
            if (sender.hasPermission("sellall.player") || sender.hasPermission("sellall.admin")) {
                plugin.getSellMenu().openMenu((Player) sender, true, 1);
            } else {
                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.NO_PERMISSION_MESSAGE)));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player");
        }
        return true;
    }
}
