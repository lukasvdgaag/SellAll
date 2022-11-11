package net.gcnt.sellall.files;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.items.Item;

import java.util.List;

public class ItemFile extends YamlFile {

    private final List<Item> items;

    public ItemFile(SellAll plugin, String id) {
        super(plugin, id + ".yml", null, "items");
        this.items = Lists.newArrayList();
        setup();
        loadData();
    }

    @Override
    public void loadData() {
        if (!this.file.exists()) return;

        items.clear();

        for (String item : conf.getKeys(false)) {
            try {
                items.add(new Item(item, conf.getDouble(item + ".worth", 0), conf.getInt(item + ".max_sells", -1)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Item> getItems() {
        return items;
    }
}
