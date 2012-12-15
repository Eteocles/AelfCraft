package com.alf.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Output debug information through the logger.
 * @author Eteocles
 */
public class DebugLog {

	private FileHandler fh;
	private Logger log;
	
	/**
	 * Constructs the DebugLog.
	 * @param logger
	 * @param file
	 */
	public DebugLog(String logger, String file) {
		this.log = Logger.getLogger(logger);
		try {
			this.fh = new FileHandler(file, true);
			this.log.setUseParentHandlers(false);
			//Remove all initial handlers, and replace with this one.
			for (Handler handler: this.log.getHandlers())
				this.log.removeHandler(handler);
			this.log.addHandler(this.fh);
			this.log.setLevel(Level.ALL);
			this.fh.setFormatter(new LogFormatter());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log a message through the file handler.
	 * @param level
	 * @param msg
	 */
	public void log(Level level, String msg) {
		this.log.log(level, msg);
	}
	
	/**
	 * Log a throwable.
	 * @param sourceClass
	 * @param sourceMethod
	 * @param thrown
	 */
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		log.throwing(sourceClass, sourceMethod, thrown);
	}

	/**
	 * Close the DebugLog file handler.
	 */
	public void close() {
		this.fh.close();
	}
	
	/**
	 * Format a log record.
	 * @author Eteocles
	 */
	private class LogFormatter extends Formatter {

		private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		private LogFormatter() {}
		
		public String format(LogRecord record) {
			String formatted = "";
			Throwable ex = record.getThrown();
			formatted += this.date.format(record.getMillis());
			formatted += " [";
 			formatted += record.getLevel().getLocalizedName().toUpperCase();
 			formatted += "] ";
 			formatted += record.getMessage();
 			formatted += "\n";
 			if (ex != null) {
 				StringWriter writer = new StringWriter();
 				ex.printStackTrace(new PrintWriter(writer));
 				formatted += writer.toString();
 			}
			return formatted;
		}
		
	}

}
