package chord;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class node
{
	public static int count=0;
	public static List<node> allnodes=new ArrayList<node>();
	
	private long key;
	private final int id;		//uniq node number from 1 to max_nodes // also use this as ip-address
	private final int x,y;		//co-ordiantes of node x,y for distance purposes

	private int valid=1;
	private node[] finger = new node[common.keyLength];
	public int ffilled=0;
	
	private node predec;
	
	private int active=0;
	
	
	
	public int isActive()
	{
		return active;
	}
	
	public void setActive(int v)
	{
//		if (active==0 && v==1) simulate.tnodes++;
//		else if (active==1 && v==0) simulate.tnodes--;
		active=v;
	}
	
	final void makeKey(int id) throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error
	{
		key=common.makeKey(Integer.toString(id));
	}
	
	synchronized void countup() {
		count++;
	}
	
	synchronized void countdown() {
		//TODO error if count==0
		count--;
	}
	
	public node(int id,int x, int y) throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error 
	{
		makeKey(id);
		this.x=x;
		this.y=y;
		this.id=id;
		count++;
		allnodes.add(this);
	}
	
	public node getFinger(int i)
	{
		return finger[i];
	}
	
	public void setFinger(int i, node val)
	{
		finger[i]=val;
	}

	public long getKey()
	{
		return key;
	}


	public int getId()
	{
		return id;
	}


	public int getX()
	{
		return x;
	}


	public int getY()
	{
		return y;
	}

	public int isValid()
	{
		return valid;
	}

	public void setValid(int valid)
	{
		this.valid = valid;
	}
	
	node nextNode()
	{
		return finger[0];
	}
	
	
	/**
	 * @param id
	 * @return if null then this is the pred // do query found=1 and send to this.next();
	 * @throws pval_error
	 * @throws make_key_error
	 */
	node findPred(long key) throws pval_error, make_key_error
	{
		long dist=common.compareKeys(key,getKey()+1);
		long dist1;
		node ret=null;
		for (int i=ffilled-1;i>=0;i--)
		{
			dist1=common.compareKeys(finger[i].getKey(),getKey()+1);
			if (dist1<dist)
			{
				ret=finger[i];
				break;
			}
		}
		return ret;
	}
	
	public node getPredec()
	{
		return predec;
	}
	
	public void setPredec(node n)
	{
		predec=n;
	}
	
	public long getStartIndex(int i)
	{
		long ret=(getKey()+(long)Math.pow(2,i))%common.maxN;
		return ret;
	}
	
	public long getRegisterIndex(int i)
	{
		long ret=getKey()-(long)Math.pow(2,i);
		if (ret<0)
			ret=ret+common.maxN;
		return ret;
	}
}
