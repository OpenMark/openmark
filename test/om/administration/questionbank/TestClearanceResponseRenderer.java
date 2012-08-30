package om.administration.questionbank;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import om.administration.questionbank.ClearanceResponseRenderer.PagingDetails;

import org.junit.Before;
import org.junit.Test;

public class TestClearanceResponseRenderer extends TestCase {

	private Map<String, IdentifiedSuperfluousQuestion> results;

	@Before
	public void setUp() {
		results = new LinkedHashMap<String, IdentifiedSuperfluousQuestion>();
		Set<String> pretendLocations = new HashSet<String>();
		for (int i = 1; i < 2; i++) {
			pretendLocations.add("pretendLocation " + i);
		}
		for (int i = 1; i <= 22 ; i++) {
			IdentifiedSuperfluousQuestion q = new IdentifiedSuperfluousQuestion(
				"QUESTION " + i, pretendLocations);
			results.put("q" + i, q);
		}
	}

	@Test public void testPageResults() throws Exception {
		ClearanceResponseRenderer crr = new ClearanceResponseRenderer();
		PagingDetails pd = crr.new PagingDetails();
		StringBuffer sb = new StringBuffer();
		crr.pageResults(sb, pd, results);
		assertNotNull(sb);
		for (int i = 1; i <= 10 ; i++) {
			assertTrue(sb.toString().contains("QUESTION " + i));
		}
	}

	@Test public void testPageResultsPageOne() throws Exception {
		ClearanceResponseRenderer crr = new ClearanceResponseRenderer();
		PagingDetails pd = crr.new PagingDetails();
		StringBuffer sb = new StringBuffer();
		crr.pageResults(sb, pd, results);
		assertNotNull(sb);
		for (int i = 1; i <= 10 ; i++) {
			assertTrue(sb.toString().contains("QUESTION " + i));
		}
		assertFalse(sb.toString().contains("QUESTION 11"));
	}

	@Test public void testPageResultsPageTwo() throws Exception {
		ClearanceResponseRenderer crr = new ClearanceResponseRenderer();
		PagingDetails pd = crr.new PagingDetails();
		pd.numberPerPage = 10;
		pd.pageNumber = 1;
		StringBuffer sb = new StringBuffer();
		crr.pageResults(sb, pd, results);
		assertNotNull(sb);
		for (int i = 11; i <= 10 ; i++) {
			assertTrue(sb.toString().contains("QUESTION " + i));
		}
		assertFalse(sb.toString().contains("QUESTION 21"));
	}

	@Test public void testPageResultsPageThree() throws Exception {
		ClearanceResponseRenderer crr = new ClearanceResponseRenderer();
		PagingDetails pd = crr.new PagingDetails();
		pd.numberPerPage = 10;
		pd.pageNumber = 2;
		StringBuffer sb = new StringBuffer();
		crr.pageResults(sb, pd, results);
		assertNotNull(sb);
		for (int i = 21; i <= 10 ; i++) {
			assertTrue(sb.toString().contains("QUESTION " + i));
		}
		assertFalse(sb.toString().contains("QUESTION 23"));
	}
}
