package net.gcnt.sellall.files;

import com.google.common.collect.Lists;
import net.gcnt.sellall.SellAll;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Objects;

public class MenuFile extends YamlFile {

    private String sellMenuTitle = "&8&lSell All";

    private Material itemWorthOpenMaterial = Material.CHEST;
    private String itemWorthOpenDisplayName = "&e&lWorth item List";
    private List<String> itemWorthOpenLore = Lists.newArrayList("" +
                    ChatColor.translateAlternateColorCodes('&', "&8-----------------------------------"),
            " ",
            ChatColor.translateAlternateColorCodes('&', "       &f&lClick&7 to Open Worth Item List"),
            " ",
            ChatColor.translateAlternateColorCodes('&', "&8-----------------------------------"));

    private Material infoMaterial = Material.BOOK;
    private String infoDisplayName = "&eHow to sell your items";
    private List<String> infoLore = Lists.newArrayList(
            ChatColor.GRAY + "Click an item in your inventory to add it",
            ChatColor.GRAY + "to the sell menu. If you think you're ready,",
            ChatColor.GRAY + "click the " + ChatColor.GREEN + "slime ball" + ChatColor.GRAY + " to sell the items.");

    private Material fillMaterial = Material.BLUE_STAINED_GLASS_PANE;

    private Material noItemAddedMaterial = Material.BLACK_STAINED_GLASS_PANE;
    private String noItemAddedDisplayName = "&cNo item";
    private List<String> noItemAddedLore = Lists.newArrayList();

    private Material itemAddedMaterial = Material.GREEN_STAINED_GLASS_PANE;
    private String itemAddedDisplayName = "&a&lFilled";
    private List<String> itemAddedLore = Lists.newArrayList("" +
                    ChatColor.translateAlternateColorCodes('&', "&7+----------------------------------+"),
            " ",
            ChatColor.translateAlternateColorCodes('&', "       &b&lWorth: &f&l$%worth%"),
            " ",
            ChatColor.translateAlternateColorCodes('&', "&8+----------------------------------+"));

    private Material cancelMaterial = Material.BARRIER;
    private String cancelDisplayName = "&cCancel Sell All";
    private List<String> cancelLore = Lists.newArrayList(ChatColor.GRAY + "Cancel the selling of your items");

    private Material proceedMaterial = Material.SLIME_BALL;
    private String proceedDisplayName = "&aSell the items";
    private List<String> proceedLore = Lists.newArrayList(ChatColor.GRAY + "Ready to sell your items?",
            ChatColor.GRAY + "Click this item to proceed your selling.",
            " ",
            ChatColor.AQUA + "Worth: " + ChatColor.GREEN + "$%totalworth%",
            ChatColor.RED + "Tax: " + ChatColor.GRAY + "$%tax_amount% (%tax_percentage%%)");

    private Sound invalidItemSound = Sound.ENTITY_VILLAGER_NO;
    private Sound menuOpenSound = Sound.BLOCK_ENDER_CHEST_OPEN;
    private Sound sellSound = Sound.ENTITY_VILLAGER_TRADE;
    private Sound cancelSound = Sound.BLOCK_BEEHIVE_EXIT;
    private Sound itemAddSound = Sound.BLOCK_NOTE_BLOCK_PLING;
    private Sound itemRemoveSound = Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM;

    private List<String> sellMessage = Lists.newArrayList("&8--------------------------------------------",
            "&aThanks for the sale, &e%player%!",
            "&7You earned: &b%earned% SBDollars",
            "&7Taxes: %tax_percentage%% (%tax_amount% SBDollars)",
            "&8--------------------------------------------");
    private List<String> cancelMessage = Lists.newArrayList("&cYou cancelled your item selling!",
            "&7All items have been returned to your inventory.");


    ///////////////////

    private String itemListMenuTitle = "&8&lSell All";

    private Material itemListMaterial = Material.BOOK;
    private String itemListDisplayName = "&aSell All Menu";
    private List<String> itemListLore = Lists.newArrayList("&7Click here to return to the sell menu");

    private Material nextPageMaterial = Material.PAPER;
    private String nextPageDisplayName = "&bNext page";
    private List<String> nextPageLore = Lists.newArrayList("&7Click to go to page %page%");

    private Material previousPageMaterial = Material.PAPER;
    private String previousPageDisplayName = "&bPrevious page";
    private List<String> previousPageLore = Lists.newArrayList("&7Click to go to page %page%");

    private Material itemListFillMaterial = Material.BLUE_STAINED_GLASS_PANE;

    private List<String> itemListItemLore = Lists.newArrayList(ChatColor.GRAY + "Worth: " + ChatColor.AQUA + "$%worth%");

    private Sound nextPageSound = Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM;
    private Sound previousPageSound = Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM;


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
        return sellMessage;
    }

    public List<String> getCancelMessage() {
        return cancelMessage;
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
        return itemWorthOpenLore;
    }

    public Material getInfoMaterial() {
        return infoMaterial;
    }

    public String getInfoDisplayName() {
        return infoDisplayName;
    }

    public List<String> getInfoLore() {
        return infoLore;
    }

    public Material getProceedMaterial() {
        return proceedMaterial;
    }

    public String getProceedDisplayName() {
        return proceedDisplayName;
    }

    public List<String> getProceedLore() {
        return proceedLore;
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
        return noItemAddedLore;
    }

    public Material getItemAddedMaterial() {
        return itemAddedMaterial;
    }

    public String getItemAddedDisplayName() {
        return itemAddedDisplayName;
    }

    public List<String> getItemAddedLore() {
        return itemAddedLore;
    }

    public Material getCancelMaterial() {
        return cancelMaterial;
    }

    public String getCancelDisplayName() {
        return cancelDisplayName;
    }

    public List<String> getCancelLore() {
        return cancelLore;
    }

    public List<String> getItemListItemLore() {
        return itemListItemLore;
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
        return itemListLore;
    }

    public Material getNextPageMaterial() {
        return nextPageMaterial;
    }

    public String getNextPageDisplayName() {
        return nextPageDisplayName;
    }

    public List<String> getNextPageLore() {
        return nextPageLore;
    }

    public Material getPreviousPageMaterial() {
        return previousPageMaterial;
    }

    public String getPreviousPageDisplayName() {
        return previousPageDisplayName;
    }

    public List<String> getPreviousPageLore() {
        return previousPageLore;
    }

    public Material getItemListFillMaterial() {
        return itemListFillMaterial;
    }
}

