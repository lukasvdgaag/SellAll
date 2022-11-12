package net.gcnt.sellall.commands;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.config.ConfigProperties;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record SellAllCmd(SellAll plugin) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.NO_MENU_SPECIFIED)));
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("sellall.admin")) {
                plugin.getCfg().setup();
                plugin.getCfg().loadData();

                plugin.getMenuManager().load();

                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.RELOAD_MESSAGE)));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed from in-game. Use '/sellall reload' to reload the plugin, " +
                        "or '/sellall [menu] [player]' to open a menu for a player.");
                return true;
            }
            String menu = args[0];

            if (!sender.hasPermission("sellall.menu." + menu) && !sender.hasPermission("sellall.admin")) {
                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.NO_PERMISSION_MESSAGE)));
                return true;
            }

            // opening a specific menu
            if (plugin.getMenuManager().menuExists(menu)) {
                plugin.getMenuManager().openSellMenu((Player) sender, menu);
            } else {
                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.INVALID_MENU_MESSAGE)));
            }
        } else if (sender.hasPermission("sellall.admin")) {
            // at least 2 arguments, is admin, so opening menu for other players.
            String menu = args[0];

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.INVALID_PLAYER_MESSAGE)));
                return true;
            }

            if (plugin.getMenuManager().menuExists(menu)) {
                plugin.getMenuManager().openSellMenu(target, menu);
            } else {
                sender.sendMessage(Utils.c(plugin.getCfg().getString(ConfigProperties.INVALID_MENU_MESSAGE)));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> strings = plugin.getMenuManager().getMenus().stream()
                    .filter(s -> sender.hasPermission("sellall.menu." + s) || sender.hasPermission("sellall.admin"))
                    .collect(Collectors.toList());

            if (sender.hasPermission("sellall.admin")) strings.add("reload");
            return strings;
        } else if (args.length == 2 && sender.hasPermission("sellall.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }

        return new ArrayList<>();
    }
}
