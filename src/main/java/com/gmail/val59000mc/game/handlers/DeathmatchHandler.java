package com.gmail.val59000mc.game.handlers;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.maploader.MapLoader;
import com.gmail.val59000mc.players.PlayerState;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.schematics.DeathmatchArena;
import com.gmail.val59000mc.threads.StartDeathmatchThread;
import com.gmail.val59000mc.utils.LocationUtils;
import com.gmail.val59000mc.utils.UniversalSound;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.AbstractMap;
import java.util.List;
import java.util.stream.IntStream;

import static com.gmail.val59000mc.maploader.MapLoader.DO_MOB_SPAWNING;

public class DeathmatchHandler {

    private final GameManager gameManager;
    private final MainConfig config;
    private final PlayerManager playerManager;
    private final MapLoader mapLoader;
    private final CustomEventHandler customEventHandler;

    public DeathmatchHandler(GameManager gameManager, MainConfig config, PlayerManager playerManager, MapLoader mapLoader, CustomEventHandler customEventHandler) {
        this.gameManager = gameManager;
        this.config = config;
        this.playerManager = playerManager;
        this.mapLoader = mapLoader;
        this.customEventHandler = customEventHandler;
    }

    public void startDeathmatch() {
        // DeathMatch can only be stated while GameState = Playing
        if (gameManager.getGameState() != GameState.PLAYING){
            return;
        }

        gameManager.setGameState(GameState.DEATHMATCH);
        gameManager.setPvp(false);
        gameManager.broadcastInfoMessage(Lang.GAME_START_DEATHMATCH);
        playerManager.playSoundToAll(UniversalSound.ENDERDRAGON_GROWL);

        DeathmatchArena arena = mapLoader.getArena();
        if (arena.isUsed()) {
            startArenaDeathmatch(arena);
        }
        else{
            startCenterDeathmatch();
        }
    }

