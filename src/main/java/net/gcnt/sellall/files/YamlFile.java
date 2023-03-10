package net.gcnt.sellall.files;

import net.gcnt.sellall.SellAll;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class YamlFile {

    protected final SellAll plugin;
    protected final String fileName;
    protected final String resource;
    protected final String directory;
    protected File file;
    protected FileConfiguration conf;

    public YamlFile(SellAll plugin, String fileName) {
        this(plugin, fileName, fileName);
    }

    public YamlFile(SellAll plugin, String fileName, @Nullable String resource) {
       this(plugin, fileName, resource, null);
    }

    public YamlFile(SellAll plugin, String fileName, @Nullable String resource, @Nullable String directory) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.resource = resource;
        this.directory = directory;
        determineFile();
    }

    public void determineFile() {
        File dir = directory == null || directory.equals("") ? plugin.getDataFolder() : new File(plugin.getDataFolder(), directory);
        this.file = new File(dir, this.fileName);
    }

    public void setup() {
        if (!file.exists()) {

            if (resource != null) {
                plugin.saveResource(fileName, false);
            } else {
                try {
                    if (!file.createNewFile()) {
                        plugin.getLogger().severe("SellAll failed to create the " + fileName + " file.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        conf = YamlConfiguration.loadConfiguration(file);
    }

    public abstract void loadData();

    public String getString(String property) {
        return getString(property, null);
    }

    public String getString(String property, String def) {
        return conf.getString(property, def);
    }

    public int getInt(String property) {
        return getInt(property, 0);
    }

    public int getInt(String property, int def) {
        return conf.getInt(property, def);
    }

    public double getDouble(String property) {
        return getDouble(property, 0.0);
    }

    public double getDouble(String property, double def) {
        return conf.getDouble(property, def);
    }

    public List<String> getStringList(String property) {
        return conf.getStringList(property);
    }


}
