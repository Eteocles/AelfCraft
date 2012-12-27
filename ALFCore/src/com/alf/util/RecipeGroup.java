package com.alf.util;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Encapsulates a recipe.
 * @author Eteocles
 */
public class RecipeGroup extends HashMap<ItemData, Boolean> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int level;
	public final String name;
	private boolean allRecipes = false;

	/**
	 * Construct the recipe group.
	 * @param name
	 * @param level
	 */
	public RecipeGroup(String name, int level) {
		this.name = name;
		this.level = level;
	}

	/**
	 * Whether this group has all recipes.
	 * @return
	 */
	public boolean hasAllRecipes() {
		return this.allRecipes;
	}
	
	/**
	 * Set flag for all recipes.
	 * @param val
	 */
	public void setAllRecipes(boolean val) {
		this.allRecipes = val;
	}
	
	/**
	 * Determine whether or not this RecipeGroup contains the given object.
	 */
	public boolean containsKey(Object o) {
		if (o == null)
			return false;
		if (this.allRecipes)
			return true;
		if (o instanceof ItemStack) {
			ItemStack is = (ItemStack) o;
			return super.containsKey(new ItemData(is.getType(),
					is.getType().getMaxDurability() > 16 ? 0 : is.getDurability()));
		} if (o instanceof Material)
			return super.containsKey(new ItemData((Material)o));
		return super.containsKey(o);
	}
	
	/**
	 * Return a Boolean corresponding to the state of the stored data (whether or not it contains o).
	 */
	public Boolean get(Object o) {
		if (o == null) return null;
		Boolean val;
		if ((o instanceof ItemStack)) {
			ItemStack is = (ItemStack)o;
			val = (Boolean)super.get(new ItemData(is.getType(), 
					is.getType().getMaxDurability() > 16 ? 0 : is.getDurability()));
		} else {
			if (o instanceof Material)
				val = (Boolean)super.get(new ItemData((Material)o));
				else val = (Boolean)super.get(o);
		}
		if (val == null && this.allRecipes)
			return Boolean.valueOf(this.allRecipes);
		return val;
	}
	
	public int hashCode()
	{	return this.name.hashCode();	}
	
	/**
	 * Define Equality for the RecipeGroup.
	 */
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o instanceof String)
			return this.name.equals(0);
		if (o instanceof RecipeGroup)
			return ((RecipeGroup)o).name.equals(this.name);
		return false;
	}
	
}
