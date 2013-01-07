package com.alf.listener;


import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_4_6.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.PlayerInventory;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.CharacterManager;
import com.alf.character.Pet;
import com.alf.character.classes.AlfClass;
import com.alf.character.effect.CombatEffect;
import com.alf.character.effect.PeriodicEffect;
import com.alf.command.Command;
import com.alf.skill.Skill;
import com.alf.util.DeathManager;
import com.alf.util.Messaging;
import com.alf.util.Util;
import com.alf.util.DeathManager.PlayerInventoryStorage;
import com.alf.util.DeathManager.StoredItemStack;
import com.alf.util.Properties;

/**
 * Handle player related events.
 * @author Eteocles
 */
public class APlayerListener implements Listener {

	private final AlfCore plugin;

	/**
	 * Constructs an APlayerListener.
	 * @param plugin
	 */
	public APlayerListener(AlfCore plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handle player joining.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CharacterManager cm = this.plugin.getCharacterManager();
		final Alf alf = cm.getAlf(player);

		/**
		 * Forcefully reset combat effect.
		 */
		if (! alf.hasEffect("Combat")) {
			alf.resetCombatEffect();
		}

		cm.checkClass(alf);
		alf.syncExperience();
		alf.syncMana();
		alf.syncHealth();
		alf.checkInventory();

		//class prefix name
		if (AlfCore.properties.prefixClassName)
			player.setDisplayName("[" + alf.getAlfClass().getName() + "] " + player.getName());

		//bonus expiration
		if (System.currentTimeMillis() < AlfCore.properties.expiration)
			Messaging.send(player, AlfCore.properties.bonusMessage, new Object[0]);

		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				APlayerListener.this.plugin.getCharacterManager().performSkillChecks(alf);
				alf.checkInventory();
			}
		}, 5L);
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Welcome to AelfCraft!");
	}

	/**
	 * Handle player quitting.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		CharacterManager alfManager = this.plugin.getCharacterManager();
		Alf alf = alfManager.getAlf(player);
		alf.cancelDelayedSkill();
		alf.clearEffects();
		if (alf.isInCombat())
			alf.leaveCombat(CombatEffect.LeaveCombatReason.LOGOUT);

		if (alf.getPet() != null)
			alfManager.removePet(alf.getPet());

		alfManager.saveAlf(alf, true);
		alfManager.removeAlf(alf);

		for (Command command : this.plugin.getCommandParser().getCommands())
			if (command.isInteractive())
				command.cancelInteraction(player);
	}

	/**
	 * Handle player respawning.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		final Alf alf = this.plugin.getCharacterManager().getAlf(player);
		alf.setHealth(alf.getMaxHealth());
		alf.setMana(alf.getMaxMana() / 2);

		EntityType petType = alf.popPetOnRespawn();
		if (petType != null) {
			Location safeLoc = player.getLocation();
			Util.spawnPet(plugin, alf, petType, safeLoc);
		}


		//If the DeathManager contains the stored player...
		DeathManager dm = this.plugin.getDeathManager();
		if (dm.containsPlayer(player)) {
			//Remove player from DeathManager.
			PlayerInventoryStorage invStore = dm.popPlayer(player);
			PlayerInventory playerInv = player.getInventory();
			List<StoredItemStack> inv = invStore.getInventory();
			//Restore items or drop them if needed.
			for (StoredItemStack is : inv) {
				//If spot is occupied...
				//				if (playerInv.getItem(is.getSlot()) != null) {
				//					HashMap<Integer, ItemStack> leftovers = playerInv.addItem(new ItemStack[] { is.getItem() });
				//					if (leftovers.size() > 0)
				//		            	Util.dropItems(player.getLocation(), leftovers, false);
				//				} 		           
				//				else 
				playerInv.setItem(is.getSlot(), is.getItem());
			}
			AlfCore.log(Level.INFO, "Death Manager restored " + inv.size() + " item stacks to " +
					" player: " + player.getName());
		}

		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer entityPlayer = craftPlayer.getHandle();
		entityPlayer.exp = 0.0F;
		entityPlayer.expTotal = 0;
		entityPlayer.expLevel = 0;
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				APlayerListener.this.plugin.getCharacterManager().performSkillChecks(alf);
				alf.checkInventory();
				alf.syncExperience();
				alf.syncMana();
			}
		}, 20L);
	}

	/**
	 * Handle player interacting.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		//TODO lots of stuff here

		if (player.getItemInHand() != null && player.hasPermission("alf.bind")) {
			Material material = player.getItemInHand().getType();
			if (alf.hasBind(material)) {
				if ((!material.isBlock() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) || (material.isBlock() && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))) {
					String[] args = alf.getBind(material);
					this.plugin.getCommandParser().dispatch(player, "skill", "skill", args);

					try {
						String ident = "skill";
						for (String arg : args)
							ident += " " + arg;

						Skill skill = this.plugin.getSkillManager().getSkillFromIdent(ident, player);
						if (skill == null && args.length > 1)
							skill = this.plugin.getSkillManager().getSkillFromIdent("skill " + args[0], player);

						//	              isStealthy = skill.isType(SkillType.STEALTHY);
					} catch (Exception e) {
						String val = "";
						for (String arg : args)
							val = val + arg + " ";
						AlfCore.log(Level.SEVERE, player.getName() + " attempted to use bind for command: " + val + " but that command was not found!");
					}
				} else alf.cancelDelayedSkill();
			}
			else alf.cancelDelayedSkill();

		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		Entity entity = event.getRightClicked();
		
		if (entity instanceof LivingEntity) {
			Pet pet = alf.getPet();
			if (pet != null && pet.equals(entity)) {
				alf.getPet().toggleStationary();
			}
		}
		
		if (! alf.canEquipItem(player.getInventory().getHeldItemSlot())) {
			event.setCancelled(true);
			Util.syncInventory(player, this.plugin);
		}
	}

	/**
	 * Handle Player Exp Change.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		int amount = event.getAmount();
		if (amount != 0) {
			Alf alf = this.plugin.getCharacterManager().getAlf(event.getPlayer());
			if (amount < 0)
				alf.gainExp(event.getAmount(), AlfClass.ExperienceType.EXTERNAL, alf.getViewingLocation(1.0D));
		}
		//Suppress regular exp gain.
		event.setAmount(0);
	}

	/**
	 * Hand player entering bed shenanigans.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		Properties props = AlfCore.properties;
		if (props.bedHeal) {
			Alf alf = this.plugin.getCharacterManager().getAlf(event.getPlayer());
			long period = props.healInterval * 1000;
			double tickHealPercent = props.healPercent / 100.0D;

			BedHealEffect bhEffect = new BedHealEffect(period, tickHealPercent);
			alf.addEffect(bhEffect);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
		if (AlfCore.properties.bedHeal) {
			Alf alf = this.plugin.getCharacterManager().getAlf(event.getPlayer());
			if (alf.hasEffect("BedHeal"))
				alf.removeEffect(alf.getEffect("BedHeal"));
		}
	}

	/**
	 * A condition applied to a player in bed which heals him/her.
	 */
	public class BedHealEffect extends PeriodicEffect {
		private final double tickHealPercent;

		/**
		 * Construct the Bed Healing Effect.
		 * @param period
		 * @param tickHealPercent
		 */
		public BedHealEffect(long period, double tickHealPercent) {
			super(APlayerListener.this.plugin, "BedHeal", period);
			this.tickHealPercent = tickHealPercent;
		}

		/**
		 * Apply the effect to the given alf.
		 */
		public void applyToAlf(Alf alf) {
			super.applyToAlf(alf);
			this.lastTickTime = System.currentTimeMillis();
		}

		/**
		 * Tick the effect.
		 */
		public void tickAlf(Alf alf) {
			super.tickAlf(alf);
			Player player = alf.getPlayer();
			int healAmount = (int) Math.ceil(alf.getMaxHealth() * this.tickHealPercent);
			alf.setHealth(alf.getHealth() + healAmount);
			alf.syncHealth();
			if (alf.isVerbose())
				player.sendMessage(Messaging.createFullHealthBar(alf.getHealth(), alf.getMaxHealth()));
		}
	}

}
