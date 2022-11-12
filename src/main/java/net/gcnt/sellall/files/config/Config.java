package net.gcnt.sellall.files.config;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.LinkedHashMap;

public class Config extends YamlFile {

    private final LinkedHashMap<String, Integer> taxes; // permission - tax rate
    private final LinkedHashMap<Integer, Double> worthTiers; // tier - worth

    public Config(SellAll plugin) {
        super(plugin, "config.yml", "config.yml");
        this.taxes = new LinkedHashMap<>();
        this.worthTiers = new LinkedHashMap<>();
        setup();
        loadData();
    }

    @Override
    public void loadData() {
        if (!file.exists()) return;

        taxes.clear();

        if (conf.contains("tax-permissions")) {
            ConfigurationSection section = conf.getConfigurationSection("tax-permissions");
            assert section != null;
            for (String s : section.getKeys(false)) {
                int tax = section.getInt(s);
                taxes.put(s, tax);

                if (Bukkit.getPluginManager().getPermission(s) == null) {
                    Permission perm = new Permission(s);
                    perm.setDescription("Sets the SellAll tax rate to " + tax + "%");
                    perm.setDefault(PermissionDefault.NOT_OP);
                    Bukkit.getPluginManager().addPermission(perm);
                }
            }
        }

        if (conf.contains(ConfigProperties.WORTH_TIERS)) {
            ConfigurationSection section = conf.getConfigurationSection(ConfigProperties.WORTH_TIERS);
            assert section != null;
            for (String tier : section.getKeys(false)) {
                double worth = section.getDouble(tier) / 100;
                worthTiers.put(Integer.parseInt(tier), worth);
            }
        }
    }

    public LinkedHashMap<Integer, Double> getWorthTiers() {
        return worthTiers;
    }

    public LinkedHashMap<String, Integer> getTaxes() {
        return taxes;
    }
}
