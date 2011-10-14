package om.qengine.dynamics;

import om.OmException;

import org.w3c.dom.Element;

public interface QuestionHandlerTypeClassBuilding {

	/**
	 * Based on what is provided from omxml the implementor of this interface
	 *  will generate a Java class definition that can then be compiled and
	 *  utilised at runtime by OpenMark.  
	 * 
	 * @param input
	 * @exception
	 * @return
	 * @author Trevor Hinson
	 */
	QuestionRepresentation generateClassRepresentation(Element handler)
		throws OmException;

}
