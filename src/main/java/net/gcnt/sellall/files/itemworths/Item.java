package net.gcnt.sellall.files.itemworths;

import org.bukkit.Material;

public record Item(Material item, double sellWorth) {

    public static Item fromMaterial(ItemFile itemFile, Material material) {
        for (Item item : itemFile.getItems()) {
            if (item.getItem() == material) {
                return item;
            }
        }
        return null;
    }

    public Material getItem() {
        return item;
    }

    public double getSellWorth() {
        return sellWorth;
    }

}
