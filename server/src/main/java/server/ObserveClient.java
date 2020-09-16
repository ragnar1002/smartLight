package server;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class ObserveClient extends CoapClient {

	CoapObserveRelation cor;
	private Presence presence;
	String key;
	
	public ObserveClient(Presence p) {
		super(p.getResourceURI());
		presence = p;
		
	}
	
	public void startObserve() {
		cor = this.observe(new CoapHandler() {
			public void onLoad(CoapResponse response) {
				
				try {
					String value;
					
					JSONObject jo = (JSONObject) JSONValue.parseWithException(response.getResponseText());
					if(jo.containsKey("presence")) {
						value = jo.get("presence").toString();

						int valuePres = Integer.parseInt(value.trim());
						key = getKey(Application.presenceMap, presence);
						
						Light l = Application.lightMap.get(key);
						
						
						if (valuePres < 50) {										
							l.setValue(false);		
							
						}
						else if (valuePres >= 50) {
							l.setValue(true);	
							
						}
						Application.presenceMap.get(key).setPresValue(valuePres);
						
					}
					else {
						System.out.println("Presence value not found!");
						return;
					}
					
					
					if(Application.observeMode == true) {
						Date date = new Date();
						long time = date.getTime();
						Timestamp ts = new Timestamp(time);
						System.out.println("TimeStamp: "+ts);
						Application.printState(key);
						
					}
				}catch(Exception e) {e.printStackTrace();}
			}
			public void onError() {
				System.out.println("Request timeout or rejected in observing");
			}
			
		}
								
		);
				
				
	}
	
	public static <String, Presence> String getKey(Map <String, Presence> map, Presence p) {
		for(String k: map.keySet()) {
			if(p.equals(map.get(k))) {
				return k;
			}
		}
		
		return null;
	}
}
