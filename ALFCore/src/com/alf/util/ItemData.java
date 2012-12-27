package com.alf.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Describes and encapsulates an item stack.
 * @author Eteocles
 */
public class ItemData {
	public final Material mat;
	public final int id;
	public final short subType;

	/** */
	public ItemData(Material mat)
	{	this(mat, (short)0);	}

	/** */
	public ItemData(int id)
	{	this(Material.getMaterial(id), (short)0);	}

	public ItemData(int id, short subType)
	{	this(Material.getMaterial(id), subType);	}

	/**
	 * Constructs the ItemData with the provided information.
	 * @param mat - material data
	 * @param subType - data value for specific typing
	 */
	public ItemData(Material mat, short subType) {
		this.mat = mat;
		this.id = mat.getId();
		this.subType = subType;
	}

	/**
	 * Simple hashing for an item's data. 0 Collisions.
	 */
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + this.id;
		result = prime * result + this.subType;
		return result;
	}

	/**
	 * Defines equality for ItemData.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof ItemStack) {
			ItemStack os = (ItemStack)obj;
			if (os.getType().getMaxDurability() > 16)
				return os.getTypeId() == this.id;
			return (os.getTypeId() == this.id) && (os.getDurability() == this.subType);
		}
		ItemData other = (ItemData) obj;
		return (this.id == other.id) && (this.subType == other.subType);
	}
}
