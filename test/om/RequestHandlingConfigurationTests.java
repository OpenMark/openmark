package om;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import om.abstractservlet.AbstractRequestHandlerTestCase;
import om.abstractservlet.RequestHandlerSettings;
import om.abstractservlet.RequestHandlingConfiguration;

import org.junit.Test;

public class RequestHandlingConfigurationTests extends AbstractRequestHandlerTestCase {

	@Test public void testInterogateSettings() throws Exception {
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		assertTrue(settings.size() > 0);
		assertNull(settings.get("/tester"));
		RequestHandlerSettings rhs = settings.get("/trevor/hinson");
		assertNotNull(rhs);
	}

	@Test public void testNulled() throws Exception {
		RequestHandlingConfiguration configuration
			= new RequestHandlingConfiguration(null);
		assertTrue(configuration.getSettings().size() == 0);
	}

	@Test public void testValidRequestHandlerSettings() throws Exception {
		assertTrue(configuration.getSettings().size() == 2);
		RequestHandlerSettings rhs = configuration.getRequestHandlerSettings("/trevor/hinson");
		assertNotNull(rhs);
		assertTrue(rhs.valid());
	}
	
}
