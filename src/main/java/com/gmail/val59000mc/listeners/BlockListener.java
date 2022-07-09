package com.gmail.val59000mc.listeners;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.LootConfiguration;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.UhcItems;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.RandomUtils;
import com.gmail.val59000mc.utils.TimeUtils;
import com.gmail.val59000mc.utils.UniversalMaterial;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockListener implements Listener{

	private final GameManager gameManager;
	private final PlayerManager playerManager;
	private final MainConfig configuration;
	private final Map<Material, LootConfiguration<Material>> blockLoots;
	private final int maxBuildingHeight;
	private final int maxDmBuildingHeight;
	private final Map<Location, Long> blockLastPlaced = new HashMap<>();
	
	public BlockListener(GameManager gameManager){
		this.gameManager = gameManager;
		playerManager = gameManager.getPlayerManager();
		configuration = gameManager.getConfig();
		blockLoots = configuration.get(MainConfig.ENABLE_BLOCK_LOOT) ? configuration.get(MainConfig.BLOCK_LOOT) : new HashMap<>();
		maxBuildingHeight = configuration.get(MainConfig.MAX_BUILDING_HEIGHT);
		maxDmBuildingHeight = configuration.get(MainConfig.MAX_DM_BUILDING_HEIGHT);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		handleBlockLoot(event);
		handleShearedLeaves(event);
		handleFrozenPlayers(event);
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			handleMapProtection(event);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		handleMaxBuildingHeight(event);
		handleFrozenPlayers(event);
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			handleDeathMatchPlace(event);
		}
	}

	@EventHandler
	public void onMultiPlace(BlockMultiPlaceEvent event) {
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			handleDeathMatchMultiPlace(event);
		}
	}

	@EventHandler
	public void onLiquidPlace(PlayerBucketEmptyEvent event){
		handleFrozenPlayers(event);
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			handleDeathMatchBucketEmpty(event);
		}
	}

	@EventHandler
	public void onPistonExtendEvent(BlockPistonExtendEvent event){
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBurnEvent(BlockBurnEvent event){
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockExplodeEvent(BlockExplodeEvent event){
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			event.setYield(0);
		}
	}

	@EventHandler
	public void onTreeGrowEvent(StructureGrowEvent event){
		if(gameManager.getGameState().equals(GameState.DEATHMATCH)){
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockFromToEvent(BlockFromToEvent event) {
		if (gameManager.getGameState().equals(GameState.DEATHMATCH)) {
			handleDeathMatchBlockFromTo(event);
		}
	}

	private void handleDeathMatchBlockFromTo(BlockFromToEvent event){
		Block block = event.getBlock();
		Block toBlock = event.getToBlock();
		blockLastPlaced.put(toBlock.getLocation(), 1L);
		blockLastPlaced.put(block.getLocation(), 1L);

		if(!block.isLiquid()){
			return;
		}

		Block north = toBlock.getRelative(BlockFace.NORTH);
		Block east = toBlock.getRelative(BlockFace.EAST);
		Block south = toBlock.getRelative(BlockFace.SOUTH);
		Block west = toBlock.getRelative(BlockFace.WEST);
		Block down = toBlock.getRelative(BlockFace.DOWN);

		if(north.getType()!= block.getType() && north.isLiquid() && !north.getType().isInteractable()){
			blockLastPlaced.put(north.getLocation(),1L);
		}
		if(east.getType()!= block.getType() && east.isLiquid() && !east.getType().isInteractable()){
			blockLastPlaced.put(east.getLocation(),1L);
		}
		if(south.getType()!= block.getType() && south.isLiquid() && !south.getType().isInteractable()){
			blockLastPlaced.put(south.getLocation(),1L);
		}
		if(west.getType()!= block.getType() && west.isLiquid() && !west.getType().isInteractable()){
			blockLastPlaced.put(west.getLocation(),1L);
		}
		if(down.getType()!= block.getType() && down.isLiquid() && !down.getType().isInteractable()){
			blockLastPlaced.put(down.getLocation(),1L);
		}
		/*if (!block.isLiquid()){
			return;
		}

		if (block.getType()!=toBlock.getType()){
			if (toBlock.isLiquid()) {
				blockLastPlaced.put(toBlock.getLocation(), 1L);
				blockLastPlaced.put(block.getLocation(), 1L);
			}else if(toBlock.getRelative(BlockFace.NORTH).getType() != block.getType() ||
					toBlock.getRelative(BlockFace.EAST).getType() != block.getType() ||
					toBlock.getRelative(BlockFace.SOUTH).getType() != block.getType() ||
					toBlock.getRelative(BlockFace.WEST).getType() != block.getType() ||
					toBlock.getRelative(BlockFace.DOWN).getType() != block.getType() ||
					toBlock.getRelative(BlockFace.UP).getType() != block.getType()){
				blockLastPlaced.put(block.getLocation(), 1L);
				blockLastPlaced.put(toBlock.getLocation(), 1L);
			}
		}*/
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event){
		handleAppleDrops(event);
	}

	private void handleMaxBuildingHeight(BlockPlaceEvent e){
		if (maxBuildingHeight < 0 || e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

		if (e.getBlock().getY() > maxBuildingHeight){
			e.setCancelled(true);
			e.getPlayer().sendMessage(Lang.PLAYERS_BUILD_HEIGHT);
		}
	}

	private void handleBlockLoot(BlockBreakEvent event){
		Material material = event.getBlock().getType();
		if(blockLoots.containsKey(material)){
			LootConfiguration<Material> lootConfig = blockLoots.get(material);
			Location loc = event.getBlock().getLocation().add(.5,.5,.5);

			event.getBlock().setType(Material.AIR);
			event.setExpToDrop(lootConfig.getAddXp());

			lootConfig.getLoot().forEach(item -> loc.getWorld().dropItem(loc, item.clone()));

			if (lootConfig.getAddXp() > 0) {
				UhcItems.spawnExtraXp(loc, lootConfig.getAddXp());
			}
		}
	}

	private void handleShearedLeaves(BlockBreakEvent e){
		if (!configuration.get(MainConfig.APPLE_DROPS_FROM_SHEARING)){
			return;
		}

		if (!UniversalMaterial.isLeaves(e.getBlock().getType())){
			return;
		}

		if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS){
			Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(e.getBlock()));
		}
	}

	private void handleFrozenPlayers(BlockBreakEvent e){
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(e.getPlayer());
		if (uhcPlayer.isFrozen()){
			e.setCancelled(true);
		}
	}

	private void handleFrozenPlayers(BlockPlaceEvent e){
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(e.getPlayer());
		if (uhcPlayer.isFrozen()){
			e.setCancelled(true);
		}
	}

	private void handleFrozenPlayers(PlayerBucketEmptyEvent e){
		UhcPlayer uhcPlayer = playerManager.getUhcPlayer(e.getPlayer());
		if (uhcPlayer.isFrozen()){
			e.setCancelled(true);
		}
	}

	private void handleAppleDrops(LeavesDecayEvent e){
		Block block = e.getBlock();
		Material type = block.getType();
		boolean isOak;

		if (configuration.get(MainConfig.APPLE_DROPS_FROM_ALL_TREES)){
			if (type != UniversalMaterial.OAK_LEAVES.getType()) {
				e.getBlock().setType(UniversalMaterial.OAK_LEAVES.getType());
			}
			isOak = true;
		}else {
			isOak = type == UniversalMaterial.OAK_LEAVES.getType() || type == UniversalMaterial.DARK_OAK_LEAVES.getType();
		}

		if (!isOak){
			return; // Will never drop apples so drops don't need to increase
		}

		double percentage = configuration.get(MainConfig.APPLE_DROP_PERCENTAGE)-0.5;

		if (percentage <= 0){
			return; // No added drops
		}

		// Number 0-100
		double random = RandomUtils.randomInteger(0, 200)/2D;

		if (random > percentage){
			return; // Number above percentage so no extra apples.
		}

		// Add apple to drops
		Bukkit.getScheduler().runTask(UhcCore.getPlugin(), () -> block.getWorld().dropItem(block.getLocation().add(.5, .5, .5), new ItemStack(Material.APPLE)));
	}


	private void handleMapProtection(BlockBreakEvent e){
		if(blockLastPlaced.getOrDefault(e.getBlock().getLocation(),-1L)<0){
			e.setCancelled(true);
		}
	}

	private void handleDeathMatchBucketEmpty(PlayerBucketEmptyEvent event){
		Block block = event.getBlock();
		if (block.getType().isInteractable()){
			handleDeathMatchBlockClear(block.getLocation());
		}else {
			blockLastPlaced.put(block.getLocation(), 1L);
		}

		if (block.getY() > maxDmBuildingHeight){
			event.setCancelled(true);
			event.getPlayer().sendMessage(Lang.PLAYERS_BUILD_HEIGHT);
			return;
		}

		if(!block.getType().isInteractable()){

			Block north = block.getRelative(BlockFace.NORTH);
			Block east = block.getRelative(BlockFace.EAST);
			Block south = block.getRelative(BlockFace.SOUTH);
			Block west = block.getRelative(BlockFace.WEST);

			if(north.isLiquid() && !north.getType().isInteractable()){
				blockLastPlaced.put(north.getLocation(),1L);
			}
			if(east.isLiquid() && !east.getType().isInteractable()){
				blockLastPlaced.put(east.getLocation(),1L);
			}
			if(south.isLiquid() && !south.getType().isInteractable()){
				blockLastPlaced.put(south.getLocation(),1L);
			}
			if(west.isLiquid() && !west.getType().isInteractable()){
				blockLastPlaced.put(west.getLocation(),1L);
			}
		}
	}

	private void handleDeathMatchPlace(BlockPlaceEvent e){
		Block block = e.getBlock();

		if(block.getType().hasGravity()){
			e.setCancelled(true);
			return;
		}

		if (block.getY() > maxDmBuildingHeight){
			e.setCancelled(true);
			e.getPlayer().sendMessage(Lang.PLAYERS_BUILD_HEIGHT);
			return;
		}

		if(block.getType().equals(Material.TNT)){
			block.setType(Material.AIR);
			block.getWorld().createExplosion(block.getLocation().add(0.5,0.5,0.5),4,false,false,e.getPlayer());
			return;
		}

		handleDeathMatchBlockClear(block.getLocation());
	}

	private void handleDeathMatchMultiPlace(BlockMultiPlaceEvent e){

		if (e.getBlock().getY()+1 > maxDmBuildingHeight){
			e.setCancelled(true);
			e.getPlayer().sendMessage(Lang.PLAYERS_BUILD_HEIGHT);
			return;
		}

		e.getReplacedBlockStates().forEach(s -> handleDeathMatchBlockClear(s.getLocation()));
	}

	private void handleDeathMatchBlockClear(Location loc){
		blockLastPlaced.put(loc,System.currentTimeMillis());

		new BukkitRunnable() {
			@Override
			public void run() {
				if(blockLastPlaced.get(loc)+10*TimeUtils.SECOND-50L <= System.currentTimeMillis()
						&& blockLastPlaced.get(loc)>1){
					loc.getBlock().setType(Material.AIR);
				}
			}
		}.runTaskLater(UhcCore.getPlugin(), 200);
	}
}