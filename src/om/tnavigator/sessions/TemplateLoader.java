package om.tnavigator.sessions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;

import om.OmException;
import om.tnavigator.NavigatorConfig;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.misc.IO;
import util.xml.XML;

/**
 * Class for loading templates from a given folder.
 */
public class TemplateLoader
{
	/** The folder to load templates from. */
	private File templateDirectory;

	/**
	 * Create a template loader to load templates from a given folder.
	 * @param path
	 * @throws OmException
	 */
	public TemplateLoader(File path) throws OmException
	{
		if (!path.isDirectory() && path.canRead())
		{
			throw new OmException("Invalid template folder " + path);
		}
		this.templateDirectory = path;
	}

	/**
	 * Factory method to create a template loader to load templates from within
	 * a given subfolder of the WEB-INF/templates folder.
	 * @param templateSet the subfolder to load templates from, or null for the top level folder.
	 * @param nc the Navigator config.
	 * @param sc the Servlet context.
	 * @throws OmException
	 */
	public static TemplateLoader make(String templateSet, NavigatorConfig nc, ServletContext sc) throws OmException {
		String path = sc.getRealPath(nc.getTemplateLocation());
		if (templateSet != null) {
			path += "/" + templateSet;
		}
		return new TemplateLoader(new File(path));
	}

	/**
	 * Load and parse an XHTML template.
	 * @param name the template name. E.g. "templates.xhtml".
	 * @return the parsed template.
	 * @throws IOException
	 */
	public Document loadTemplate(String name) throws IOException
	{
		Document d = XML.parse(new File(templateDirectory + "/" + name));
		return d;
	}

	/**
	 * Load and parse an XHTML template, and deal with the per-question stylesheet tag.
	 * @param name the template name. E.g. "templates.xhtml".
	 * @param removeQuestionCss whether to strip out the link to the per-question stylesheet, if present.
	 * @return the parsed template.
	 * @throws IOException
	 */
	public Document loadTemplate(String name, boolean removeQuestionCss) throws IOException
	{
		Document d = loadTemplate(name);

		Element e = XML.find(d.getDocumentElement(), "ss", "here");
		if (e != null) {
			if (removeQuestionCss)
			{
				XML.remove(e);
			}
			else
			{
				e.removeAttribute("ss");
			}
		}

		return d;
	}

	/**
	 * Load a string template.
	 * @param name the template name.
	 * @return the contents of that template. E.g. "submit.email.txt".
	 * @throws IOException
	 */
	public String loadStringTemplate(String name) throws IOException
	{
		return IO.loadString(new FileInputStream(new File(templateDirectory + "/" + name)));
	}
}
