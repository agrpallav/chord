package barrel;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chord.MakeKeyException;
import chord.pval_error;

public class Node {
	public static int count=0;
	public static List<Node> all=new ArrayList<Node>();
	
	private final long key;
	private final int id;		//uniq node number from 1 to max_nodes // also use this as ip-address
	private final int level;
	private final int x,y;		//co-ordiantes of node x,y for distance purposes
	private int valid=0;
	
	private Node[] finger = new Node[Common.keyLength];
	public int ffilled=0;
	
	public Node uplevel=null;
	Node predec;
	
	private List<keyStruct> keys=new ArrayList<keyStruct>();
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
	
	public Node getPredec() {
		return predec;
	}
	
	public void setPredec(Node n) {
		predec=n;
	}
	
	long getKey() {
		return key;
	}
	
	public long getStartIndex(int i) {
		long ret=(getKey()+(long)Math.pow(2,i))%Common.maxN;
		return ret;
	}
	
	public long getRegisterIndex(int i)
	{
		long ret=getKey()-(long)Math.pow(2,i);
		if (ret<0)
			ret=ret+Common.maxN;
		return ret;
	}
	
	/**
	 * @param id
	 * @return if null then this is the pred // do query found=1 and send to this.next();
	 * @throws pval_error
	 * @throws MakeKeyException
	 */
	Node findPred(long key) throws pval_error, MakeKeyException {
		long dist=Common.compareKeys(key,getKey()+1);
		long dist1;
		Node ret=null;
		for (int i=ffilled-1;i>=0;i--)
		{
			dist1=Common.compareKeys(finger[i].getKey(),getKey()+1);
			if (dist1<=dist)
			{
				ret=finger[i];
				break;
			}
		}
		return ret;
	}
	
	Node nextNode() {
		return finger[0];
	}
	
	keyStruct hasKey(long key) {
		Iterator<keyStruct> it = keys.iterator();
		keyStruct k;
		while(it.hasNext()) {
			k=it.next();
			if (k.key==key) { 
				if (k.ttl>0) return k;
				else break;
			}
		}
		return null;
	}
	
	void addKey(long key) {
		keyStruct k=hasKey(key);
		if (k==null) keys.add(new keyStruct(key));
		else k.key=key;
	}
	
	public void setFinger(int i, Node val)
	{
		finger[i]=val;
	}
	
	public Node getFinger(int i)
	{
		return finger[i];
	}
	
}
