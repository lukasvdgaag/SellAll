package net.gcnt.sellall;

import net.gcnt.sellall.commands.SellAllCmd;
import net.gcnt.sellall.files.config.Config;
import net.gcnt.sellall.files.MenuFile;
import net.gcnt.sellall.files.ItemFile;
import net.gcnt.sellall.files.logs.Log;
import net.gcnt.sellall.files.logs.MySQLLog;
import net.gcnt.sellall.menus.ItemListMenu;
import net.gcnt.sellall.menus.SellMenu;
import net.gcnt.sellall.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class SellAll extends JavaPlugin implements Listener {

    public Economy economy = null;
    private ItemFile itemFile;
    private MenuFile menuFile;
    private Config config;
    private Log log;
    private Utils utils;

    private ItemListMenu itemListMenu;
    private SellMenu sellMenu;
    private MySQLLog mySQLLog;

    public ItemFile getItemFile() {
        return itemFile;
    }

    public MenuFile getMenuFile() {
        return menuFile;
    }

    public Config getCfg() {
        return config;
    }

    public Log getLog() {
        return log;
    }

    public Utils getUtils() {
        return utils;
    }

    public ItemListMenu getItemListMenu() {
        return itemListMenu;
    }

    public SellMenu getSellMenu() {
        return sellMenu;
    }

    public Economy getEconomy() {
        return economy;
    }

    public MySQLLog getMySQLLog() {
        return mySQLLog;
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

        this.itemFile = new ItemFile(this);
        this.menuFile = new MenuFile(this);
        this.config = new Config(this);
        this.log = new Log(this);
        this.utils = new Utils(this);
        this.mySQLLog = new MySQLLog(this);

        this.itemListMenu = new ItemListMenu(this);
        Bukkit.getPluginManager().registerEvents(this.itemListMenu, this);
        this.sellMenu = new SellMenu(this);
        Bukkit.getPluginManager().registerEvents(this.sellMenu, this);

        Bukkit.getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("sellall")).setExecutor(new SellAllCmd(this));
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }

        return (this.economy != null);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getMessage().equals("opensellmenu")) {
            Bukkit.getScheduler().runTask(this, () -> sellMenu.openMenu(e.getPlayer(), true, 1));
        }
    }
}
