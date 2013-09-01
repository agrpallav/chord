package pastry;

import java.util.ArrayList;

public class query
{
	public final node inode;	//initial node on which this query was issued
	private node cnode; //current node on which this query is present
	public node extran;	// used in na for storing the node to add	// used in d for storing the destination
	private int iter=0;
	public final QTYPE qtype; //na, fn
	public int arg1,arg2,arg3,arg4,skip=0;
	public final int qid;
	private int[] key;
	public query pquery; //parent query
	public int cmsgs=0;	//messages sent by child queries
	@Deprecated
	public String state="f";	//possible states: f (find), r (reached) 
	public int found=0;
	public ArrayList<node> road = new ArrayList<node>();
	public static int counter=0;
	public int step=0;
	public node sender;
	public int ltime,mtime,rtime;
	public int pqid;
	
	query(QTYPE type,int[] key,node n)
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
		if (cnode.isValid()==0) throw new valid_not_checked();
		sender=this.cnode;
		this.cnode = cnode;
		road.add(cnode);
	}

	public int[] getKey()
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
	
	public boolean keyFound()
	{
		return state.equals("r"); 
	}
	
	public void finish()
	{
		//TODO
	}
}
