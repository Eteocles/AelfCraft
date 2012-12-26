package com.alf.skill;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import com.alf.AlfCore;

/**
 * Manages skills.
 * @author Eteocles
 */
public class SkillManager {

	private Map<String, Skill> skills;
	private Map<String, Skill> identifiers;
	private Map<String, File> skillFiles;
	private final AlfCore plugin;
	private final File dir;
	private final ClassLoader classLoader;
	
	/**
	 * Construct the Skill Manager.
	 * @param alfCore
	 */
	public SkillManager(AlfCore plugin) {
		this.plugin = plugin;
		this.skills = new LinkedHashMap<String, Skill>();
		this.identifiers = new HashMap<String, Skill>();
		this.dir = new File(plugin.getDataFolder(), "skills");
		this.dir.mkdir();
		
		List<URL> urls = new ArrayList<URL>();
		for (String skillFile : this.dir.list()) {
			if (skillFile.contains(".jar")) {
				File file = new File(this.dir, skillFile);
				String name = skillFile.toLowerCase().replace(".jar", "").replace("skill", "");
				if (this.skillFiles.containsKey(name))
					AlfCore.log(Level.SEVERE, "Duplicate skil jar found! Please remove " + 
						skillFile + " or " + ((File) this.skillFiles.get(name)).getName());
				else {
					this.skillFiles.put(name, file);
					try {
						urls.add(file.toURI().toURL());
					} catch (MalformedURLException e) { e.printStackTrace(); }
				}
			}
		}
		
		ClassLoader cl = plugin.getClass().getClassLoader();
		this.classLoader = URLClassLoader.newInstance((URL[]) urls.toArray(
				new URL[urls.size()]), cl);
	}

	public Skill getSkillFromIdent(String ident, CommandSender executor) {
		// TODO Auto-generated method stub
		return null;
	}

	public Skill getSkill(String name) {
		return null;
	}

	public Collection<Skill> getSkills() {
		throw new Error("Implement me!");
	}

}
