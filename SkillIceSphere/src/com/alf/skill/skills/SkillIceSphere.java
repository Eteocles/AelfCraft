package com.alf.skill.skills;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

public class SkillIceSphere extends ActiveSkill {

	private Map<Snowball, Long> snowballs = new LinkedHashMap<Snowball, Long>(100) {
		private static final long serialVersionUID = -6515147668124304427L;

		protected boolean removeEldestEntry(Map.Entry<Snowball, Long> eldest) {
			return (size() > 60) || (eldest.getValue() + 5000L <= System.currentTimeMillis());
		}
	};

	private long expireTime = -1;

	/**
	 * Construct the skill.
	 * @param plugin
	 */
	public SkillIceSphere(AlfCore plugin) {
		super(plugin, "IceSphere");
		setDescription("You launch an ice ball to pinpoint an ice sphere!");
		setUsage("/skill icesphere");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill icesphere" });
		setTypes(new SkillType[] {SkillType.ICE});
		Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(), plugin);
	}

	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {

		broadcastExecuteText(alf);
		
		//Generate the snowball and launch it in the direction the player is facing.
		Player player = alf.getPlayer();

		Snowball snowball = player.launchProjectile(Snowball.class);
		
		this.snowballs.put(snowball, System.currentTimeMillis());

		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		int duration = SkillConfigManager.getUseSetting(a, this, Setting.DURATION, 5000, false);
		int damage = SkillConfigManager.getUseSetting(a, this, Setting.DAMAGE, 4, false);

		return getDescription().replace("$1", damage + "").replace("$2", duration / 1000 + "");
	}

	/**
	 * Describes a listener for the SkillIceForm skill.
	 * @author Eteocles
	 */
	public class SkillListener implements Listener {
		
		private Set<Location> iceLocs = new HashSet<Location>();
		
		private List<Triplet> offsets;

		public SkillListener() {
			offsets = new ArrayList<Triplet>();
			for (int i = 2; i < 5; i++)
				for (int j = -1; j < 2; j++)
					offsets.add(new Triplet(-2, i, j));
			for (int i = 2; i < 5; i++)
				for (int j = -1; j < 2; j++)
					offsets.add(new Triplet(2, i, j));
			for (int i = -1; i < 2; i++)
				for (int j = 2; j < 5; j++)
					offsets.add(new Triplet(i, j, -2));
			for (int i = -1; i < 2; i++)
				for (int j = 2; j < 5; j++)
					offsets.add(new Triplet(i, j, 2));
			for (int i = -1; i < 2; i++)
				for (int j = -1; j < 2; j++)
					if (! (i == 0 && j == 0)) {
						offsets.add(new Triplet(i, 1, j));
						offsets.add(new Triplet(i, 5, j));
					}
			offsets.add(new Triplet(0, 0, 0));
			offsets.add(new Triplet(0, 6, 0));
		}

		/**
		 * Handle when the projectile hits a block.
		 * @param event
		 */
		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			Projectile proj = event.getEntity();
			if (proj instanceof Snowball) {
				SkillIceSphere.this.snowballs.remove(proj);
				Location loc = proj.getLocation();
				World w = loc.getWorld();
				
				for (Triplet t : offsets) {
					Location tempLoc = new Location(w, loc.getBlockX() + t.first, loc.getBlockY() + t.second, loc.getBlockZ() + t.third);
					if (tempLoc.getBlock().getType() == Material.AIR) {
						iceLocs.add(tempLoc);
						tempLoc.getBlock().setType(Material.ICE);
					}
				}
				
				SkillIceSphere.this.expireTime = System.currentTimeMillis() + 5000L;
				
				SkillIceSphere.this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(SkillIceSphere.this.plugin, new Runnable() {
					public void run() {
						if (System.currentTimeMillis() > SkillIceSphere.this.expireTime) {
							for (Location l : iceLocs) {
								if (l.getBlock().getType() == Material.ICE)
									l.getBlock().setType(Material.AIR);
							}
							SkillIceSphere.this.expireTime = -1;
							iceLocs.clear();
						}
					}
				}, 0, 5L);
			}
		}
		
		@EventHandler
		public void onBlockFade(BlockFadeEvent event) {
			Block block = event.getBlock();
			if (iceLocs.contains(block.getLocation()))
				event.setCancelled(true);
		}
		
		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Block block = event.getBlock();
			if (iceLocs.contains(block.getLocation()))
				event.setCancelled(true);
		}
	}
	
	class Triplet {
		int first;
		int second;
		int third;
		
		Triplet(int u, int v, int w) {
			first = u;
			second = v;
			third = w;
		}
	}

}
