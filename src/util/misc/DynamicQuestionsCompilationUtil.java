package util.misc;

import java.util.Arrays;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import om.OmException;

public class DynamicQuestionsCompilationUtil {

    /** compile your files by JavaCompiler */
    public static DynamicCompilationResponse compile(String classPath,
    	Iterable<? extends JavaFileObject> files, String classOutputFolder)
    	throws OmException {
    	DynamicCompilationResponse response = new DynamicCompilationResponse();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (null == compiler) {
        	throw new OmException("Unable to continue as the Java compiler"
        		+ " was null returned from the ToolProvider.getSystemJavaCompiler() "
        		+ "Please check that the JDK is referenced in JAVA_HOME rather"
        		+ " than the JRE to use this facility.");
        }
        DynamicDiagnosticListener c = new DynamicDiagnosticListener();
        StandardJavaFileManager fileManager = compiler
        	.getStandardFileManager(c, Locale.ENGLISH, null);
        //String classPath = System.getProperty("java.class.path");
        Iterable<String> options = Arrays.asList("-d", classOutputFolder,
        	"-classpath", classPath);
        System.out.println("CLASSPATH -------------------------");
        String[] arr = classPath.split(";");
        for (int i = 0; i < arr.length; i++) {
			System.out.println(arr[i]);
		}
        System.out.println("END OF CLASSPATH ------------------");
        JavaCompiler.CompilationTask task = compiler
        	.getTask(null, fileManager, c, options, null, files);
        Boolean result = task.call();
        if (result == true) {
            response.setSuccess(true);
        } else{
        	response.setReports(c.getReports());
        }
        return response;
    }

}
