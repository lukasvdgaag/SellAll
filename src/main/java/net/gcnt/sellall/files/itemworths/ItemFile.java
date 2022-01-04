package net.gcnt.sellall.files.itemworths;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.files.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.List;
import java.util.logging.Level;

public class ItemFile extends YamlFile {

    private final List<Item> items;

    public ItemFile(SellAll plugin) {
        super(plugin, "items.yml", true);
        this.items = Lists.newArrayList();
        setup();
        loadData();
    }

    @Override
    public void loadData() {
        if (!this.file.exists()) return;

        items.clear();

        for (String item : conf.getKeys(false)) {
            if (Material.matchMaterial(item) != null) {
                items.add(new Item(Material.matchMaterial(item), conf.getDouble(item + ".worth", 0)));
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Item '" + item + "' is not a valid material. Ignoring it.");
            }
        }
    }

    public List<Item> getItems() {
        return items;
    }
}
