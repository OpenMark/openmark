package om.tnavigator;

import java.util.HashMap;
import java.util.Map;

import util.misc.LabelSets;

public class TestLabelSet extends LabelSets
{
	public TestLabelSet()
	{
		super(null);

		Map<String, String> labels = new HashMap<String, String>(7);
		labels.put("lTRYAGAIN", "Try again");
		labels.put("lGIVEUP", "Give up");
		labels.put("lNEXTQUESTION", "Next question");
		labels.put("lENTERANSWER", "Enter answer");
		labels.put("lCLEAR", "Clear");
		labels.put("lTRY", "attempt");
		labels.put("lTRIES", "attempts");

		labelReplace.put("!default", labels);
	}
}
