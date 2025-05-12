package com.darksoldier1404.dps;


import com.darksoldier1404.dppc.action.helper.ActionGUIHandler;
import com.darksoldier1404.dppc.lang.DLang;
import com.darksoldier1404.dppc.utils.PluginUtil;
import com.darksoldier1404.dps.commands.DDSCommand;
import com.darksoldier1404.dps.events.DDSEvent;
import com.darksoldier1404.dps.functions.DDSFunction;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.Quadruple;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shop extends JavaPlugin
{
    public static Shop plugin;
    public static YamlConfiguration config;
    public static Map<String, YamlConfiguration> shops = new HashMap<>();
    public static String prefix;
    public static Map<UUID, DInventory> currentInv = new HashMap<>();
    public static Map<UUID, Quadruple<String, Integer, String, Integer>> isSettingPriceWithChat = new HashMap<>();
    public static DLang lang;

    public static Shop getInstance() {
        return plugin;
    }

    @Override
    public void onLoad() {
        plugin = this;
        PluginUtil.addPlugin(plugin, 25819);
    }

    public void onEnable() {
        DDSFunction.initConfig();
        lang = new DLang(config.getString("Settings.Lang") == null ? "Korean" : config.getString("Settings.Lang"), plugin);
        getCommand("dshop").setExecutor(new DDSCommand());
        getServer().getPluginManager().registerEvents(new DDSEvent(), plugin);
    }

    public void onDisable() {
        DDSFunction.saveAllShops();
    }
}
