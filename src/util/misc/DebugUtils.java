package util.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public abstract class DebugUtils
{
	/**
	 * Append a messeage to the file "C:/Temp/om.txt".
	 * @param message debug output.
	 */
	public static void appendToDebugFile(String message)
	{
		try
		{
			PrintWriter pw = new PrintWriter(new FileOutputStream(new File("C:/Temp/om.txt"), true));
			pw.println(message);
			pw.close();
		}
		catch(FileNotFoundException e)
		{
			// Ignore.
		}
	}

	/**
	 * Debug helper class for writing a file with a detailed time log.
	 */
	public static class TimingLogger {
		private static long startTime;
		private static long lastTime;
		private static PrintWriter pw;

		/**
		 * Start. Open the file and record the time.
		 *
		 * Will overwrite any previous file.
		 *
		 * @throws FileNotFoundException
		 */
		public static void start() throws FileNotFoundException {
			if (pw == null) {
				pw = new PrintWriter(new File("C:\\Temp\\om-time-log.txt"));
			}
			pw.println("======================================================");
			startTime = System.nanoTime();
			lastTime = startTime;
		}

		/**
		 * Log one step, with the time, and the time since the last step.
		 * @param step Some information about what this time is.
		 */
		public static void logTime(String step) {
			if (pw == null) {
				return;
			}
			long time = System.nanoTime();
			pw.printf("%+9.0fus   %10.6fs %s\n", (time - lastTime) / 1000.0,
					(time - startTime) / 1000000000.0, step);
			lastTime = time;
		}

		/**
		 * Close the file.
		 */
		public static void close() {
			pw.close();
			pw = null;
		}
	}
}
