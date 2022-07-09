package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CheckArmorThread implements Runnable{

    private final Player player;

    static Map<Player, Long> armorLastChange = new HashMap<>();

    public CheckArmorThread(Player player){
        this.player = player;
    }

    @Override
    public void run() {
        armorLastChange.put(player,System.currentTimeMillis());

        ItemStack[] itemStacks = player.getInventory().getArmorContents();

        if(itemStacks[0]!=null
                && itemStacks[0].getItemMeta()!=null
                && itemStacks[0].getItemMeta().getLore()!=null
                && itemStacks[0].getItemMeta().getLore().contains(Lang.ITEMS_FUSION_ARMOR)
        && itemStacks[1]!=null
                && itemStacks[1].getItemMeta()!=null
                && itemStacks[1].getItemMeta().getLore()!=null
                && itemStacks[1].getItemMeta().getLore().contains(Lang.ITEMS_FUSION_ARMOR)
        && itemStacks[2]!=null
                && itemStacks[2].getItemMeta()!=null
                && itemStacks[2].getItemMeta().getLore()!=null
                && itemStacks[2].getItemMeta().getLore().contains(Lang.ITEMS_FUSION_ARMOR)
        && itemStacks[3]!=null
                && itemStacks[3].getItemMeta()!=null
                && itemStacks[3].getItemMeta().getLore()!=null
                && itemStacks[3].getItemMeta().getLore().contains(Lang.ITEMS_FUSION_ARMOR)){
            handleFusion(player, armorLastChange.getOrDefault(player, -1L));
            return;
        }

        if(itemStacks[2]!=null && itemStacks[2].getItemMeta()!=null
        && itemStacks[2].getItemMeta().getLore()!=null
        && itemStacks[2].getItemMeta().getLore().contains(Lang.ITEMS_BARBARIAN)){
            handleBarbarian(player, armorLastChange.getOrDefault(player, -1L));
        }
    }

    public static void handleFusion(Player player, Long time){
        new BukkitRunnable(){
            public void run(){
                if(!time.equals(armorLastChange.get(player))){
                    this.cancel();
                }else{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 39, 0));
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,10);
    }

    public static void handleBarbarian(Player player, Long time){
        new BukkitRunnable(){
            public void run(){
                if(!time.equals(armorLastChange.get(player))){
                    this.cancel();
                }else{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 39, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 39, 0));
                }
            }
        }.runTaskTimer(UhcCore.getPlugin(UhcCore.class),0,10);
    }
}

