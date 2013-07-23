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
}
