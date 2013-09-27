package barrel;

import java.util.ArrayList;

public class Message {
	
	public static int msgCounter=1;
	
	private Node curnode;
	public final Node inode;
	public ArrayList<Node> road = new ArrayList<Node>();
	
	boolean reached=false;
	boolean response=false;
	MTYPE mtype;
	
	public final int mid;
	public final int pmid;
	
	int iter=0;
	int skip=0;
	
	public void incItr() {
		this.iter++;
	}
	
	protected Message(MTYPE mt, Message parmsg, Node in, Node cur) {
		mtype=mt;
		mid=msgCounter++;
		pmid=parmsg.mid;
		
		curnode=cur;
		inode=in;
				
	}
	protected Message(MTYPE mt, Node in, Node cur) {
		mtype=mt;
		mid=msgCounter++;
		pmid=mid;
		
		curnode=cur;
		inode=in;
	}
	
	Node getCnode() {
		return curnode;
	}
	
	void setCnode(Node n) {
		curnode=n;
		road.add(n);
	}
	
	
}
