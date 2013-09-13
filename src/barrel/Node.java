package barrel;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Node {
	public static int count=0;
	public static List<Node> all=new ArrayList<Node>();
	
	private final long key;
	private final int id;		//uniq node number from 1 to max_nodes // also use this as ip-address
	private final int level;
	private final int x,y;		//co-ordiantes of node x,y for distance purposes
	private int valid=0;
	
	private Node[] finger = new Node[Common.keyLength];
	Node uplevel;
	Node predec;
	
	private List<Long> keys=new ArrayList<Long>();
	private List<Wait> wait=new ArrayList<Wait>();
	
	public Node(int id, int x, int y, int level) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		this.id=id;
		this.x=x;
		this.y=y;
		this.level=level;
		key=Common.makeKey(id+"");
		
		count++;
		all.add(this);
		Common.levels[level].all.add(this);
	}
	
	
	
	

}
