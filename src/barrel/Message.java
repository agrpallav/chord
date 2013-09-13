package barrel;

import java.util.ArrayList;

public class Message {
	
	Node inode, curnode;
	public ArrayList<Node> road = new ArrayList<Node>();
	
	boolean reached=false;
	MTYPE mtype;
	QTYPE qtype;
}
