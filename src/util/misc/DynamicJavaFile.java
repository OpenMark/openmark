package util.misc;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class DynamicJavaFile extends SimpleJavaFileObject {

	private String content;

	public DynamicJavaFile(String className, String s) {
		super(URI.create("string:///" + className.replace('.','/')
			+ Kind.SOURCE.extension), Kind.SOURCE);
		content = s;
	}
	
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return content;
	}
	
}
