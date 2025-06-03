package com.darksoldier1404.dps.events;

import com.darksoldier1404.dps.Shop;
import com.darksoldier1404.dps.enums.SettingType;
import com.darksoldier1404.dps.functions.DDSFunction;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.NBT;
import com.darksoldier1404.dppc.utils.Quadruple;
import com.darksoldier1404.dppc.utils.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DDSEvent implements Listener {

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        final Player p = (Player) e.getPlayer();
        if (!Shop.currentInv.containsKey(p.getUniqueId())) {
            return;
        }
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) Shop.currentInv.get(p.getUniqueId()).getObj();
        switch (obj.a) {
            case SETTING_ITEMS: {
                DDSFunction.saveShopItems(p, obj.b);
                break;
            }
            case SETTING_PT: {
                DDSFunction.savePageTool(p, Shop.currentInv.get(p.getUniqueId()), obj.b);
                break;
            }
            case SETTING_PRICE:
            case DISPLAY_ITEMS: {
                Shop.currentInv.remove(p.getUniqueId());
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        if (!Shop.currentInv.containsKey(p.getUniqueId())) {
            return;
        }
        final DInventory di = Shop.currentInv.get(p.getUniqueId());
        final Triple<SettingType, String, Integer> obj = (Triple<SettingType, String, Integer>) di.getObj();
        switch (obj.a) {
            case DISPLAY_ITEMS: {
                e.setCancelled(true);
                if (e.getCurrentItem() == null) {
                    return;
                }
                if (e.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                if (e.getRawSlot() >= 54) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_display")) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_page")) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_prev")) {
                    di.prevPage();
                    DDSFunction.updatePageTools(di, p);
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_next")) {
                    di.nextPage();
                    DDSFunction.updatePageTools(di, p);
                    return;
                }
                if (e.getClick() == ClickType.LEFT) {
                    DDSFunction.buyItem(p, e.getRawSlot(), 1);
                    return;
                }
                if (e.getClick() == ClickType.RIGHT) {
                    DDSFunction.sellItem(p, e.getRawSlot(), 1);
                    return;
                }
                if (e.getClick() == ClickType.SHIFT_LEFT) {
                    DDSFunction.buyItem(p, e.getRawSlot(), 64);
                    return;
                }
                if (e.getClick() == ClickType.SHIFT_RIGHT) {
                    DDSFunction.sellItem(p, e.getRawSlot(), 64);
                    return;
                }
                if (e.getClick() == ClickType.MIDDLE) {
                    DDSFunction.sellAllItems(p, e.getRawSlot());
                    return;
                }
                break;
            }
            case SETTING_PRICE: {
                e.setCancelled(true);
                if (e.getCurrentItem() == null) {
                    return;
                }
                if (e.getCurrentItem().getType() == Material.AIR) {
                    return;
                }
                if (e.getRawSlot() >= 54) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_display")) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_page")) {
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_prev")) {
                    di.prevPage();
                    return;
                }
                if (NBT.hasTagKey(e.getCurrentItem(), "dlss_next")) {
                    di.nextPage();
                    return;
                }
                Shop.isSettingPriceWithChat.put(p.getUniqueId(), Quadruple.of(obj.b, di.getCurrentPage(), null, e.getRawSlot()));
                p.closeInventory();
                break;
            }
        }
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        if (!Shop.isSettingPriceWithChat.containsKey(p.getUniqueId())) {
            return;
        }
        e.setCancelled(true);
        final Quadruple<String, Integer, String, Integer> obj = Shop.isSettingPriceWithChat.get(p.getUniqueId());
        obj.setC(e.getMessage());
        DDSFunction.setPrice(p, obj.getA(), obj.getB(), obj.getC(), obj.getD());
    }
}
