package com.alf.util;

import java.lang.reflect.Method;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkEffectUtil {
	// internal references, performance improvements
	private static Method world_getHandle = null;
	private static Method nms_world_broadcastEntityEffect = null;
	private static Method firework_getHandle = null;
	
	/**
	 * Play the firework effect at a given location.
	 * @param world
	 * @param loc
	 * @param fe
	 * @throws Exception
	 */
	public static void playFirework(World world, Location loc, FireworkEffect fe) throws Exception {
		//Bukkit load.
		Firework fw = (Firework) world.spawn(loc, Firework.class);
		//net minecraft server World
		Object nms_world = null;
		Object nms_firework = null;
		
		//Reflection yay
		if (world_getHandle == null) {
			//get the methods of the craftbukkit types
			world_getHandle = getMethod(world.getClass(), "getHandle");
			firework_getHandle = getMethod(fw.getClass(), "getHandle");
		}
		//invoke the reflected objects, no args
		nms_world = world_getHandle.invoke(world, (Object[])null);
		nms_firework = firework_getHandle.invoke(fw, (Object[])null);
		//null checks are fast, so having this separate is ok.
		if(nms_world_broadcastEntityEffect == null) {
            // get the method of the nms_world
            nms_world_broadcastEntityEffect = getMethod(nms_world.getClass(), "broadcastEntityEffect");
        }
		
		//Meta Handling
		FireworkMeta data = fw.getFireworkMeta();
		data.clearEffects();
		data.setPower(1);
		data.addEffect(fe);
		fw.setFireworkMeta(data);
		
		//Invoke the reflected method with arguments and then kill the fireworks object.
		nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {nms_firework, (byte) 17});
        // remove from the game
		fw.remove();
		
	}
	
	/**
	 * Get the specified method type.
	 * @param clazz
	 * @param method
	 * @return
	 */
	private static Method getMethod(Class<?> clazz, String method) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(method))
				return m;
		}
		
		return null;
	}
	
}
