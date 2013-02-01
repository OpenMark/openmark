package om.abstractservlet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import om.AbstractTestCase;
import om.abstractservlet.RequestHandlerSettings;
import om.abstractservlet.RequestHandlingConfiguration;

import org.junit.Before;
import org.junit.Test;

public abstract class AbstractRequestHandlerTestCase extends AbstractTestCase {

	protected static String REQUEST_HANDLER_XML = "requestHandling.xml";

	protected RequestHandlingConfiguration configuration;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		configuration = new RequestHandlingConfiguration(
			pickUpFile(REQUEST_HANDLER_XML, AbstractRequestHandlerTestCase.class));
	}

	@Test public void testConfigurationLoading() throws Exception {
		assertNotNull(configuration);
		Map<String, RequestHandlerSettings> settings = configuration.getSettings();
		assertNotNull(settings);
		assertTrue(settings.size() == 2);
	}

}
