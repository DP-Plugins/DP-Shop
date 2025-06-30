package com.darksoldier1404.dps.functions;

import com.darksoldier1404.dppc.api.essentials.MoneyAPI;
import com.darksoldier1404.dppc.lang.DLang;
import com.darksoldier1404.dps.Shop;
import com.darksoldier1404.dps.enums.SettingType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import com.darksoldier1404.dppc.api.inventory.*;
import com.darksoldier1404.dppc.utils.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.*;

import java.io.*;

import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("all")
public class DDSFunction {
    private static final Shop plugin = Shop.getInstance();
    private static String prefix;
    private static final MoneyAPI m = new MoneyAPI();

    public static void initConfig() {
        Shop.config = ConfigUtils.loadDefaultPluginConfig(plugin);
        Shop.prefix = ColorUtils.applyColor(Shop.config.getString("Settings.Prefix"));
        prefix = Shop.prefix;
        for (final YamlConfiguration shop : ConfigUtils.loadCustomDataList(plugin, "shops")) {
            Shop.shops.put(shop.getString("Shop.NAME"), shop);
        }
    }

    public static void reloadConfig(final Player p) {
        Shop.config = ConfigUtils.loadDefaultPluginConfig(plugin);
        Shop.prefix = ColorUtils.applyColor(Shop.config.getString("Settings.Prefix"));
        Shop.shops.clear();
        for (final YamlConfiguration shop : ConfigUtils.loadCustomDataList(plugin, "shops")) {
            Shop.shops.put(shop.getString("Shop.NAME"), shop);
        }
        plugin.lang = new DLang(Shop.config.getString("Settings.Lang") == null ? "Korean" : Shop.config.getString("Settings.Lang"), plugin);
        p.sendMessage(prefix + plugin.lang.get("config_reloaded"));
    }

    public static void createShop(final Player p, final String name) {
        if (Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_exists"));
            return;
        }
        final YamlConfiguration shop = new YamlConfiguration();
        shop.set("Shop.NAME", name);
        Shop.shops.put(name, shop);
        ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        p.sendMessage(prefix + name + plugin.lang.get("shop_created"));
        enableShop(p, name);
    }

