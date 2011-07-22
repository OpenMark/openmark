package om;

import java.util.Map;

public class RequestHandlingConfigurationTests extends AbstractRequestHandlerTestCase {

	public void testInterogateSettings() throws Exception {
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		assertTrue(settings.size() > 0);
		assertNull(settings.get("/tester"));
		System.out.println(settings);
		RequestHandlerSettings rhs = settings.get("/trevor/hinson");
		assertNotNull(rhs);
	}

	public void testNulled() throws Exception {
		RequestHandlingConfiguration configuration
			= new RequestHandlingConfiguration(null);
		assertTrue(configuration.getSettings().size() == 0);
	}

	public void testValidRequestHandlerSettings() throws Exception {
		assertTrue(configuration.getSettings().size() == 2);
		System.out.println(configuration.getSettings());
		RequestHandlerSettings rhs = configuration.getRequestHandlerSettings("/trevor/hinson");
		assertNotNull(rhs);
		assertTrue(rhs.valid());
	}
	
}
