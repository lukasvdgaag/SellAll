package net.gcnt.sellall.files.logs;

import net.gcnt.sellall.SellAll;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class Log {

    private final SellAll plugin;
    private File currentLogFile = null;
    private FileConfiguration currentLogConfiguration = null;

    public Log(SellAll plugin) {
        this.plugin = plugin;
        loadLogFile();
    }

    public void loadLogFile() {
        int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        currentLogFile = new File(plugin.getDataFolder() + File.separator + "logs", year + " week " + week + ".yml");

        File folder = new File(plugin.getDataFolder(), "logs");
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                plugin.getLogger().severe("SellAll failed to create the /logs folder inside the plugin's data folder.");
            }
        }

        if (!currentLogFile.exists()) {
            try {
                if (!currentLogFile.createNewFile()) {
                    plugin.getLogger().severe("SellAll failed to create the latest log file inside the /logs folder.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentLogConfiguration = YamlConfiguration.loadConfiguration(currentLogFile);
    }

    public void log(Player player, List<ItemStack> items, double tax) {
        int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        if (currentLogFile == null || currentLogConfiguration == null || !currentLogFile.getName().equals(year + " week " + week + ".yml")) {
            loadLogFile();
        }

        for (ItemStack item : items) {
            if (item != null) {
                Material material = item.getType();
                int amount = item.getAmount();
                int current = currentLogConfiguration.getInt(player.getUniqueId() + "." + material.name(), 0);
                currentLogConfiguration.set(player.getUniqueId() + "." + material.name(), current + amount);
            }
        }

        double currentTax = currentLogConfiguration.getDouble(player.getUniqueId() + ".tax", 0);
        currentLogConfiguration.set(player.getUniqueId() + ".tax", currentTax + tax);

        try {
            currentLogConfiguration.save(currentLogFile);
            currentLogConfiguration = YamlConfiguration.loadConfiguration(currentLogFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
