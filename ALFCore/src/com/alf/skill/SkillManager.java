package com.alf.skill;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

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
		this.skillFiles = new HashMap<String, File>();
		this.dir = new File(plugin.getDataFolder(), "skills");
		this.dir.mkdir();

		//Set up the ClassLoader.
		List<URL> urls = new ArrayList<URL>();
		for (String skillFile : this.dir.list()) {
			if (skillFile.contains(".jar")) {
				File file = new File(this.dir, skillFile);
				String name = skillFile.toLowerCase().replace(".jar", "").replace("skill", "");
				if (this.skillFiles.containsKey(name))
					AlfCore.log(Level.SEVERE, "Duplicate skill jar found! Please remove " + 
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

	/**
	 * Add a skill to the manager.
	 * @param skill
	 */
	public void addSkill(Skill skill) {
		this.skills.put(skill.getName().toLowerCase().replace("skill", ""), skill);

		for (String ident : skill.getIdentifiers())
			this.identifiers.put(ident.toLowerCase(), skill);
	}

	/**
	 * Get a skill from its identifier label.
	 * @param ident
	 * @param executor
	 * @return
	 */
	public Skill getSkillFromIdent(String ident, CommandSender executor) {
		if (this.identifiers.get(ident.toLowerCase()) == null)
			for (Skill skill : this.skills.values())
				if (skill.isIdentifier(executor, ident))
					return skill;
		return (Skill) this.identifiers.get(ident.toLowerCase());
	}

	/**
	 * Get a skill from the manager by its name.
	 * @param name
	 * @return
	 */
	public Skill getSkill(String name) {
		if (name == null)
			return null;
		name = name.toLowerCase();

		if (! isLoaded(name) && this.skillFiles.containsKey(name))
			loadSkill(name);

		return (Skill) this.skills.get(name);
	}

	/**
	 * Get the collection of skills stored in the manager.
	 * @return
	 */
	public Collection<Skill> getSkills() {
		return Collections.unmodifiableCollection(this.skills.values());
	}
	
	/**
	 * Load an outsourced skill.
	 * @param name
	 * @return
	 */
	public boolean loadOutsourcedSkill(String name) {
		if (name == null | this.skills.get(name.toLowerCase()) != null)
			return true;
		OutsourcedSkill oSkill = new OutsourcedSkill(this.plugin, name);
		ConfigurationSection config = SkillConfigManager.outsourcedSkillConfig.getConfigurationSection(
				oSkill.getName());
		List<String> perms = new ArrayList<String>();
		if (config != null)
			perms = config.getStringList("permissions");
		if (perms.isEmpty()) {
			AlfCore.log(Level.SEVERE, "There are no permissions defined for " + oSkill.getName());
			return false;
		}
		oSkill.setPermissions((String[])perms.toArray(new String[perms.size()]));
		oSkill.setUsage(config.getString("usage"));
		oSkill.setDescription(config.getString("usage"));
		this.skills.put(name.toLowerCase(), oSkill);
		return true;
	}

	/**
	 * Whether a skill is loaded.
	 * @param name
	 * @return
	 */
	public boolean isLoaded(String name) {
		return this.skills.containsKey(name.toLowerCase());
	}

	/**
	 * Load a skill by name.
	 * @param name
	 * @return
	 */
	public boolean loadSkill(String name) {
		if (isLoaded(name))
			return true;

		Skill skill = loadSkill((File) this.skillFiles.get(name.toLowerCase()));

		if (skill == null)
			return false;
		addSkill(skill);

		return true;
	}

	/**
	 * Load a skill from its jar file.
	 * @param file
	 * @return
	 */
	public Skill loadSkill(File file) {
		try {
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();

			String mainClass = null;
			while (entries.hasMoreElements()) {
				JarEntry element = (JarEntry) entries.nextElement();
				if (element.getName().equalsIgnoreCase("skill.info")) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							jarFile.getInputStream(element)));
					mainClass = reader.readLine().substring(12);
					break;
				}
			}
			
			jarFile.close();

			if (mainClass != null) {
				Class<?> clazz = Class.forName(mainClass, true, this.classLoader);
				Class<? extends Skill> skillClass = clazz.asSubclass(Skill.class);
				Constructor<? extends Skill> ctor = skillClass.getConstructor(new Class[] {
						this.plugin.getClass()
				});

				Skill skill = (Skill) ctor.newInstance(new Object[] { this.plugin });
				this.plugin.getSkillConfigs().loadSkillDefaults(skill);
				skill.init();

				return skill;
			}
			throw new IllegalArgumentException();
		} catch (NoClassDefFoundError e) {
			AlfCore.log(Level.WARNING, "Unable to load " + file.getName() +
					" skill was written for a previous AlfCore version, please check" +
					" the debug log for more information!");
			AlfCore.debugThrow(getClass().toString(), "loadSkill", e);
			return null;
		} catch (ClassNotFoundException e) {
			AlfCore.log(Level.WARNING, "Unable to load " + file.getName() +
					" skill was written for a previous AlfCore version, please check" +
					" the debug log for more information!");
			AlfCore.debugThrow(getClass().toString(), "loadSkill", e);
			return null;
		} catch (IllegalArgumentException e) {
			AlfCore.log(Level.SEVERE, "Could not detect the proper Skill class to load for: " +
					file.getName());
			return null;
		} catch (Exception e) {
			AlfCore.log(Level.INFO, "The skill " + file.getName() + " failed to load for an unknown reason.");
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Load skills.
	 */
	public void loadSkills() {
		for (Map.Entry<String, File> entry : this.skillFiles.entrySet()) {
			if (! isLoaded(entry.getKey())) {
				Skill skill = loadSkill(entry.getValue());
				if (skill != null) {
					addSkill(skill);
					AlfCore.debugLog(Level.INFO, "Skill " + skill.getName() + " Loaded.");
				}
			}
		}
	}

	/**
	 * Remove the skill from the manager.
	 * @param skill
	 */
	public void removeSkill(Skill skill) {
		this.skills.put(skill.getName().toLowerCase().replace("skill", ""), skill);
		for (String ident : skill.getIdentifiers())
			this.identifiers.remove(ident);
	}

}