    private void startArenaDeathmatch(DeathmatchArena arena) {
        Location arenaLocation = arena.getLocation();

        //Set big border size to avoid hurting players
        mapLoader.setBorderSize(arenaLocation.getWorld(), arenaLocation.getBlockX(), arenaLocation.getBlockZ(), 50000);

        //Set Game Rule
        World overworld =  mapLoader.getUhcWorld(World.Environment.NORMAL);
        VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doMobSpawning", false);
        VersionUtils.getVersionUtils().setGameRuleValue(overworld, "doFireTick", false);

        // Teleport players
        List<Location> spots = arena.getTeleportSpots();
        int spotIndex = 0;
        for (UhcTeam teams : playerManager.listUhcTeams()) {
            teleportTeam(teams, spots.get(spotIndex), arenaLocation);

            if (teams.getPlayingMemberCount() != 0) {
                spotIndex++;
                if (spotIndex == spots.size()) {
                    spotIndex = 0;
                }
            }
        }
        // Shrink border to arena size
        mapLoader.setBorderSize(mapLoader.getUhcWorld(World.Environment.NORMAL), 10000.5, 10000.5, 200);

        // Shrink border to arena size
        //mapLoader.setBorderSize(arenaLocation.getWorld(), arenaLocation.getBlockX(), arenaLocation.getBlockZ(), arena.getMaxSize());

        // Start Enable pvp thread
        Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StartDeathmatchThread(gameManager, true, customEventHandler), 20);
    }

    private void startCenterDeathmatch() {
        //Set big border size to avoid hurting players
        mapLoader.setBorderSize(mapLoader.getUhcWorld(World.Environment.NORMAL), 0, 0, 50000);

        // Teleport players
        Location spectatingLocation = new Location(mapLoader.getUhcWorld(World.Environment.NORMAL), 0, 100, 0);
        for (UhcTeam team : playerManager.listUhcTeams()) {
            Location teleportSpot = LocationUtils.findRandomSafeLocation(mapLoader.getUhcWorld(World.Environment.NORMAL), config.get(MainConfig.DEATHMATCH_START_SIZE) - 10);
            teleportTeam(team, teleportSpot, spectatingLocation);
        }

        // Shrink border to arena size
        mapLoader.setBorderSize(mapLoader.getUhcWorld(World.Environment.NORMAL), 0, 0, config.get(MainConfig.DEATHMATCH_START_SIZE)*2);

        // Start Enable pvp thread
        Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StartDeathmatchThread(gameManager, true, customEventHandler), 20);
    }

    private void teleportTeam(UhcTeam team, Location spawnLocation, Location spectateLocation) {
        for (UhcPlayer player : team.getMembers()) {
            Player bukkitPlayer;
            try {
                bukkitPlayer = player.getPlayer();
            } catch (UhcPlayerNotOnlineException e) {
                continue; // Ignore offline players
            }

            if (player.getState().equals(PlayerState.PLAYING)) {
                if (config.get(MainConfig.DEATHMATCH_ADVENTURE_MODE)) {
                    bukkitPlayer.setGameMode(GameMode.ADVENTURE);
                } else {
                    bukkitPlayer.setGameMode(GameMode.SURVIVAL);
                }

                smeltGold(bukkitPlayer.getInventory());
                /*for (int i=0;i< bukkitPlayer.getInventory().getSize();i++){
                    if (bukkitPlayer.getInventory().getItem(i) != null
                            &&( bukkitPlayer.getInventory().getItem(i).getType().equals(Material.RAW_GOLD)
                    || bukkitPlayer.getInventory().getItem(i).getType().equals(Material.GOLD_ORE))){
                        ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
                        goldIngot.setAmount(bukkitPlayer.getInventory().getItem(i).getAmount());
                        bukkitPlayer.getInventory().setItem(i,goldIngot);
                    }
                    if (bukkitPlayer.getInventory().getItem(i) != null
                            &&( bukkitPlayer.getInventory().getItem(i).getType().equals(Material.SHULKER_BOX))){
                        if (bukkitPlayer.getInventory().getItem(i).getItemMeta()==null) return;
                        BlockStateMeta itemMeta = (BlockStateMeta)bukkitPlayer.getInventory().getItem(i).getItemMeta();
                        ShulkerBox shulker = (ShulkerBox) itemMeta.getBlockState();
                        for (int j=0;j< shulker.getInventory().getSize();j++){
                            if (shulker.getInventory().getItem(j) != null&&
                                    (shulker.getInventory().getItem(j).getType().equals(Material.RAW_GOLD)
                                            || shulker.getInventory().getItem(j).getType().equals(Material.GOLD_ORE))){
                                ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT);
                                goldIngot.setAmount(shulker.getInventory().getItem(j).getAmount());
                                shulker.getInventory().setItem(j,goldIngot);
                            }
                        }

                        itemMeta.setBlockState(shulker);
                        bukkitPlayer.getInventory().getItem(i).setItemMeta(itemMeta);
                    }
                }*/

                player.freezePlayer(spawnLocation);
                spectateLocation.setY(spawnLocation.getY());
                org.bukkit.util.Vector vectorAB = spectateLocation.clone().subtract(spawnLocation).toVector();
                vectorAB.normalize();
                spawnLocation.setDirection(vectorAB);
                bukkitPlayer.teleport(spawnLocation);
            } else {
                bukkitPlayer.teleport(spectateLocation);
            }
        }
    }

    private void smeltGold(Inventory inventory) {
        ItemStack[] stacks = inventory.getContents();
        IntStream.range(0, stacks.length).boxed().map(i -> new AbstractMap.SimpleEntry<Integer, ItemStack>(i, stacks[i]))
                .filter(entry -> entry.getValue() != null &&
                        (entry.getValue().getType() == Material.RAW_GOLD || entry.getValue().getType() == Material.GOLD_ORE))
                .forEach(entry -> {
                    entry.getValue().setType(Material.GOLD_INGOT);
                    inventory.setItem(entry.getKey(), entry.getValue());
                });
        IntStream.range(0, stacks.length).boxed().map(i -> new AbstractMap.SimpleEntry<Integer, ItemStack>(i, stacks[i]))
                .filter(entry -> entry.getValue() != null &&
                        (Tag.SHULKER_BOXES.isTagged(entry.getValue().getType())))
                .forEach(entry -> {
                    BlockStateMeta meta = (BlockStateMeta) entry.getValue().getItemMeta();
                    assert meta != null;
                    ShulkerBox box = (ShulkerBox) meta.getBlockState();
                    smeltGold(box.getInventory());
                    meta.setBlockState(box);
                    entry.getValue().setItemMeta(meta);
                    inventory.setItem(entry.getKey(), entry.getValue());
                });
    }

}
