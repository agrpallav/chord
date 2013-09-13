package chord;

public class Wait {
	public static final int ATTL=5;
	public static final int QTTL=20;
	
	public int ttl;
	public int ack;
	public query q;
	public node n;
	
	public Wait(int t, int a, query y, node no) {
		ttl=t;
		ack=a;
		q=y;
		n=no;
	}
}
