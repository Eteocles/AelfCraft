package com.alf.skill.skills;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillType;

/**
 * Describes a shout in which a player broadcasts to nearby players.
 * @author Eteocles
 */
public class SkillShout extends ActiveSkill {

	private List<String> shouts = new ArrayList<String>();
	private File shoutsFile;
	private Configuration shoutsConfig;

	/**
	 * 
	 * @param plugin
	 */
	public SkillShout(AlfCore plugin) {
		super(plugin, "Shout");
		setDescription("You shout a random message to distract your enemies!");
		setUsage("/skill shout");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill shout", "skill vshout" });
		setTypes(new SkillType[] {SkillType.TEXT});

		shoutsFile = new File(plugin.getDataFolder() + File.separator + "skills" + File.separator + "shout", "shouts.yml");
		checkForConfig(shoutsFile);
	}

	/**
	 * Initiate the skill and load in shouts.
	 */
	public void init() {
		super.init();
		shoutsConfig = YamlConfiguration.loadConfiguration(shoutsFile);
		shouts = shoutsConfig.getStringList("shouts");
	}

	public void checkForConfig(File config) {
		if (! config.exists())
			try {
				AlfCore.log(Level.WARNING, "File "+config.getName()+" not found - generating defaults.");
				config.getParentFile().mkdir();
				config.createNewFile();
				OutputStream output = new FileOutputStream(config, false);
				InputStream input = SkillShout.class.getResourceAsStream("/skills/shout/"+config.getName());
				byte[] buf = new byte[8192];
				int length = 1;
				while (length >= 0) {
					length = input.read(buf);
					if (length >= 0)
						output.write(buf, 0, length);
				}
				input.close();
				output.close();
			} catch (Exception e) { e.printStackTrace(); }
	}

	@Override
	/**
	 * Broadcast a loaded shout.
	 */
	public SkillResult use(Alf alf, String[] args) {
		if (!this.shouts.isEmpty()) {
			broadcastExecuteText(alf);

			this.broadcast(alf.getPlayer().getLocation(), getShout(), new Object[0]);

			return SkillResult.NORMAL;
		} else {
			return SkillResult.FAIL;
		}
	}

	/**
	 * Get the shout.
	 * @return
	 */
	private String getShout() {
		String shout = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "";

		shout += this.shouts.get((int)(Math.random() * shouts.size()));

		return shout;
	}

	@Override
	/**
	 * Get the description for this skill.
	 */
	public String getDescription(Alf a) {
		return getDescription();
	}

}
