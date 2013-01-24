package om.administration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import om.AbstractRequestHandlerTestCase;
import om.abstractservlet.RequestHandlerSettings;

import org.junit.Test;

public class TestAdministrationDisplayRequestHandler extends AbstractRequestHandlerTestCase {

	@Test public void testRenderRequestHandlerSettings() throws Exception {
		AdministrationDisplayRequestHandler handler = new AdministrationDisplayRequestHandler();
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		StringBuffer sb = new StringBuffer();
		for (RequestHandlerSettings rhs : settings.values()) {
			sb.append(handler.renderRequestHandlerSettings("/om-admin", rhs));
		}
		assertTrue(sb.toString().contains("/trevor/hinson"));
	}

}
