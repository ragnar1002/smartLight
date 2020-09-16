package server;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class RegistrationResource extends CoapResource{
	
	private final static int MAX_RESOURCES = 5;
	private int roomsRegistered = 0;


	public static final String []Rooms= {"BedRoom 1", "BedRoom 2", "LivingRoom", "BathRoom", "Kitchen"};
		public RegistrationResource(String name) {
			super(name);
		}
		
		public void handleGET(CoapExchange exchange) {
			int nodeId = 0;
			exchange.accept();
		
			
			nodeId = fromPayloadToInt(exchange.getRequestText());		
			String key = nodeIdToRoomName(nodeId);
					
			InetAddress inetAddress = exchange.getSourceAddress();
			CoapClient client = new CoapClient("coap://["+inetAddress.getHostAddress() + "]:5683/.well-known/core");
			
			CoapResponse response = client.get();
			
			String code = response.getCode().toString();
			
			if (!code.startsWith("2")) { //if == 2 correct
				System.out.println("ERROR "+code);
				return;
			}
			
			String responseText = response.getResponseText();
					
			String  []s = responseText.split(";");
			
			String []resPath =  new String[MAX_RESOURCES]; 
			int index = 0;
			for (int i = 0; i<s.length; i++) {
				String []s2 = s[i].split(",");
				if (s2.length > 1) {
					resPath[index++] = s2[1].replaceAll("[\\<>]", "");			
					}
			}
			
			for (int i = 0; i<resPath.length; i++) {
				if(resPath[i]!=null) {
					if (resPath[i].contains("res_presence")){
						Presence p = new Presence(resPath[i], inetAddress.getHostAddress());
						if (!Application.presenceMap.containsValue(p)) {
							Application.presenceMap.put(key, p);
							//observing
							addObserving(p);
						}						
					}
					if (resPath[i].contains("res_light")){
						Light l = new Light(resPath[i], inetAddress.getHostAddress());
						if (!Application.lightMap.containsValue(l)) {
							Application.lightMap.put(key, l);
					
						}					
					}				
				}
				
			}	
			System.out.println("Room " + key + " added!");
			System.out.println(++roomsRegistered+"/"+Rooms.length+ " rooms registered");
			if(roomsRegistered == Rooms.length) {
				System.out.println("Registration complete!");
				Application.waitRegistration = false;
			}
		}
		
		private static void addObserving(Presence p) {
			ObserveClient oc = new ObserveClient(p);
			Application.observeClients.add(oc);
			//start
			Application.observeClients.get(Application.observeClients.size() - 1).startObserve();;
		}
		
		public static int fromPayloadToInt(String p) {
			
			char [] a = p.toCharArray();
			return Character.getNumericValue(a[0]);
			
		}
		
		public static String nodeIdToRoomName(int id){ //get the String (key) for that room
			String room = null;
			room = Rooms[id-2]; // node 1 is the border router
			return room;
		}
}
