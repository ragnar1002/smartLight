package server;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;

public class Server extends CoapServer{

		static {
			CaliforniumLogger.disableLogging();
		}
	
	public void startServer() {
		this.add(new RegistrationResource("registration"));
		this.start();
		
	}

}
