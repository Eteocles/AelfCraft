package com.alf.chat.event;

import org.bukkit.event.*;

import com.alf.chat.ChPlayer;
import com.alf.chat.channel.ChatChannel;

/**
 * Thrown when a player joins a channel.
 * @author Eteocles
 */
public class PlayerJoinChannelEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final ChPlayer player;
	private final ChatChannel channel;
	private JoinChannelReason reason;
	private boolean cancelled = false;
	
	public PlayerJoinChannelEvent(ChPlayer player, ChatChannel channel, JoinChannelReason reason) {
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
		return reason == JoinChannelReason.COMMAND;
	}
	
	public JoinChannelReason getReason()
	{	return this.reason;	}
	
	public static enum JoinChannelReason {
		//Player joins via command.
		COMMAND,
		//Player rejoins and is placed in channels.
		CONNECT,
		//Player forced into channel by admin.
		FORCED;
	}
}