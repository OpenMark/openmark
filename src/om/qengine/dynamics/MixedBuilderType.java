package om.qengine.dynamics;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import om.OmException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.misc.Strings;

public class MixedBuilderType implements QuestionHandlerTypeClassBuilding {

	private static String ERROR_MESSAGE = "Unable to continue as there are not the right number of merging parts :";

	protected static String LINE_SEPERATOR = System.getProperty("line.separator");

	private static String ADDITIONAL_IMPORTS = "additionalImports";

	private static String MIXED = "mixed";

	private static String CLASS_NAME = "className";

	private static String VARIABLES = "variables";

	private static String INIT = "init";

	private static String IS_RIGHT = "isRight";

	private static String EXTENDS = "extends";

	private static String PACKAGE_SUFFIX = "packageSuffix";

	private static int SUBSTITUTIONS = 7;

	private static String TEMPLATED_JAVA = "package om.dynamic.questions.{0};"
		+ LINE_SEPERATOR + "{1}"
		+ LINE_SEPERATOR + "public class {2} {3}["
		+ LINE_SEPERATOR + "{4}"
		+ LINE_SEPERATOR + LINE_SEPERATOR + "public Rendering init(Document d,InitParams ip) throws OmException ["
		+ LINE_SEPERATOR + "{5}"
		+ LINE_SEPERATOR + "]"
		+ LINE_SEPERATOR + LINE_SEPERATOR + "protected abstract boolean isRight(int attempt) throws OmDeveloperException ["
		+ LINE_SEPERATOR + "{6}"
		+ LINE_SEPERATOR + "]"
		+ LINE_SEPERATOR + "]";

	@Override
	public QuestionRepresentation generateClassRepresentation(Element handler)
		throws OmException {
		QuestionRepresentation qr = null;
		if (null != handler) {
			String className = handler.getAttribute(
				QuestionClassBuilder.CLASS_NAME.toString());
			String classDefinition = accomodateForMix(handler);
			qr = new QuestionRepresentation(classDefinition, className);
		}
		return qr;
	}

	protected String accomodateForMix(Element handler) throws OmException {
		String classRepresentation = null;
		if (null != handler) {
			Map<String, String> parts = extractMixedTemplateDetails(handler);
			classRepresentation = generate(parts);
		}
		return classRepresentation;
	}

	protected String generate(Map<String, String> parts) throws OmException {
		List<String> ordered = applyCorrectiveOrdering(parts);
		Object[] arguments = ordered.toArray();
		String output = MessageFormat.format(TEMPLATED_JAVA, arguments);
		output = output.replace("[", "{");
		return output.replace("]", "}");
	}

	protected List<String> applyCorrectiveOrdering(Map<String, String> parts)
		throws OmException {
		List<String> ordered = new ArrayList<String>();
		if (null != parts ? parts.size() == SUBSTITUTIONS : false) {
			String packageSuffix = parts.get(PACKAGE_SUFFIX);
			ordered.add(Strings.isNotEmpty(packageSuffix)
				? packageSuffix : MIXED);
			String imports = parts.get(ADDITIONAL_IMPORTS);
			ordered.add(Strings.isNotEmpty(imports)
				? applyImports(imports) : " ");
			ordered.add(parts.get(CLASS_NAME));
			String extending = parts.get(EXTENDS);
			ordered.add(Strings.isNotEmpty(extending)
				? applyExtends(extending) : " ");
			String variables = parts.get(VARIABLES);
			ordered.add(Strings.isNotEmpty(variables) ? variables : " ");
			String init = parts.get(INIT);
			ordered.add(Strings.isNotEmpty(init) ? init : " ");
			ordered.add(parts.get(IS_RIGHT));
		} else {
			throw new OmException(ERROR_MESSAGE + parts);
		}
		if (ordered.size() != SUBSTITUTIONS) {
			throw new OmException(ERROR_MESSAGE + parts);
		}
		return ordered;
	}

	private String applyExtends(String s) {
		String str = " ";
		if (Strings.isNotEmpty(s)) {
			str = "extends " + s + " ";
		}
		return str;
	}

	private String applyImports(String configuredImports) {
		StringBuilder imports = new StringBuilder()
			.append(getRequiredImports());
		if (Strings.isNotEmpty(configuredImports)) {
			imports.append(configuredImports).append(LINE_SEPERATOR);
		}
		return imports.toString();
	}

	private String getRequiredImports() {
		return new StringBuilder()
			.append(LINE_SEPERATOR).append("import om.OmDeveloperException;")
			.append(LINE_SEPERATOR).append("import om.OmException;")
			.append(LINE_SEPERATOR).append("import om.question.*;")
			.append(LINE_SEPERATOR).append("import om.stdquestion.*;")
			.append(LINE_SEPERATOR).append("import om.helper.*;")
			.append(LINE_SEPERATOR).toString();
	}

	protected Map<String, String> extractMixedTemplateDetails(Element handler)
		throws OmException {
		Map<String, String> results = new HashMap<String, String>();
			String packagingSuffix = handler.getAttribute(PACKAGE_SUFFIX);
			results.put(PACKAGE_SUFFIX, packagingSuffix);
			String className = handler.getAttribute(CLASS_NAME);
			results.put(CLASS_NAME, className);
			String extending = handler.getAttribute(EXTENDS);
			results.put(EXTENDS, extending);
			NodeList nl = handler.getChildNodes();
			if (null != nl) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (null != n ? Node.ELEMENT_NODE == n.getNodeType() : false) {
						Element e = (Element) n;
						results.put(e.getNodeName(), e.getTextContent());
					}
				}
			}
		return results;
	}

}
