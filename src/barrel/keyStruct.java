package barrel;

public class keyStruct {
	long key;
	int ttl;
	
	public keyStruct(long k) {
		ttl=Common.keyTTL;
		key=k;
	}
}