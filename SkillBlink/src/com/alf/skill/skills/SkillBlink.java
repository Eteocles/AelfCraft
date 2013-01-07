package com.alf.skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BlockIterator;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;
import com.alf.util.Util;

/**
 * A skill that teleports a player to a certain location.
 * @author Eteocles
 */
public class SkillBlink extends ActiveSkill {

	public SkillBlink(AlfCore plugin) {
		super(plugin, "Blink");
	    setDescription("Teleports you up to $1 blocks away.");
	    setUsage("/skill blink");
	    setArgumentRange(0, 0);
	    setIdentifiers(new String[] { "skill blink" });
	    setTypes(new SkillType[] { SkillType.SILENCABLE, SkillType.TELEPORT });

	    Bukkit.getServer().getPluginManager().registerEvents(new SkillPlayerListener(this), plugin);
	}
	
	  public ConfigurationSection getDefaultConfig()
	  {
	    ConfigurationSection node = super.getDefaultConfig();
	    node.set(Setting.MAX_DISTANCE.node(), Integer.valueOf(6));
	    node.set("restrict-ender-pearl", Boolean.valueOf(true));
	    return node;
	  }

	  public SkillResult use(Alf alf, String[] args)
	  {
		  Player player = alf.getPlayer();
		    Location loc = player.getLocation();
		    if ((loc.getBlockY() > loc.getWorld().getMaxHeight()) || (loc.getBlockY() < 1)) {
		      Messaging.send(player, "The void prevents you from blinking!", new Object[0]);
		      return SkillResult.FAIL;
		    }
		    
		    int distance = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE, 6, false);
		    Block prev = null;

		    BlockIterator iter = null;
		    try {
		      iter = new BlockIterator(player, distance);
		    } catch (IllegalStateException e) {
		      Messaging.send(player, "There was an error getting your blink location!", new Object[0]);
		      return SkillResult.INVALID_TARGET_NO_MSG;
		    }
		    while (iter.hasNext()) {
		      Block b = iter.next();
		      if (!Util.transparentBlocks.contains(b.getType()) || (!Util.transparentBlocks.contains(b.getRelative(BlockFace.UP).getType()) && !Util.transparentBlocks.contains(b.getRelative(BlockFace.DOWN).getType()))) 
		    	  break;
		      prev = b;
		    }

		    if (prev != null) {
		      Location teleport = prev.getLocation().clone();

		      teleport.setPitch(player.getLocation().getPitch());
		      teleport.setYaw(player.getLocation().getYaw());
		      player.teleport(teleport);
		      
		      broadcastExecuteText(alf);
		      
		      return SkillResult.NORMAL;
		    }
		    Messaging.send(player, "No location to blink to.", new Object[0]);
		    return SkillResult.INVALID_TARGET_NO_MSG;
	  }

	  public String getDescription(Alf alf)
	  {
	    int distance = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE, 6, false);
	    return getDescription().replace("$1", distance + "");
	  }

	  /**
	   * Listens to teleport events.
	   * @author Eteocles
	   */
	  public class SkillPlayerListener implements Listener
	  {
	    private final Skill skill;

	    public SkillPlayerListener(Skill skill) {
	      this.skill = skill;
	    }

	    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	    public void onPlayerTeleport(PlayerTeleportEvent event) {
	      Alf alf = SkillBlink.this.plugin.getCharacterManager().getAlf(event.getPlayer());
	      if (!SkillConfigManager.getUseSetting(alf, this.skill, "restrict-ender-pearl", true))
	        return;
	      if ((alf.getSkillLevel(this.skill) < 1) && (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
	        event.setCancelled(true);
	    }
	  }
}
