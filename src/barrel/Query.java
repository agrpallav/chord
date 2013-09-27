package barrel;

import java.util.Stack;

public class Query extends Message{
	
	private final long key;
	public final QTYPE qtype;
	
	Stack<Node> sendResposeTo = new Stack<Node>();
	Stack<Message> pendingMsgs = new Stack<Message>();
	
	int fingerNum=0;
	
	public Query(QTYPE qt,long k, Node in, Node cur) {
		super(MTYPE.QUERY,in,cur);
		key=k;
		qtype=qt;
	}
	
	public Query(QTYPE qt,long k, Message pm, Node in, Node cur) {
		super(MTYPE.QUERY,pm,in,cur);
		key=k;
		qtype=qt;
	}
	
	long getKey() {
		return key;
	}

}
