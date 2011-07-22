package om;

import java.util.Map;

public abstract class AbstractRequestHandlerTestCase extends AbstractTestCase {

	protected static String REQUEST_HANDLER_XML = "requestHandling.xml";

	protected RequestHandlingConfiguration configuration;

	public void setUp() throws Exception {
		super.setUp();
		configuration = new RequestHandlingConfiguration(
			pickUpFile(REQUEST_HANDLER_XML));
	}

	public void testConfigurationLoading() throws Exception {
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		assertTrue(settings.size() == 2);
	}

}
