package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Application {

	public static boolean observeMode = false; 
	public static boolean waitRegistration = true;

	public static HashMap<String, Presence> presenceMap = new HashMap<String, Presence>();
	public static HashMap<String, Light> lightMap = new HashMap<String, Light>();
	

	public static ArrayList <ObserveClient> observeClients = new ArrayList<ObserveClient>();
	public static void main(String[] args) {
			
		
		runServer();
		System.out.println("SmartLight Dashboard");
		System.out.println("Please insert a command");
		
		while (true) {
			System.out.println("1 - Get Resources Info");
			System.out.println("2 - Modify a Light State");
			System.out.println("3 - Observe Resources Mode");
			if (waitRegistration)
				System.out.println("Please wait for the registration of the resources...");
			
			Scanner keyboard = new Scanner(System.in);
			int cmd = keyboard.nextInt();
			int index;
			
			if(cmd == 1) { //presence + light
				
				 getState();
			}		
			if (cmd == 2) { // switch on/off a light status
				
				System.out.println("Select a node by its ID");
				int nodeId = keyboard.nextInt();	
				if(nodeId > 6 || nodeId<= 1) 
					System.out.println("WRONG NODE ID");			
				else
					switchLight(nodeId);
			}
			if (cmd == 3) { //Observe Resources Mode
				System.out.println("This is the Observe Resource mode!");
				System.out.println("Please Insert '0 + Enter' to return to the main menu");
				observeMode = true;
				while(observeMode) {
					int exit = keyboard.nextInt();
					if(exit == 0) {
						observeMode = false;
					}
					
				}
			}
		
		}
	}
	
	public static void runServer() {
		
		new Thread() {
			public void run() {
				Server s = new Server();
				s.startServer();
			}
		}.start();
		
	}
	public static void getState() { //retrieve all the presence states + light

		for (String k: Application.presenceMap.keySet()) {
			printState(k);		
		}
				
	}
	public static void switchLight(int nodeID) {
		int newState;
		String key = RegistrationResource.nodeIdToRoomName(nodeID);
		boolean oldState = lightMap.get(key).getValue();
		
		
		CoapClient client = new CoapClient(lightMap.get(key).getResourceURI());
		
		newState = (!oldState ? 1 : 0);
		
				
		CoapResponse response = client.post("state=" + newState, MediaTypeRegistry.TEXT_PLAIN);
		
		String code = response.getCode().toString();
		
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		lightMap.get(key).setValue(!oldState);
		
		String state = newState == 1 ? "ON" : "OFF";
		System.out.println("Light of room " + key + " is now: " + state);
		
	}
	
	public static void printState(String key) {
		int nodeIndex = Arrays.asList(RegistrationResource.Rooms).indexOf(key) + 2;
		String presence = presenceMap.get(key).getPresenceValue() >= 50 ? "Yes" : "No";		
		String light =  lightMap.get(key).getValue() == true ? "ON":"OFF" ;
		
		System.out.println("Node: " + nodeIndex + " Room: " +key + " Presence: " + presence+ " Light:" + light);
	}

	
	
}