    public static void openItemSettingGUI(final Player p, final String name, final int page) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        final DInventory inv = new DInventory( name + " Shop item setting (Page : " + page + ")", 54, false, plugin);
        inv.setObj(Triple.of(SettingType.SETTING_ITEMS, name, page));
        if (shop.getConfigurationSection("Shop.Items." + page) != null) {
            for (final String key : shop.getConfigurationSection("Shop.Items." + page).getKeys(false)) {
                inv.setItem(Integer.parseInt(key), shop.getItemStack("Shop.Items." + page + "." + key));
            }
        }
        p.openInventory(inv.getInventory());
        Shop.currentInv.put(p.getUniqueId(), inv);
    }

    public static void saveShopItems(final Player p, final String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        final DInventory inv = Shop.currentInv.get(p.getUniqueId());
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) inv.getObj();
        if (obj == null || obj.getA() != SettingType.SETTING_ITEMS || !((String) obj.getB()).equals(name)) {
            p.sendMessage(prefix + plugin.lang.get("wrong_inventory"));
            return;
        }
        final int page = obj.getC();
        for (int i = 0; i < 54; ++i) {
            if (inv.getItem(i) != null) {
                shop.set("Shop.Items." + page + "." + i, inv.getItem(i));
            } else {
                shop.set("Shop.Items." + page + "." + i, null);
            }
        }
        ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        p.sendMessage(prefix + name + plugin.lang.get("shop_items_saved"));
        Shop.shops.put(name, shop);
        Shop.currentInv.remove(p.getUniqueId());
    }

    public static void openPriceSettingGUI(final Player p, final String name, final int prevPage) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        if (getPageTools(p) == null) {
            p.sendMessage(prefix + "page tools is not set, [ /dshop ptget -> /dshop pt ] use this commands to set page tools.");
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        final DInventory inv = new DInventory( name + " shop price setting", 54, true, plugin);
        inv.setObj(Triple.of(SettingType.SETTING_PRICE, name, 0));
        final Map<Integer, Map<Integer, ItemStack>> items = new HashMap<>();
        if (shop.getConfigurationSection("Shop.Items") != null) {
            for (final String spage : shop.getConfigurationSection("Shop.Items").getKeys(false)) {
                final Map<Integer, ItemStack> pageContent = new HashMap<>();
                for (final String key : shop.getConfigurationSection("Shop.Items." + spage).getKeys(false)) {
                    final ItemStack item = shop.getItemStack("Shop.Items." + spage + "." + key).clone();
                    if (NBT.hasTagKey(item, "dlss_display")) {
                        final ItemMeta im = item.getItemMeta();
                        im.setDisplayName("§f");
                        item.setItemMeta(im);
                        inv.setItem(Integer.parseInt(key), item);
                        pageContent.put(Integer.parseInt(key), item);
                    } else if (NBT.hasTagKey(item, "dlss_page")) {
                        final ItemMeta im = item.getItemMeta();
                        final List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<>();
                        int index = 0;
                        for (String s : lore) {
                            if (s.contains("<currentMoney>")) {
                                s = s.replace("<currentMoney>", String.valueOf(getMoney(p)));
                                lore.set(index, s);
                            }
                            ++index;
                        }
                        im.setLore(lore);
                        im.setDisplayName("§f");
                        item.setItemMeta(im);
                        inv.setItem(Integer.parseInt(key), item);
                        pageContent.put(Integer.parseInt(key), item);
                    } else if (NBT.hasTagKey(item, "dlss_prev")) {
                        inv.setItem(Integer.parseInt(key), item);
                        pageContent.put(Integer.parseInt(key), item);
                    } else if (NBT.hasTagKey(item, "dlss_next")) {
                        inv.setItem(Integer.parseInt(key), item);
                        pageContent.put(Integer.parseInt(key), item);
                    } else {
                        final ItemMeta im = item.getItemMeta();
                        final List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<String>();
                        lore.add("");
                        if (shop.contains("Shop.Price." + spage + "." + key + ".BuyPrice")) {
                            lore.add(ColorUtils.applyColor("&bBuy Price : &f" + shop.getInt("Shop.Price." + spage + "." + key + ".BuyPrice")));
                        } else {
                            final double price = shop.getDouble("Shop.Price." + spage + "." + key + ".BuyPrice");
                            if (price == 0.0) {
                                lore.add(ColorUtils.applyColor("&bBuy Price : &cBuy Disabled"));
                            }
                        }
                        if (shop.contains("Shop.Price." + spage + "." + key + ".SellPrice")) {
                            lore.add(ColorUtils.applyColor("&bSell Price : &f" + shop.getInt("Shop.Price." + spage + "." + key + ".SellPrice")));
                        } else {
                            final double price = shop.getDouble("Shop.Price." + spage + "." + key + ".SellPrice");
                            if (price == 0.0) {
                                lore.add(ColorUtils.applyColor("&bSell Price : &cSell Disabled"));
                            }
                        }
                        im.setLore(lore);
                        item.setItemMeta(im);
                        inv.setItem(Integer.parseInt(key), item);
                        pageContent.put(Integer.parseInt(key), item);
                    }
                }
                items.put(Integer.parseInt(spage), pageContent);
            }
        }
        final int maxPages = shop.getInt("Shop.MaxPage");
        ItemStack[] contents = new ItemStack[54];
        inv.setPages(maxPages);
        for (final int page : items.keySet()) {
            final Map<Integer, ItemStack> pageContent2 = items.get(page);
            for (final int slot : pageContent2.keySet()) {
                contents[slot] = pageContent2.get(slot);
            }
            inv.setPageContent(page, contents);
            contents = new ItemStack[54];
        }
        inv.setUsePageTools(true);
        inv.setPageTools(getPageTools(p));
        inv.setCurrentPage(prevPage);
        inv.update();
        p.openInventory(inv.getInventory());
        Shop.currentInv.put(p.getUniqueId(), inv);
    }

    public static void setPrice(final Player p, final String name, final int page, final String sPrice, final int slot) {
        if (!sPrice.contains(":")) {
            p.sendMessage(prefix + plugin.lang.get("price_format_error"));
            p.sendMessage(prefix + plugin.lang.get("price_format_guide"));
            p.sendMessage(prefix + plugin.lang.get("price_format_example"));
            return;
        }
        final double buyPrice = Double.parseDouble(sPrice.split(":")[0]);
        final double sellPrice = Double.parseDouble(sPrice.split(":")[1]);
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        if (buyPrice == 0.0) {
            shop.set("Shop.Price." + page + "." + slot + ".BuyPrice", (Object) null);
        } else {
            shop.set("Shop.Price." + page + "." + slot + ".BuyPrice", (Object) buyPrice);
        }
        if (sellPrice == 0.0) {
            shop.set("Shop.Price." + page + "." + slot + ".SellPrice", (Object) null);
        } else {
            shop.set("Shop.Price." + page + "." + slot + ".SellPrice", (Object) sellPrice);
        }
        ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        p.sendMessage(prefix + name + plugin.lang.get("price_set_success"));
        p.sendMessage(prefix + plugin.lang.get("buy_price_label") + buyPrice);
        p.sendMessage(prefix + plugin.lang.get("sell_price_label") + sellPrice);
        Shop.shops.put(name, shop);
        Bukkit.getScheduler().runTask(plugin, () -> openPriceSettingGUI(p, name, page));
        Shop.isSettingPriceWithChat.remove(p.getUniqueId());
    }

    public static void saveAllShops() {
        for (final String name : Shop.shops.keySet()) {
            final YamlConfiguration shop = Shop.shops.get(name);
            ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        }
        ConfigUtils.savePluginConfig(plugin, plugin.config);
    }

    public static void updatePageTools(final DInventory inv, final Player p) {
        inv.setPageTools(getPageTools(p));
        inv.update();
    }

    public static void openShop(final Player p, final String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        if (getPageTools(p) == null) {
            p.sendMessage(prefix + "page tools is not set, [ /dshop ptget -> /dshop pt ] use this commands to set page tools.");
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        if (shop.isSet("Shop.Permission")) {
            if (!p.hasPermission(shop.getString("Shop.Permission"))) {
                p.sendMessage(prefix + plugin.lang.get("no_shop_permission"));
                return;
            }
        }
        if (shop.contains("Shop.Disabled") && shop.getBoolean("Shop.Disabled")) {
            p.sendMessage(prefix + plugin.lang.get("shop_disabled"));
            return;
        }
        String title = null;
        if (shop.get("Shop.Title") != null) {
            title = ColorUtils.applyColor(shop.getString("Shop.Title"));
        }
        final DInventory inv = new DInventory( title != null ? title : name + " Shop", 54, true, plugin);
        inv.setObj(Triple.of(SettingType.DISPLAY_ITEMS, name, 0));
        final Map<Integer, Map<Integer, ItemStack>> items = new HashMap<>();
        if (shop.getConfigurationSection("Shop.Items") != null) {
            for (final String spage : shop.getConfigurationSection("Shop.Items").getKeys(false)) {
                final Map<Integer, ItemStack> pageContent = new HashMap<>();
                for (final String key : shop.getConfigurationSection("Shop.Items." + spage).getKeys(false)) {
                    final ItemStack item = shop.getItemStack("Shop.Items." + spage + "." + key).clone();
                    final ItemMeta im = item.getItemMeta();
                    final List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<>();
                    lore.add("");
                    lore.add("");
                    lore.add(ColorUtils.applyColor(plugin.lang.get("left_click_info")));
                    final double buyPrice = shop.getDouble("Shop.Price." + spage + "." + key + ".BuyPrice");
                    if (buyPrice == 0.0) {
                        lore.add(ColorUtils.applyColor(plugin.lang.get("buy_price_disabled")));
                    } else {
                        int buyPriceInt = shop.getInt("Shop.Price." + spage + "." + key + ".BuyPrice");
                        int stackPrice = buyPriceInt * item.getMaxStackSize();
                        lore.add(ColorUtils.applyColor(plugin.lang.getWithArgs("buy_price_enabled", String.valueOf(buyPriceInt), String.valueOf(stackPrice))));
                    }
                    lore.add("");
                    lore.add(ColorUtils.applyColor(plugin.lang.get("right_click_info")));
                    final double sellPrice = shop.getDouble("Shop.Price." + spage + "." + key + ".SellPrice");
                    if (sellPrice == 0.0) {
                        lore.add(ColorUtils.applyColor(plugin.lang.get("sell_price_disabled")));
                    } else {
                        int sellPriceInt = shop.getInt("Shop.Price." + spage + "." + key + ".SellPrice");
                        int stackPrice = sellPriceInt * item.getMaxStackSize();
                        lore.add(ColorUtils.applyColor(plugin.lang.getWithArgs("sell_price_enabled", String.valueOf(sellPriceInt), String.valueOf(stackPrice))));
                    }
                    im.setLore(lore);
                    item.setItemMeta(im);
                    inv.setItem(Integer.parseInt(key), item);
                    pageContent.put(Integer.parseInt(key), item);
                }
                items.put(Integer.parseInt(spage), pageContent);
            }
        }
        final int maxPages = shop.getInt("Shop.MaxPage");
        ItemStack[] contents = new ItemStack[54];
        inv.setPages(maxPages);
        for (final int page : items.keySet()) {
            final Map<Integer, ItemStack> pageContent2 = items.get(page);
            for (final int slot : pageContent2.keySet()) {
                contents[slot] = pageContent2.get(slot);
            }
            inv.setPageContent(page, contents);
            contents = new ItemStack[54];
        }
        inv.setUsePageTools(true);
        inv.setPageTools(getPageTools(p));
        inv.setCurrentPage(0);
        p.openInventory(inv.getInventory());
        Shop.currentInv.put(p.getUniqueId(), inv);
        updatePageTools(inv, p);
    }

    public static void buyItem(final Player p, final int slot, final int amount) {
        if (amount <= 0) {
            p.sendMessage(prefix + plugin.lang.get("invalid_quantity"));
            return;
        }

        if (!hasEnoughSpace(p)) {
            p.sendMessage(prefix + plugin.lang.get("inventory_full"));
            return;
        }

        final DInventory inv = Shop.currentInv.get(p.getUniqueId());
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) inv.getObj();

        if (obj.getA() != SettingType.DISPLAY_ITEMS) {
            return;
        }

        final int page = inv.getCurrentPage();
        final String name = obj.getB();

        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }

        final YamlConfiguration shop = Shop.shops.get(name);

        if (!shop.contains("Shop.Price." + page + "." + slot + ".BuyPrice")) {
            p.sendMessage(prefix + plugin.lang.get("item_not_purchasable"));
            return;
        }

        final double price = shop.getDouble("Shop.Price." + page + "." + slot + ".BuyPrice");

        if (price <= 0.0) {
            p.sendMessage(prefix + plugin.lang.get("item_not_purchasable"));
            return;
        }

        final ItemStack item = shop.getItemStack("Shop.Items." + page + "." + slot).clone();
        int maxStack = item.getMaxStackSize();

        if (item.getMaxStackSize() == 1 && amount > 1) {
            p.sendMessage(prefix + plugin.lang.get("single_purchase_only"));
            return;
        }

        double totalPrice = price * amount;

        if (!hasEnoughMoney(p, totalPrice)) {
            p.sendMessage(prefix + plugin.lang.get("insufficient_balance"));
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int toGive = Math.min(remaining, maxStack);
            ItemStack clone = item.clone();
            clone.setAmount(toGive);
            HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(clone);
            if (!leftovers.isEmpty()) {
                p.sendMessage(prefix + plugin.lang.get("inventory_add_failed"));
                return;
            }
            remaining -= toGive;
        }

        takeMoney(p, totalPrice);
        p.sendMessage(prefix + plugin.lang.get("item_purchased"));
        p.sendMessage(prefix + plugin.lang.get("balance_label") + getMoney(p));
        updatePageTools(inv, p);
    }

    public static void sellItem(final Player p, final int slot, final int amount) {
        final DInventory inv = Shop.currentInv.get(p.getUniqueId());
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) inv.getObj();
        if (obj.getA() != SettingType.DISPLAY_ITEMS) {
            return;
        }
        final int page = inv.getCurrentPage();
        final String name = obj.getB();
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        if (!shop.contains("Shop.Price." + page + "." + slot + ".SellPrice")) {
            p.sendMessage(prefix + plugin.lang.get("item_not_for_sale"));
            return;
        }
        final double price = shop.getDouble("Shop.Price." + page + "." + slot + ".SellPrice");
        if (price == 0.0) {
            p.sendMessage(prefix + plugin.lang.get("item_not_for_sale"));
            return;
        }
        final ItemStack item = shop.getItemStack("Shop.Items." + page + "." + slot).clone();
        item.setAmount(amount);
        if (!p.getInventory().containsAtLeast(item, amount)) {
            p.sendMessage(prefix + plugin.lang.get("not_enough_items"));
            return;
        }
        p.getInventory().removeItem(item);
        addMoney(p, price * amount);
        p.sendMessage(prefix + plugin.lang.get("item_sold"));
        p.sendMessage(prefix + plugin.lang.get("balance_label_alt") + getMoney(p));
        inv.update();
        updatePageTools(inv, p);
    }

    public static void sellAllItems(final Player p, int slot) {
        final DInventory inv = Shop.currentInv.get(p.getUniqueId());
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) inv.getObj();
        if (obj.getA() != SettingType.DISPLAY_ITEMS) {
            return;
        }
        final int page = inv.getCurrentPage();
        final String name = obj.getB();
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        if (!shop.contains("Shop.Price." + page + "." + slot + ".SellPrice")) {
            p.sendMessage(prefix + plugin.lang.get("item_not_for_sale"));
            return;
        }
        final double price = shop.getDouble("Shop.Price." + page + "." + slot + ".SellPrice");
        if (price == 0.0) {
            p.sendMessage(prefix + plugin.lang.get("item_not_for_sale"));
            return;
        }
        final ItemStack item = shop.getItemStack("Shop.Items." + page + "." + slot).clone();
        int amount = getSimlarItemCount(p.getInventory().getStorageContents(), item);
        if (amount <= 0) {
            p.sendMessage(prefix + plugin.lang.get("not_enough_items"));
            return;
        }
        item.setAmount(amount);
        p.getInventory().removeItem(item);
        addMoney(p, price * amount);
        p.sendMessage(prefix + plugin.lang.get("item_sold"));
        p.sendMessage(prefix + plugin.lang.get("balance_label_alt") + getMoney(p));
    }

    public static int getSimlarItemCount(ItemStack[] content, ItemStack item) {
        Inventory inv = Bukkit.createInventory(null, 54);
        inv.setContents(content);
        int count = 0;
        for (ItemStack is : inv.getContents()) {
            if (is != null && is.isSimilar(item)) {
                count += is.getAmount();
            }
        }
        return count;
    }

    public static boolean hasEnoughSpace(final Player p) {
        int space = 0;
        for (final ItemStack item : p.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                ++space;
            }
        }
        return space > 0;
    }

    public static void disableShop(final Player p, final String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        shop.set("Shop.Disabled", true);
        ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        Shop.shops.put(name, shop);
        p.sendMessage(prefix + plugin.lang.get("shop_disabled_success"));
    }

    public static void enableShop(final Player p, final String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(name);
        shop.set("Shop.Disabled", false);
        ConfigUtils.saveCustomData(plugin, shop, name, "shops");
        Shop.shops.put(name, shop);
        p.sendMessage(prefix + plugin.lang.get("shop_enabled_success"));
    }

    public static void deleteShop(final Player p, final String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        new File(plugin.getDataFolder() + "/shops/" + name + ".yml").delete();
        Shop.shops.remove(name);
        p.sendMessage(prefix + plugin.lang.get("shop_deleted_success"));
    }

    public static boolean hasEnoughMoney(final Player p, final double amount) {
        return MoneyAPI.hasEnoughMoney(p, amount);
    }

    public static void takeMoney(final Player p, final double amount) {
        MoneyAPI.takeMoney(p, amount);
    }

    public static void addMoney(final Player p, final double amount) {
        MoneyAPI.addMoney(p, amount);
    }

    public static BigDecimal getMoney(final Player p) {
        return MoneyAPI.getMoney(p);
    }

    public static void setMaxPage(final Player p, final String arg, final int page) {
        if (!Shop.shops.containsKey(arg)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        final YamlConfiguration shop = Shop.shops.get(arg);
        shop.set("Shop.MaxPage", page);
        ConfigUtils.saveCustomData(plugin, shop, arg, "shops");
        Shop.shops.put(arg, shop);
        p.sendMessage(prefix + plugin.lang.get("max_page_set"));
    }

    public static void setPermission(Player p, String name, String permission) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        YamlConfiguration shop = Shop.shops.get(name);
        shop.set("Shop.Permission", permission);
        p.sendMessage(prefix + name + plugin.lang.get("permission_set") + permission);
        saveAllShops();
    }

    public static void delPermission(Player p, String name) {
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        YamlConfiguration shop = Shop.shops.get(name);
        shop.set("Shop.Permission", null);
        saveAllShops();
        p.sendMessage(prefix + name + plugin.lang.get("permission_deleted"));
    }

    public static void setTitle(Player p, String name, String[] args) {
        args = Arrays.copyOfRange(args, 2, args.length);
        if (!Shop.shops.containsKey(name)) {
            p.sendMessage(prefix + plugin.lang.get("shop_not_exist"));
            return;
        }
        YamlConfiguration shop = Shop.shops.get(name);
        String title = "";
        for (String arg : args) {
            title += arg + " ";
        }
        title = title.trim();
        shop.set("Shop.Title", title);
        saveAllShops();
        p.sendMessage(prefix + name + plugin.lang.get("title_set") + title);
    }

    public static void openPageToolSetting(Player p) {
        DInventory inv = new DInventory( "Page Tool Settings", 9, plugin);
        inv.setObj(Triple.of(SettingType.SETTING_PT, "global", 0));
        if (Shop.config.isSet("Settings.PT")) {
            for (String key : Shop.config.getConfigurationSection("Settings.PT").getKeys(false)) {
                ItemStack item = Shop.config.getItemStack("Settings.PT." + key);
                inv.setItem(Integer.parseInt(key), item);
            }
        }
        Shop.currentInv.put(p.getUniqueId(), inv);
        p.openInventory(inv.getInventory());
    }

    public static void savePageTool(Player p, DInventory inv, String name) {
        for (int i = 0; i < 9; i++) {
            Shop.config.set("Settings.PT." + i, inv.getItem(i));
        }
        Shop.currentInv.remove(p.getUniqueId());
        ConfigUtils.savePluginConfig(plugin, plugin.config);
    }

    @Nullable
    public static ItemStack[] getPageTools(Player p) {
        if (Shop.config.isSet("Settings.PT")) {
            ItemStack[] pt = new ItemStack[9];
            for (String key : Shop.config.getConfigurationSection("Settings.PT").getKeys(false)) {
                ItemStack item = Shop.config.getItemStack("Settings.PT." + key).clone();
                if (item.hasItemMeta()) {
                    ItemMeta im = item.getItemMeta();
                    im.setDisplayName(PlaceholderAPI.setPlaceholders(p, im.getDisplayName()));
                    List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<>();
                    im.setLore(PlaceholderAPI.setPlaceholders(p, lore));
                    item.setItemMeta(im);
                }
                pt[Integer.parseInt(key)] = item;
            }
            return pt;
        }
        return null;
    }

    public static void giveDefaultPageTools(Player p) {
        ItemStack prev = new ItemStack(Material.ARROW);
        final ItemMeta prevm = prev.getItemMeta();
        prevm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        prevm.setDisplayName("§aPrevious Page");
        prev.setItemMeta(prevm);
        prev = NBT.setStringTag(prev, "dlss_prev", "true");
        ItemStack next = new ItemStack(Material.ARROW);
        final ItemMeta nextm = next.getItemMeta();
        nextm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        nextm.setDisplayName("§bNext Page");
        next.setItemMeta(nextm);
        next = NBT.setStringTag(next, "dlss_next", "true");
        ItemStack page = new ItemStack(Material.PAPER);
        final ItemMeta pagem = page.getItemMeta();
        pagem.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        pagem.setDisplayName("§f");
        pagem.setLore(Arrays.asList("§f", "§eCurrent Balance: §b%vault_eco_balance%"));
        page.setItemMeta(pagem);
        page = NBT.setStringTag(page, "dlss_page", "true");
        ItemStack glass = new ItemStack(Material.GLASS_PANE, 1, (short) 7);
        final ItemMeta glassm = glass.getItemMeta();
        glassm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        glassm.setDisplayName("§f");
        glass.setItemMeta(glassm);
        glass = NBT.setStringTag(glass, "dlss_display", "true");
        p.getInventory().addItem(prev, next, page, glass);
        p.sendMessage(prefix + "you received default page tools, you can edit the item type, name, lore.");
    }
}