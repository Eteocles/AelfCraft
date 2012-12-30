package com.alf.character.party;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;
import com.alf.util.Messaging;
import com.alf.util.Properties;
import com.alf.util.Util;

/**
 * Describes an Alf party.
 * @author Eteocles
 */
public class AlfParty {

	private Alf leader;
	private Set<Alf> members = new HashSet<Alf>();
	private Boolean noPvp = true;
	private Boolean exp = true;
	private LinkedList<String> invites = new LinkedList<String>();
	
	/**
	 * Construct the party.
	 * @param leader
	 * @param plugin
	 */
	public AlfParty(Alf leader, AlfCore plugin) {
		this.leader = leader;
		this.members.add(leader);
	}
	
	/**
	 * Add a player to the stored list of invited players.
	 * @param player
	 */
	public void addInvite(String player)
	{	this.invites.add(player);	}
	
	/**
	 * Add the alf as a party member.
	 * @param alf
	 */
	public void addMember(Alf alf) {
		if (this.members.size() != AlfCore.properties.maxPartySize) {
			this.members.add(alf);
			update();
		}
	}
	
	/**
	 * Toggle exp sharing.
	 */
	public void expToggle() {
		if (this.exp){
			this.exp = false;
			messageParty("EXP Sharing is now disabled!", new Object[0]);
		} else {
			this.exp = true;
			messageParty("EXP Sharing is now enabled!", new Object[0]);
		}
	}
	
	/**
	 * Get party experience.
	 * @return
	 */
	public Boolean getExp() 
	{	return this.exp;	}
	
	/**
	 * Get the number of invited players.
	 * @return
	 */
	public int getInviteCount()
	{	return this.invites.size();	}
	
	/**
	 * Get the party leader.
	 * @return
	 */
	public Alf getLeader()
	{	return this.leader;	}
	
	/**
	 * Get party members.
	 * @return
	 */
	public Set<Alf> getMembers()
	{	return new HashSet<Alf>(this.members);	}
	
	/**
	 * Whether a player is invited.
	 * @param player
	 * @return
	 */
	public boolean isInvited(String player)
	{	return this.invites.contains(player);	}
	
	/**
	 * Whether friendly pvp is toggled.
	 * @return
	 */
	public Boolean isNoPvp()
	{	return this.noPvp;	}
	
	/**
	 * Whether an alf is a party member.
	 * @param alf
	 * @return
	 */
	public boolean isPartyMember(Alf alf)
	{	return this.members.contains(alf);	}
	
	/**
	 * Whether a player is a party member.
	 * @param player
	 * @return
	 */
	public boolean isPartyMember(Player player) {
		for (Alf alf : this.members)
			if (alf.getPlayer().equals(player))
				return true;
		return false;
	}
	
	/**
	 * Message all members of a party.
	 * @param msg
	 * @param params
	 */
	public void messageParty(String msg, Object[] params) {
		for (Alf alf : this.members)
			Messaging.send(alf.getPlayer(), msg, params, ChatColor.DARK_AQUA);
	}
	
	/**
	 * Toggle pvp for a party.
	 */
	public void pvpToggle() {
		if (this.noPvp) {
			this.noPvp = false;
			messageParty("PvP is now enabled!", new Object[0]);
		} else {
			this.noPvp = true;
			messageParty("PvP is now disabled!", new Object[0]);
			
		}
	}
	
	/**
	 * Remove an invitation from a player.
	 * @param player
	 */
	public void removeInvite(Player player)
	{	this.invites.remove(player);	}
	
	/**
	 * Remove an alf as a member of the party.
	 * @param alf
	 */
	public void removeMember(Alf alf) {
		this.members.remove(alf);
		alf.setParty(null);
		
		if (this.members.size() == 1) {
			Alf remainingMember = (Alf)this.members.iterator().next();
			remainingMember.setParty(null);
			messageParty("Party disbanded.", new Object[0]);
			this.members.remove(remainingMember);
			this.leader = null;
		} 
		
		else if (alf.equals(this.leader) && ! this.members.isEmpty()) {
			this.leader = ((Alf)this.members.iterator().next());
			messageParty("$1 is now leading the party.", new Object[] { 
					this.leader.getPlayer().getDisplayName() });
		}
		update();
	}
	
	/**
	 * Remove the oldest invite.
	 */
	public void removeOldestInvite()
	{	this.invites.pop();	}
	
	/**
	 * Set the new party leader.
	 * @param leader
	 */
	public void setLeader(Alf leader)
	{	this.leader = leader;	}
	
	/**
	 * Update the party.
	 */
	public void update() {}
	
	/**
	 * Have the party members gain exp.
	 * @param amount
	 * @param type
	 * @param location
	 */
	public void gainExp(double amount, AlfClass.ExperienceType type, Location location) {
		Set<Alf> inRangeMembers = new HashSet<Alf>();
		
		//Party members must be in the same world to share exp bonuses.
		for (Alf partyMember : this.members) {
			if (location.getWorld().equals(partyMember.getPlayer().getLocation().getWorld()) && location.distanceSquared(partyMember.getPlayer().getLocation()) <= 2500.0D) {
				if (partyMember.canGain(type))
					inRangeMembers.add(partyMember);
			}
		}
		
		int partySize = inRangeMembers.size();
		double sharedExp = amount / partySize;
		double bonusExp = partySize > 1 ? sharedExp : 0.0D;
		
		//Determine bonus exp by scaling.
		if (partySize > 1)
			bonusExp *= Properties.partyMults[partySize-1];
		bonusExp *= AlfCore.properties.partyBonus;
		bonusExp = Util.formatDouble(bonusExp);
		
		//Have all party members gain experience of the type.
		for (Alf partyMember : inRangeMembers)
			partyMember.gainExp(sharedExp + bonusExp, type, location);
	}
}
