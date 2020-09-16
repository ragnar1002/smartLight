package server;

public class Light extends Resource{
	
	private boolean value;

	public Light(String path, String address) {
		super(path, address);
		value = false;
	}
	
	public boolean getValue() {
		return this.value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
}
