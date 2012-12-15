package com.alf;

import java.util.*;

/**
 * Dumps debug output for tasks.
 * @author Eteocles
 */	
public class DebugTimer {
	/** Time of origin for this DebugTimer instance. */
	private long origin = System.nanoTime(); 
	/** Contains all of the start times for tasks. */
	private Map<String, Long> starts = new HashMap<String, Long>();
	/** Contains all of the runtime lengths for tasks. */
	private Map<String, Long> lengths = new HashMap<String, Long>();
	
	/**
	 * Reset the DebugTimer.
	 * Clear all stored/tracked processes.
	 */
	public void reset() {
		this.origin = System.nanoTime();
		this.starts.clear();
		this.lengths.clear();
	}
	
	/**
	 * Add a task to the DebugTimer.
	 * @param task - name of the task to be tracked
	 */
	public void startTask(String task) {
		this.starts.put(task, System.nanoTime());
	}
	
	/**
	 * "Stop a task" and update the length of runtime in the DebugTimer.
	 * @param task - name of the task
	 */
	public void stopTask(String task) {
		if (this.starts.containsKey(task)) {
			if (this.lengths.containsKey(task))
				this.lengths.put(task, System.nanoTime() - 
				(this.starts.get(task) + this.lengths.get(task)));
			else 
				this.lengths.put(task, System.nanoTime() - this.starts.get(task));
		}
	}
	
	/**
	 * Get the amount of time spent on a task relative to the DebugTimer's instantiation.
	 * @param task - task to be checked
	 * @return relative amount of time or zero if the task is not found
	 */
	public double getRelativeTimeSpent(String task) {
		if (this.lengths.containsKey(task)) {
			double total = System.nanoTime() - this.origin;
			return this.lengths.get(task) / total;
		}
		return 0D;
	}
	
	/**
	 * Dump the output for all stored tasks.
	 * @return - the string output for all of the stored tasks
	 */
	public String dump() {
		double total = (System.nanoTime() - this.origin) / 1000000000.0D;
		String output = String.format("Total run time: %.2fs\n", new Object[] { total });
		for (Map.Entry<String, Long> entry : this.lengths.entrySet())
			output += String.format("%s:\t\t%.4f%%\n", 
					new Object[] {entry.getKey(), getRelativeTimeSpent((String)entry.getKey()) * 100.0D });
		return output;
	}
}
