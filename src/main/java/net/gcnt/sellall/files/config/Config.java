package net.gcnt.sellall.files.config;

import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;

public class Config extends YamlFile {

    private final HashMap<String, Integer> taxes; // permission - tax rate
    private final HashMap<Integer, Double> worthTiers; // tier - worth

    public Config(SellAll plugin) {
        super(plugin, "config.yml", true);
        this.taxes = new HashMap<>();
        this.worthTiers = new HashMap<>();
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

    public HashMap<Integer, Double> getWorthTiers() {
        return worthTiers;
    }

    public HashMap<String, Integer> getTaxes() {
        return taxes;
    }
}
