package server;


public class Presence extends Resource{

	private int pres = 200;


	public Presence(String path, String address) {
		super(path, address);
		pres=18;
	}

	public int getPresenceValue() {
		return pres;
	}
	
	public void setPresValue(int p) {
		pres = p;
		
	}
	
}
