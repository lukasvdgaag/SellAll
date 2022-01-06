package net.gcnt.sellall.files;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Objects;

public class MenuFile extends YamlFile {

    private String sellMenuTitle;

    private Material itemWorthOpenMaterial;
    private String itemWorthOpenDisplayName;
    private List<String> itemWorthOpenLore;

    private Material infoMaterial;
    private String infoDisplayName;
    private List<String> infoLore;

    private Material fillMaterial;

    private Material noItemAddedMaterial;
    private String noItemAddedDisplayName;
    private List<String> noItemAddedLore;

    private Material itemAddedMaterial;
    private String itemAddedDisplayName;
    private List<String> itemAddedLore;

    private Material cancelMaterial;
    private String cancelDisplayName;
    private List<String> cancelLore;

    private Material proceedMaterial;
    private String proceedDisplayName;
    private List<String> proceedLore;

    private Sound invalidItemSound;
    private Sound menuOpenSound;
    private Sound sellSound;
    private Sound cancelSound;
    private Sound itemAddSound;
    private Sound itemRemoveSound;

    private List<String> sellMessage;
    private List<String> cancelMessage;


    ///////////////////

    private String itemListMenuTitle;

    private Material itemListMaterial;
    private String itemListDisplayName;
    private List<String> itemListLore;

    private Material nextPageMaterial;
    private String nextPageDisplayName;
    private List<String> nextPageLore;

    private Material previousPageMaterial;
    private String previousPageDisplayName;
    private List<String> previousPageLore;

    private Material itemListFillMaterial;

    private List<String> itemListItemLore;

    private Sound nextPageSound;
    private Sound previousPageSound;


    public MenuFile(SellAll plugin) {
        super(plugin, "menu.yml", true);
        setup();
        loadData();
    }

    @Override
    public void loadData() {
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            // sell menu section
            if (config.contains("item-list-menu")) {
                ConfigurationSection section = config.getConfigurationSection("item-list-menu");
                assert section != null;

                itemListMenuTitle = section.getString("menu_title", "&8&lSell All");
                itemListMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("return.material", "BOOK")));
                itemListDisplayName = section.getString("return.displayName", "&aSell All Menu");
                itemListLore = section.getStringList("return.lore");

                itemListFillMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("fill.material", "BLUE_STAINED_GLASS_PANE")));

                nextPageMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("next_page.material", "PAPER")));
                nextPageDisplayName = section.getString("next_page.displayName", "&bNext page");
                nextPageLore = section.getStringList("next_page.lore");

                previousPageMaterial = Material.matchMaterial(Objects.requireNonNull(section.getString("previous_page.material", "PAPER")));
                previousPageDisplayName = section.getString("previous_page.displayName", "&bPrevious page");
                previousPageLore = section.getStringList("previous_page.lore");

                itemListItemLore = section.getStringList("item.lore");

                nextPageSound = Sound.valueOf(section.getString("next_page_sound", "ENTITY_ITEM_FRAME_REMOVE_ITEM"));
                previousPageSound = Sound.valueOf(section.getString("previous_page_sound", "ENTITY_ITEM_FRAME_REMOVE_ITEM"));
            }

            if (config.contains("sell-menu")) {
                ConfigurationSection sellMenu = config.getConfigurationSection("sell-menu");
                assert sellMenu != null;

                sellMenuTitle = sellMenu.getString("menu_title", "&8&lSell All");
                itemWorthOpenMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("item_worth_list.material", "CHEST")));
                itemWorthOpenDisplayName = sellMenu.getString("item_worth_list.displayName", "&e&lWorth item List");
                itemWorthOpenLore = sellMenu.getStringList("item_worth_list.lore");

                infoMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("info.material", "BOOK")));
                infoDisplayName = sellMenu.getString("info.displayName", "&eHow to sell your items");
                infoLore = sellMenu.getStringList("info.lore");

                fillMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("fill.material", "BLUE_STAINED_GLASS_PANE")));

                noItemAddedMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("no_item_added.material", "BLACK_STAINED_GLASS_PANE")));
                noItemAddedDisplayName = sellMenu.getString("no_item_added.displayName", "&cNo item");
                noItemAddedLore = sellMenu.getStringList("no_item_added.lore");

                itemAddedMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("item_added.material", "GREEN_STAINED_GLASS_PANE")));
                itemAddedDisplayName = sellMenu.getString("item_added.displayName", "&a&lFilled");
                itemAddedLore = sellMenu.getStringList("item_added.lore");

                cancelMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("cancel.material", "BARRIER")));
                cancelDisplayName = sellMenu.getString("cancel.displayName", "&cCancel Sell All");
                cancelLore = sellMenu.getStringList("cancel.lore");

                proceedMaterial = Material.matchMaterial(Objects.requireNonNull(sellMenu.getString("proceed.material", "SLIME_BALL")));
                proceedDisplayName = sellMenu.getString("proceed.displayName", "&aSell all items");
                proceedLore = sellMenu.getStringList("proceed.lore");

                invalidItemSound = Sound.valueOf(sellMenu.getString("invalid_item_sound", "ENTITY_VILLAGER_NO"));
                menuOpenSound = Sound.valueOf(sellMenu.getString("menu_open_sound", "BLOCK_ENDER_CHEST_OPEN"));
                sellSound = Sound.valueOf(sellMenu.getString("sell_sound", "ENTITY_VILLAGER_TRADE"));
                cancelSound = Sound.valueOf(sellMenu.getString("cancel_sound", "BLOCK_BEEHIVE_EXIT"));
                itemAddSound = Sound.valueOf(sellMenu.getString("item_add_sound", "BLOCK_NOTE_BLOCK_PLING"));
                itemRemoveSound = Sound.valueOf(sellMenu.getString("item_remove_sound", "ENTITY_ITEM_FRAME_REMOVE_ITEM"));

                sellMessage = sellMenu.getStringList("sell_message");
                cancelMessage = sellMenu.getStringList("cancel_message");

            }
        }
    }

    public Sound getNextPageSound() {
        return nextPageSound;
    }

    public Sound getPreviousPageSound() {
        return previousPageSound;
    }

    public Sound getItemRemoveSound() {
        return itemRemoveSound;
    }

    public Sound getItemAddSound() {
        return itemAddSound;
    }

    public List<String> getSellMessage() {
        return Lists.newArrayList(sellMessage);
    }

    public List<String> getCancelMessage() {
        return Lists.newArrayList(cancelMessage);
    }

    public Sound getCancelSound() {
        return cancelSound;
    }

    public Sound getInvalidItemSound() {
        return invalidItemSound;
    }

    public Sound getMenuOpenSound() {
        return menuOpenSound;
    }

    public Sound getSellSound() {
        return sellSound;
    }

    public String getSellMenuTitle() {
        return sellMenuTitle;
    }

    public Material getItemWorthOpenMaterial() {
        return itemWorthOpenMaterial;
    }

    public String getItemWorthOpenDisplayName() {
        return itemWorthOpenDisplayName;
    }

    public List<String> getItemWorthOpenLore() {
        return Lists.newArrayList(itemWorthOpenLore);
    }

    public Material getInfoMaterial() {
        return infoMaterial;
    }

    public String getInfoDisplayName() {
        return infoDisplayName;
    }

    public List<String> getInfoLore() {
        return Lists.newArrayList(infoLore);
    }

    public Material getProceedMaterial() {
        return proceedMaterial;
    }

    public String getProceedDisplayName() {
        return proceedDisplayName;
    }

    public List<String> getProceedLore() {
        return Lists.newArrayList(proceedLore);
    }

    public Material getFillMaterial() {
        return fillMaterial;
    }

    public Material getNoItemAddedMaterial() {
        return noItemAddedMaterial;
    }

    public String getNoItemAddedDisplayName() {
        return noItemAddedDisplayName;
    }

    public List<String> getNoItemAddedLore() {
        return Lists.newArrayList(noItemAddedLore);
    }

    public Material getItemAddedMaterial() {
        return itemAddedMaterial;
    }

    public String getItemAddedDisplayName() {
        return itemAddedDisplayName;
    }

    public List<String> getItemAddedLore() {
        return Lists.newArrayList(itemAddedLore);
    }

    public Material getCancelMaterial() {
        return cancelMaterial;
    }

    public String getCancelDisplayName() {
        return cancelDisplayName;
    }

    public List<String> getCancelLore() {
        return Lists.newArrayList(cancelLore);
    }

    public List<String> getItemListItemLore() {
        return Lists.newArrayList(itemListItemLore);
    }

    public String getItemListMenuTitle() {
        return itemListMenuTitle;
    }

    public Material getItemListMaterial() {
        return itemListMaterial;
    }

    public String getItemListDisplayName() {
        return itemListDisplayName;
    }

    public List<String> getItemListLore() {
        return Lists.newArrayList(itemListLore);
    }

    public Material getNextPageMaterial() {
        return nextPageMaterial;
    }

    public String getNextPageDisplayName() {
        return nextPageDisplayName;
    }

    public List<String> getNextPageLore() {
        return Lists.newArrayList(nextPageLore);
    }

    public Material getPreviousPageMaterial() {
        return previousPageMaterial;
    }

    public String getPreviousPageDisplayName() {
        return previousPageDisplayName;
    }

    public List<String> getPreviousPageLore() {
        return Lists.newArrayList(previousPageLore);
    }

    public Material getItemListFillMaterial() {
        return itemListFillMaterial;
    }
}

