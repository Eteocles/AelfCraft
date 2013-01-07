package com.alf.character.effect.common;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.Skill;
import com.alf.util.Util;

/**
 * A condition in which a player can not equip weapons.
 * @author Eteocles
 */
public class DisarmEffect extends ExpirableEffect {
  private final String applyText;
  private final String expireText;
  private HashMap<Alf, ItemStack[]> disarms = new HashMap<Alf, ItemStack[]>();

  /**
   * Construct the effect.
   * @param skill
   * @param duration
   * @param applyText
   * @param expireText
   */
  public DisarmEffect(Skill skill, long duration, String applyText, String expireText) {
    super(skill, "Disarm", duration);
    this.types.add(EffectType.HARMFUL);
    this.types.add(EffectType.DISARM);
    this.applyText = applyText;
    this.expireText = expireText;
  }

  /**
   * Apply the effect to an alf.
   */
  public void applyToAlf(Alf alf)
  {
    super.applyToAlf(alf);
    Player player = alf.getPlayer();
    ItemStack[] inv = player.getInventory().getContents();
    for (int i = 0; i < 9; i++) {
      ItemStack is = inv[i];
      if ((is != null) && (Util.isWeapon(is.getType()))) {
        if (!this.disarms.containsKey(alf)) {
          ItemStack[] disarmedItems = new ItemStack[9];
          disarmedItems[i] = is.clone();
          this.disarms.put(alf, disarmedItems);
          player.getInventory().clear(i);
        } else {
          ItemStack[] items = (ItemStack[])this.disarms.get(alf);
          items[i] = is;
          player.getInventory().clear(i);
        }
      }
    }
    Util.syncInventory(player, this.plugin);
    broadcast(player.getLocation(), this.applyText, new Object[] { player.getDisplayName() });
  }

  /**
   * Remove the effect from the alf.
   */
  public void removeFromAlf(Alf alf)
  {
    super.removeFromAlf(alf);
    Player player = alf.getPlayer();

    if (this.disarms.containsKey(alf)) {
      PlayerInventory inv = player.getInventory();
      ItemStack[] contents = inv.getContents();
      ItemStack[] oldInv = (ItemStack[])this.disarms.get(alf);
      for (int i = 0; i < 9; i++) {
        if (oldInv[i] != null) {
          if (contents[i] != null) {
            Util.moveItem(alf, i, contents[i]);
          }
          inv.setItem(i, oldInv[i]);
        }
      }
      this.disarms.remove(alf);
      Util.syncInventory(player, this.plugin);
    }
    broadcast(player.getLocation(), this.expireText, new Object[] { player.getDisplayName() });
  }
}