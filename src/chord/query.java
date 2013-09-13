package chord;

import java.util.ArrayList;
import java.util.Stack;


public class query
{
	public final node inode;	//initial node on which this query was issued
	private node cnode; //current node on which this query is present
	private int iter=0;
	public final QTYPE qtype; //na, fn
	public int arg1,arg2,arg3,arg4,skip=0;
	public final int qid;
	private long key;
	private int found=0;

	public ArrayList<node> road = new ArrayList<node>();
	public static int counter=0;
	public node sender;
	int step=0; 
	public node extran;
	public int pqid;
	public Wait w;
	
	public query prevq=null;
	
	public Stack<node> recievers=new Stack<node>();
	
	public query(QTYPE type,long key,node n)
	{
		this.qtype=type;
		this.key=key;
		this.inode=n;
		this.cnode=n;
		road.add(n);
		counter++;
		qid=counter;
	}

	public node getCnode()
	{
		return cnode;
	}

	public void setCnode(node cnode) throws valid_not_checked
	{
		//if (cnode.isValid()==0) throw new valid_not_checked();
		sender=this.cnode;
		this.cnode = cnode;
		road.add(cnode);
	}
	
	public long getKey()
	{
		return key;
	}

	public int getIter()
	{
		return iter;
	}

	public void setIter(int iter)
	{
		this.iter = iter;
	}
	
	public void incIter()
	{
		this.iter++;
	}

	public int getFound()
	{
		return found;
	}

	public void setFound(int found)
	{
		this.found = found;
	}
}
