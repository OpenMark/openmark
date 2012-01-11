package util.misc;

import java.util.HashMap;
import java.util.Map;

/* holds trafficlights in 2 forms, a concatenated string, and a map of axis and traffic light value */

public class TrafficLights {
		Map <String,String> trafficlights = new HashMap<String,String>();
		
		private static String NOVALUE2="No trafficlight for this axis";
		private static String NOVALUE1="_";
		private static String NOAXIS="No value for axis name";
		private static String KEYPAIRSEPERATOR="=";
		private static String SEPERATOR=",";

	public TrafficLights() {
		//blank constructor
	}
	
	
	/**
	 * @param prefix String set the axis name of the trafficlight pair.
	 * @param prefix String set the value of the trafficlight pair.
	 */	
	public TrafficLights(String axis,String value) {
		trafficlights.put(axis,value);
	}

	
	/**
	 * @param prefix String add the axis name of the trafficlight pair to the map.
	 * @param prefix String add the value of the trafficlight pair to the map.
	 */	
	public void addTrafficLights(String axis,String value) {
		trafficlights.put(axis,value);
	}


	public String getTrafficLightValuesAsString()
	{
		StringBuffer tls=new StringBuffer();
		for (Map.Entry<String, String> me : trafficlights.entrySet()) {
			// Get create the score
			String value = me.getValue().equals("")?NOVALUE1:me.getValue();
			tls.append(value);
		}
	    return tls.toString();
	}
	
	public Map<String,String> getTrafficlightPairs()
	{
		return trafficlights;
	}
	
	public String getTrafficLightPairsAsString()
	{
		StringBuffer tls=new StringBuffer();
		String s="";
		for (Map.Entry<String, String> me : trafficlights.entrySet()) {
			// Get create the score
			String axis = me.getKey().equals("")?NOAXIS:me.getKey();
			String value = me.getValue().equals("")?NOVALUE2:me.getValue();
			tls.append(s);tls.append(axis); tls.append(KEYPAIRSEPERATOR);tls.append(value);
			s=SEPERATOR;
		}
	    return tls.toString();
	}

}
