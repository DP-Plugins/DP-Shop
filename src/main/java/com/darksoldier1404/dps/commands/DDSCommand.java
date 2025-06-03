package com.darksoldier1404.dps.commands;

import com.darksoldier1404.dppc.lang.DLang;
import com.darksoldier1404.dps.Shop;
import com.darksoldier1404.dps.functions.DDSFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class DDSCommand implements CommandExecutor, TabExecutor {
    private static final Shop plugin = Shop.getInstance();
    private static final String prefix = Shop.prefix;
    private static final DLang lang = plugin.lang;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + lang.get("player_only"));
            return false;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            if (p.isOp()) {
                p.sendMessage(prefix + lang.get("help_create"));
                p.sendMessage(prefix + lang.get("help_ptget"));
                p.sendMessage(prefix + lang.get("help_pt"));
                p.sendMessage(prefix + lang.get("help_pages"));
                p.sendMessage(prefix + lang.get("help_title"));
                p.sendMessage(prefix + lang.get("help_items"));
                p.sendMessage(prefix + lang.get("page_start_zero"));
                p.sendMessage(prefix + lang.get("help_price"));
                p.sendMessage(prefix + lang.get("help_disable"));
                p.sendMessage(prefix + lang.get("help_enable"));
                p.sendMessage(prefix + lang.get("help_delete"));
                p.sendMessage(prefix + lang.get("help_reload"));
                p.sendMessage(prefix + lang.get("help_permission"));
                p.sendMessage(prefix + lang.get("help_delpermission"));
            }
            p.sendMessage(prefix + lang.get("help_open"));
            return false;
        }
        if (p.isOp()) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_create"));
                    return false;
                }
                DDSFunction.createShop(p, args[1]);
                return false;
            }
            if( args[0].equalsIgnoreCase("ptget")) {
                DDSFunction.giveDefaultPageTools(p);
                return false;
            }
            if( args[0].equalsIgnoreCase("pt")) {
                DDSFunction.openPageToolSetting(p);
                return false;
            }
            if( args[0].equalsIgnoreCase("title")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_title"));
                    return false;
                }
                if (args.length == 2) {
                    p.sendMessage(prefix + lang.get("usage_title_2"));
                    return false;
                }
                DDSFunction.setTitle(p, args[1], args);
                return false;
            }
            if (args[0].equalsIgnoreCase("pages")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_pages"));
                    return false;
                }
                DDSFunction.setMaxPage(p, args[1], Integer.parseInt(args[2]));
                return false;
            } else if (args[0].equalsIgnoreCase("items")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_items"));
                    return false;
                }
                if (args.length == 2) {
                    DDSFunction.openItemSettingGUI(p, args[1], 0);
                    return false;
                }
                DDSFunction.openItemSettingGUI(p, args[1], Integer.parseInt(args[2]));
                return false;
            } else if (args[0].equalsIgnoreCase("price")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_price"));
                    return false;
                }
                if (args.length == 2) {
                    DDSFunction.openPriceSettingGUI(p, args[1], 0);
                    return false;
                }
                DDSFunction.openPriceSettingGUI(p, args[1], 0);
                return false;
            } else if (args[0].equalsIgnoreCase("enable")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_enable"));
                    return false;
                }
                DDSFunction.enableShop(p, args[1]);
                return false;
            } else if (args[0].equalsIgnoreCase("disable")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_disable"));
                    return false;
                }
                DDSFunction.disableShop(p, args[1]);
                return false;
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("usage_delete"));
                    return false;
                }
                DDSFunction.deleteShop(p, args[1]);
                return false;
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    DDSFunction.reloadConfig(p);
                    p.sendMessage(prefix + lang.get("config_reloaded"));
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("permission")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("enter_shop_name"));
                    return false;
                }
                if (args.length == 2) {
                    p.sendMessage(prefix + lang.get("enter_permission_node"));
                    return false;
                }
                DDSFunction.setPermission(p, args[1], args[2]);
                return false;
            }
            if (args[0].equalsIgnoreCase("delpermission")) {
                if (args.length == 1) {
                    p.sendMessage(prefix + lang.get("enter_shop_name"));
                    return false;
                }
                DDSFunction.delPermission(p, args[1]);
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("open")) {
            if (args.length == 1) {
                p.sendMessage(prefix + lang.get("help_open"));
                return false;
            }
            DDSFunction.openShop(p, args[1]);
        }
        return false;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1) {
            if (sender.isOp()) {
                return Arrays.asList("create", "ptget", "pt", "title", "items", "price", "disable", "enable", "delete", "open", "reload", "permission", "delpermission", "pages");
            }
            return Arrays.asList("open");
        } else {
            if (args.length == 2) {
                return new ArrayList<>(Shop.shops.keySet());
            }
            return null;
        }
    }
}