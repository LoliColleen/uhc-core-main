package com.gmail.val59000mc.customitems;

import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.UniversalMaterial;
import com.gmail.val59000mc.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

import static org.bukkit.Bukkit.broadcastMessage;

public class Craft {

	private final String name;
	private final List<ItemStack> recipe;
	private final ItemStack displayItem, craft;
	private final int limit;
	private final boolean shapeless;
	private final boolean unbreakable;
	private final Set<OnCraftListener> onCraftListeners;
	private final Set<OnConsumeListener> onConsumeListeners;
	Material[] itemStacks;

	public Craft(String name, List<ItemStack> recipe, ItemStack craft, int limit, boolean defaultName, boolean shapeless, boolean unbreakable){
		this.name = name;
		this.recipe = recipe;
		this.craft = craft;
		this.limit = limit;
		this.shapeless = shapeless;
		this.unbreakable = unbreakable;
		onCraftListeners = new HashSet<>();
		onConsumeListeners = new HashSet<>();

		if (!defaultName){
			ItemMeta im = craft.getItemMeta();
			im.setDisplayName(ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', name));
			craft.setItemMeta(im);
		}

		displayItem = craft.clone();

		ItemMeta im = displayItem.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', name));
		displayItem.setItemMeta(im);

		register();
	}
	
	public boolean isLimited(){
		return limit != -1;
	}
	
	public String getName() {
		return name;
	}

	public List<ItemStack> getRecipe() {
		return recipe;
	}

	public ItemStack getCraft() {
		return craft;
	}

	public ItemStack getDisplayItem() {
		return displayItem;
	}

	public int getLimit() {
		return limit;
	}

	public boolean hasLimit(){
		return limit != -1;
	}

	public boolean isShapeless() { return shapeless; }

	public boolean isUnbreakable() {return unbreakable; }

	public Set<OnCraftListener> getOnCraftListeners() {
		return onCraftListeners;
	}

	public Set<OnConsumeListener> getOnConsumeListeners() {
		return onConsumeListeners;
	}

	public void registerListener(OnCraftListener listener) {
		onCraftListeners.add(listener);
	}

	public void registerListener(OnConsumeListener listener) {
		onConsumeListeners.add(listener);
	}

	@SuppressWarnings("deprecation")
	private void register(){
		ShapedRecipe craftRecipe = VersionUtils.getVersionUtils().createShapedRecipe(craft, UUID.randomUUID().toString());
		
		craftRecipe.shape("abc","def","ghi");
		
		List<Character> symbols = Arrays.asList('a','b','c','d','e','f','g','h','i');
		for(int i=0 ; i<9 ; i++){
			if(!recipe.get(i).getType().equals(Material.AIR)){
				Material material = recipe.get(i).getType();
				MaterialData data = recipe.get(i).getData();
				if (data != null && data.getItemType() == material) {
					craftRecipe.setIngredient(symbols.get(i), data);
				}else {
					craftRecipe.setIngredient(symbols.get(i), material);
				}
			}
		}
		
		Bukkit.getLogger().info("[UhcCore] "+name+" custom craft registered");
		//Bukkit.getServer().addRecipe(craftRecipe);
	}

	public static class Builder {

		private String name;
		private final ItemStack[] recipe;
		private ItemStack craft;
		private int limit;
		private boolean defaultName;
		private boolean shapeless;
		private boolean unbreakable;

		public Builder() {
			name = null;
			recipe = new ItemStack[9];
			craft = null;
			limit = -1;
			defaultName = false;
			shapeless = false;
			unbreakable = false;
		}

		public Builder setCraftName(String name) {
			this.name = name;
			return this;
		}

		public Builder setRecipeItem(int i, ItemStack recipeItem) {
			recipe[i] = new ItemStack(recipeItem.getType(), 1, recipeItem.getDurability());
			return this;
		}

		public Builder setCraft(ItemStack craft) {
			this.craft = craft;
			return this;
		}

		public Builder setCraftLimit(int limit) {
			this.limit = limit;
			return this;
		}

		public Builder useDefaultName(boolean defaultName) {
			this.defaultName = defaultName;
			return this;
		}

		public Builder setShapeless(boolean shapeless) {
			this.shapeless = shapeless;
			return this;
		}

		public Builder setUnbreakable(boolean unbreakable){
			this.unbreakable = unbreakable;
			return this;
		}

		public Craft create() throws IllegalArgumentException {
			List<ItemStack> recipeList = new ArrayList<>();

			boolean noneAir = false;
			for (int i = 0; i < 9; i++){
				if (recipe[i] == null){
					recipeList.add(new ItemStack(Material.AIR));
				}else {
					recipeList.add(recipe[i]);
					noneAir = true;
				}
			}

			if (!noneAir){
				throw new IllegalArgumentException("No recipe items assigned!");
			}

			if (name == null){
				throw new IllegalArgumentException("Craft name is not assigned!");
			}

			if (craft == null){
				throw new IllegalArgumentException("Craft item is not assigned!");
			}

			return new Craft(name, recipeList, craft, limit, defaultName, shapeless, unbreakable);
		}
	}

	public interface OnCraftListener {
		/**
		 * Gets called when a player completes the craft.
		 * Make sure the listener is registered using {@link #registerListener(OnCraftListener)}
		 * @param uhcPlayer Player that crafts the item.
		 * @return Should return true to cancel the craft event.
		 */
		boolean onCraft(UhcPlayer uhcPlayer);
	}

	public interface OnConsumeListener {
		/**
		 * Gets called when a player consumes a crafted item.
		 * Make sure the listener is registered using {@link #registerListener(OnConsumeListener)}
		 * @param uhcPlayer Player that consumes the item.
		 * @return Should return true to cancel the consume action.
		 */
		boolean onConsume(UhcPlayer uhcPlayer);
	}

	public long itemStacksHashCode(List<ItemStack> itemStacks) {
		long hash = 0;
		for (ItemStack itemStack : recipe) {
			//hash += (i + 1) * itemStacks[i].getType().hashCode();
			hash = 131 * hash + itemStack.getType().hashCode();
		}
		return hash;
	}

	public long shapelessHashCode(List<ItemStack> itemStacks) {
		long hash = 0;
		for (ItemStack itemStack : itemStacks) {
			if (UniversalMaterial.isDiamondArmor(itemStack.getType())) {
				hash += Material.DIAMOND_HELMET.hashCode();
			} else {
				hash += itemStack.getType().hashCode();
			}
		}
		return hash;
	}

	public long recipeNeedHashCode(List<ItemStack> itemStacks) {
		long hash = 0;
		for (ItemStack itemStack : itemStacks) {
			if (UniversalMaterial.isDiamondArmor(itemStack.getType())) {
				hash += Material.DIAMOND_HELMET.hashCode();
			} else if (!itemStack.getType().equals(Material.AIR)) {
				hash += itemStack.getType().hashCode();
			}
		}
		return hash;
	}

	/*@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0 ; i < recipe.size() ; i++){
			if(shapeless){
				hash += recipe.get(i).getType().hashCode();
			}else {
				//hash += (i + 1) * recipe.get(i).getType().hashCode();
				if (UniversalMaterial.isMeat(recipe.get(i).getType())) {
					hash += (i + 1) * Material.COW_SPAWN_EGG.hashCode();
				} else {
					hash += (i + 1) * recipe.get(i).getType().hashCode();
				}
			}
		}
		return hash;
	}*/

}
