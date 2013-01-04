package com.alf.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.alf.AlfCore;
import com.alf.api.event.AlfChangeLevelEvent;
import com.alf.api.event.AlfRegainHealthEvent;
import com.alf.api.event.SkillUseEvent;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;
import com.alf.character.effect.EffectType;
import com.alf.character.party.AlfParty;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * Listens to AlfCore events.
 * @author Eteocles
 */
public class AEventListener implements Listener {
	private AlfCore plugin;
	
	public AEventListener (AlfCore plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Handle alf-level change.
	 * @param event
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	  public void onAlfChangeLevel(AlfChangeLevelEvent event) {
	    Alf alf = event.getAlf();
	    AlfClass alfClass = event.getAlfClass();

	    int level = event.getTo();
	    
	    if (level > event.getFrom()) {
	      for (Skill skill : this.plugin.getSkillManager().getSkills()) {
	        if (alfClass.hasSkill(skill.getName()) && alf.canUseSkill(skill)) {
	          int levelRequired = SkillConfigManager.getUseSetting(alf, skill, Setting.LEVEL, 1, true);
	          
	          if (levelRequired == level)
	            Messaging.send(event.getAlf().getPlayer(), "You have learned $1.", new Object[] { skill.getName() });
	        }
	      }
	    }
	    else
	      for (Skill skill : this.plugin.getSkillManager().getSkills())
	        if (alfClass.hasSkill(skill.getName())) {
	          int levelRequired = SkillConfigManager.getUseSetting(alf, skill, Setting.LEVEL, 1, true);
	          if (levelRequired > level && levelRequired <= event.getFrom())
	            Messaging.send(event.getAlf().getPlayer(), "You have forgotton how to use $1", new Object[] { skill.getName() });
	        }
	  }

	/**
	 * Handle Alf regaining health.
	 * @param event
	 */
	  @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	  public void onAlfRegainHealth(AlfRegainHealthEvent event)
	  {
	    if (! event.getAlf().hasParty()) {
	      return;
	    }

	    AlfParty party = event.getAlf().getParty();
	    if (event.getAmount() > 0)
	      party.update();
	  }

	  /**
	   * Listen to Skill Use attempts.
	   * @param event
	   */
	  @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	  public void onSkillUse(SkillUseEvent event)
	  {
	    Alf alf = event.getAlf();
	    if ((alf.hasEffect("Root")) && (event.getSkill().isType(SkillType.MOVEMENT)) && (!event.getSkill().isType(SkillType.COUNTER))) {
	      Messaging.send(alf.getPlayer(), "You can't use that skill while rooted!", new Object[0]);
	      event.setCancelled(true);
	    }

	    if ((alf.hasEffectType(EffectType.SILENCE)) && (event.getSkill().isType(SkillType.SILENCABLE))) {
	      Messaging.send(alf.getPlayer(), "You can't use that skill while silenced!", new Object[0]);
	      event.setCancelled(true);
	    } 
	    else if ((alf.hasEffectType(EffectType.STUN) || alf.hasEffectType(EffectType.DISABLE)) && 
	      (!event.getSkill().isType(SkillType.COUNTER))) {
	      event.setCancelled(true);
	    }
	  }
}
