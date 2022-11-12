package net.gcnt.sellall;

import net.gcnt.sellall.commands.SellAllCmd;
import net.gcnt.sellall.files.ItemFile;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.config.Config;
import net.gcnt.sellall.files.logs.Log;
import net.gcnt.sellall.files.logs.MySQLLog;
import net.gcnt.sellall.menus.ItemListMenu;
import net.gcnt.sellall.menus.MenuManager;
import net.gcnt.sellall.menus.SellMenu;
import net.gcnt.sellall.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class SellAll extends JavaPlugin implements Listener {

    public Economy economy = null;
    private MenuManager menuManager;
    private Config config;
    private Log log;
    private Utils utils;

    private MySQLLog mySQLLog;

    public Config getCfg() {
        return config;
    }

    public Log getLog() {
        return log;
    }

    public Utils getUtils() {
        return utils;
    }

    public Economy getEconomy() {
        return economy;
    }

    public MySQLLog getMySQLLog() {
        return mySQLLog;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    @Override
    public void onEnable() {
        if (setupEconomy()) {
            Bukkit.getLogger().log(Level.INFO, "Found Vault Economy, now loading the SellAll plugin...");
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to load Vault Economy. Is there an economy system?\n" +
                    "Now disabling the plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.config = new Config(this);
        this.log = new Log(this);
        this.utils = new Utils(this);
        this.mySQLLog = new MySQLLog(this);

        this.menuManager = new MenuManager(this);
        this.menuManager.load();

        getServer().getPluginManager().registerEvents(this.menuManager, this);

        final SellAllCmd cmdInstance = new SellAllCmd(this);
        final PluginCommand command = getCommand("sellall");
        command.setExecutor(cmdInstance);
        command.setTabCompleter(cmdInstance);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }

        return (this.economy != null);
    }

}
