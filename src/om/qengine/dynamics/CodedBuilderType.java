package om.qengine.dynamics;

import org.w3c.dom.Element;

public class CodedBuilderType implements QuestionHandlerTypeClassBuilding {

	@Override
	public QuestionRepresentation generateClassRepresentation(Element handler) {
		QuestionRepresentation qr = null;
		if (null != handler) {
			String classDefinition = handler.getTextContent();
			String className = handler.getAttribute(
				QuestionClassBuilder.CLASS_NAME.toString());
			qr = new QuestionRepresentation(classDefinition, className);
		}
		return qr;
	}

}
