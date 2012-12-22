package com.alf.chat.event;

import org.bukkit.event.HandlerList;

import com.alf.chat.ChPlayer;
import com.alf.chat.channel.ChatChannel;

/**
 * Thrown when a player leaves a chat channel.
 * @author Eteocles
 */
public class PlayerLeaveChannelEvent {

	private static final HandlerList handlers = new HandlerList();
	private final ChPlayer player;
	private final ChatChannel channel;
	private LeaveChannelReason reason;
	private boolean cancelled = false;
	
	public PlayerLeaveChannelEvent(ChPlayer player, ChatChannel channel, LeaveChannelReason reason) {
		this.player = player;
		this.channel = channel;
		this.reason = reason;
	}
	
	public ChPlayer getPlayer()
	{	return this.player;	}
	
	public ChatChannel getChannel()
	{	return this.channel;	}
	
	public HandlerList getHandlers()
	{	return handlers;	}
	
	public static HandlerList getHandlerList()
	{	return handlers;	}
	
	public boolean isCancelled()
	{	return this.cancelled && isCancellable();	}
	
	public void setCancelled(boolean cancelled)
	{	this.cancelled = cancelled;	}
	
	public boolean isCancellable() {
		return this.reason != LeaveChannelReason.DISCONNECT && this.reason != LeaveChannelReason.CLOSE;
	}
	
	public LeaveChannelReason getReason()
	{	return this.reason;	}
	
	public static enum LeaveChannelReason {
		//Player leaves via command.
		COMMAND,
		//Player leaves due to disconnect, kick, ban, etc.
		DISCONNECT,
		//Player leaves due to channel close.
		CLOSE;
	}
	
}
