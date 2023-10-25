package net.gcnt.sellall.items;

import io.th0rgal.oraxen.api.OraxenItems;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.gcnt.sellall.files.ItemFile;
import net.gcnt.sellall.utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Item {

    private final String id;
    private final Material item;
    private final double sellWorth;
    private final ItemType type;
    private final String externalId;
    private String externalType;
    private final int maxDailySells;

    public Item(String item, double sellWorth, int maxDailySells) {
        this.id = item;
        this.sellWorth = sellWorth;
        this.maxDailySells = maxDailySells;
        if (item.startsWith("MMO-")) {
            this.item = null;
            this.type = ItemType.MMO;
            String[] split = item.replace("MMO-", "").split(":");

            if (split.length != 2) {
                throw new IllegalArgumentException("Input '" + item + "' does not have the right amount of arguments for an MMO item (type:id). Ignoring it.");
            }

            if (!MMOItems.plugin.getTemplates().hasTemplate(Type.get(split[0]), split[1])) {
                throw new IllegalArgumentException("Input '" + item + "' is not a valid MMO item. Ignoring it.");
            } else {
                externalType = split[0];
                externalId = split[1];
            }
        } else if (item.startsWith("ORAX-")) {
            this.item = null;
            this.type = ItemType.ORAXEN;
            this.externalId = item.replace("ORAX-", "");
            if (!OraxenItems.exists(externalId)) {
                throw new IllegalArgumentException("Input '" + externalId + "' is not a valid oraxen item. Ignoring it.");
            }
        } else {
            this.item = Material.matchMaterial(item);
            this.type = ItemType.BUKKIT;
            this.externalId = null;
            if (this.item == null) {
                throw new IllegalArgumentException("Input '" + item + "' is not a valid material. Ignoring it.");
            }
        }
    }

    public static Item fromMaterial(ItemFile itemFile, ItemStack input) {
        if (input == null) return null;
        String oraxenId = OraxenItems.getIdByItem(input);
        String mmoId = MMOItems.getID(input);
        Type mmoType = MMOItems.getType(input);
        if (mmoId != null && mmoId.isEmpty()) mmoId = null;
        boolean hasNBT = Utils.hasNBTTag(input);

        for (Item item : itemFile.getItems()) {
            switch (item.getType()) {
                case ORAXEN -> {
                    if (oraxenId != null && oraxenId.equals(item.getExternalId())) return item;
                }
                case MMO -> {
                    if (mmoId != null && mmoType != null && mmoType.getId().equals(item.getExternalType()) && mmoId.equals(item.getExternalId())) return item;
                }
                default -> {
                    if (item.getItem().equals(input.getType()) && mmoId == null && oraxenId == null && !hasNBT) return item;
                }
            }
        }
        return null;
    }

    public static Item fromId(ItemFile itemFile, String id) {
        if (itemFile == null || id == null) return null;

        for (Item item : itemFile.getItems()) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public Material getItem() {
        return item;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getExternalType() {
        return externalType;
    }

    public int getMaxDailySells() {
        return maxDailySells;
    }

    public double getSellWorth() {
        return sellWorth;
    }

    public ItemType getType() {
        return type;
    }

    public ItemStack getExample() {
        return switch (type) {
            case BUKKIT -> new ItemStack(item, 1);
            case MMO -> MMOItems.plugin.getItem(Type.get(externalType), externalId);
            case ORAXEN -> OraxenItems.getItemById(externalId).build();
        };
    }

    public int getAmount(ItemStack item) {
        return switch (type) {
            case BUKKIT -> item.getType().equals(item.getType()) ? item.getAmount() : 0;
            case ORAXEN -> {
                String idRes = OraxenItems.getIdByItem(item);
                if (idRes != null && idRes.equals(externalId)) yield item.getAmount();
                else yield 0;
            }
            case MMO -> {
                String idRes = MMOItems.getID(item);
                Type typeRes = MMOItems.getType(item);
                if (idRes != null && idRes.equals(externalId) && typeRes != null && typeRes.getId().equals(externalType)) yield item.getAmount();
                else yield 0;
            }
        };
    }


}
