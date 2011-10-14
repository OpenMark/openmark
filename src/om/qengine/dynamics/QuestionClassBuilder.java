package om.qengine.dynamics;

import java.util.HashMap;
import java.util.Map;

import om.OmException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

public class QuestionClassBuilder {

	private static String HANDLER_TYPE = "handlerType";

	public static String CLASS_NAME = "className";

	private Map<String, QuestionHandlerTypeClassBuilding> builders
		= new HashMap<String, QuestionHandlerTypeClassBuilding>();

	public QuestionClassBuilder() {
		builders.put(QuestionBuilderTypes.Coded.toString(),
			new CodedBuilderType());
		builders.put(QuestionBuilderTypes.Mixed.toString(),
			new MixedBuilderType());
	}

	public QuestionRepresentation generate(Element input)
		throws OmException {
		QuestionHandlerTypeClassBuilding qcb = null;
		if (null != input) {
			String handler = input.getAttribute(HANDLER_TYPE);
			if (StringUtils.isNotEmpty(handler)) {
				qcb = builders.get(handler);
			}
		}
		return null != qcb ? qcb.generateClassRepresentation(input) : null;
	}

}
