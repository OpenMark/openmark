package om.administration;

import java.util.Map;

import om.AbstractRequestHandlerTestCase;
import om.RequestHandlerSettings;

public class TestAdministrationDisplayRequestHandler extends AbstractRequestHandlerTestCase {

	public void testRenderRequestHandlerSettings() throws Exception {
		AdministrationDisplayRequestHandler handler = new AdministrationDisplayRequestHandler();
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		StringBuffer sb = new StringBuffer();
		for (RequestHandlerSettings rhs : settings.values()) {
			sb.append(handler.renderRequestHandlerSettings("/om-admin", rhs));
		}
		System.out.println(sb);
		assertTrue(sb.toString().contains("/trevor/hinson"));
	}

}
