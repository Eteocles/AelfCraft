package com.alf.character.party;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages and stores all parties.
 * @author Eteocles
 */
public class PartyManager {

	/**
	 * Party storage.
	 */
	private Set<AlfParty> parties = new HashSet<AlfParty>();
	
	public PartyManager() {}
	
	/**
	 * Add a party to the manager.
	 * @param party
	 */
	public void addParty(AlfParty party)
	{	this.parties.add(party);	}
	
	/**
	 * Get all loaded parties.
	 * @return
	 */
	public Set<AlfParty> getParties()
	{	return this.parties;	}
	
	/**
	 * Remove a party from the manager.
	 * @param party
	 */
	public void removeParty(AlfParty party)
	{	this.parties.remove(party);	}
}
