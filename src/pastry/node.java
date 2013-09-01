package pastry;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class node 
{
	private byte[] bkey;	//32 bit key uniq
	private int[] key;
	private final int id;		//uniq node number from 1 to max_nodes // also use this as ip-address
	private final int x,y;		//co-ordiantes of node x,y for distance purposes
	private node[][] rtable=new node[common.maxRow][common.maxCol];		//routing table
	private node[] mtable=new node[common.sizeM];	//neighborhood node table
	public int mfilled=0,lfilledF=0,lfilledB=0,rrowfilled=0;
	private node[] ltableF=new node[common.sizeL];	//leaf node table forward
	private node[] ltableB=new node[common.sizeL];	//leaf node table backward
	private int valid=1;
	public static int count=0;
	public static List<node> allnodes=new ArrayList<node>();
	public List<cell> contactN = new ArrayList<cell>();
	private int active;
	
	public int ltime=1,mtime=1,rtime=1;
	
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
		this.bkey=common.md5(Integer.toString(id));
		key=common.makeKey(bkey,common.b);
	}
	
	public node(int id) throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error 
	{
		makeKey(id);
		Random r=new Random();
		this.x=r.nextInt(common.maxX);
		this.y=r.nextInt(common.maxY);
		this.id=id;
		count++;
		allnodes.add(this);
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
	
	static node getNode(int id, node inode)
	{
		if (inode.getId()==id)
			return inode;
		node cnode=inode;
		while((cnode=inode.ltableF[0]).getId()!=id && cnode.getId()!=inode.getId()){}
		if (cnode.getId()!=inode.getId())
			return cnode;
		return null;
	}
	
	public node nextNode()
	{
		return ltableF[0];
	}

	public node getRtable(int a, int b)
	{
		return rtable[a][b];
	}

	@Deprecated
	public void setRtable(node[][] rtable)
	{
		this.rtable = rtable;
		rtime++;
	}
	
	public void setRtable(int a, int b, node val)
	{
		if (val==null || val.getId()==getId()) return;
		this.rtable[a][b] = val;
		rtime++;
	}


	public node getMtable(int a)
	{
		return mtable[a];
	}

	@Deprecated
	public void setMtable(node[] mtable)
	{
		this.mtable = mtable;
	}
	
	public void setMtable(int a, node val)
	{
		this.mtable[a] = val;
		mtime++;
	}


	public node getLtableF(int a)
	{
		return ltableF[a];
	}

	@Deprecated
	public void setLtableF(node[] ltableF)
	{
		this.ltableF = ltableF;
		ltime++;
	}
	
	public void setLtableF(int a, node val)
	{
		this.ltableF[a]=val ;
		ltime++;
	}

	public node getLtableB(int a)
	{
		return ltableB[a];
	}

	@Deprecated
	public void setLtableB(node[] ltableB)
	{
		this.ltableB = ltableB;
		ltime++;
	}
	
	public void setLtableB(int a, node val)
	{
		this.ltableB[a]=val;
		ltime++;
	}

	public int[] getKey()
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
	
	public void insertintoLF(node n) throws pval_error, make_key_error
	{
		if (n.getId()==getId()) return;
		if (lfilledF==0) 
		{
			setLtableF(0,n);
			lfilledF++;
			return;
		}
		
		for (int i=0;i<lfilledF;i++)
			if (getLtableF(i).getId()==n.getId())
				return;
		
		long diff=common.compareKeys(n.getKey(),this.getKey()),done=0,diff2;
		
		
		long diff1=common.compareKeys(getLtableF(0).getKey(),this.getKey());
		//if (diff<0 && lfilledF!=0) return;
		if (diff<0) diff+=(long)Math.pow(2,common.bkeyLength);
		if (diff1<0) diff1+=(long)Math.pow(2,common.bkeyLength);
		
		if (diff<diff1)
		{
			int start=lfilledF-1;
			if (lfilledF==common.sizeL) start--;
			for (int j=start;j>=0;j--)
			{
				ltableF[j+1]=ltableF[j];
			}
			setLtableF(0,n);
			done=1;
		}
		else
		{
			for(int i=0;i<lfilledF;i++)
			{
				diff1=common.compareKeys(n.getKey(),getKey());
				diff2=common.compareKeys(getLtableF(i).getKey(),getKey());
				if (diff1<0) diff1+=(long)Math.pow(2,common.bkeyLength);
				if (diff2<0) diff2+=(long)Math.pow(2,common.bkeyLength);
				if (diff1<diff2)
				{
					int start=lfilledF-1;
					if (lfilledF==common.sizeL) start--;
					for (int j=start;j>=i;j--)
					{
						ltableF[j+1]=ltableF[j];
					}
					setLtableF(i,n);
					done=1;
					break;
				}
			}
		}
		
		if (lfilledF!=common.sizeL) 
		{
			if (done==0)
				setLtableF(lfilledF, n); 
			lfilledF++;
		}
	}

	public void insertintoLB(node n) throws pval_error, make_key_error
	{
		if (n.getId()==getId()) return;
		if (lfilledB==0) 
		{
			setLtableB(0,n);
			lfilledB++;
			return;
		}
		
		for (int i=0;i<lfilledB;i++)
			if (getLtableB(i).getId()==n.getId())
				return;
		
		long diff=common.compareKeys(getKey(),n.getKey()),done=0,diff2;
		long diff1=common.compareKeys(getKey(),getLtableB(0).getKey());
		//if (diff<0 && lfilledF!=0) return;
		//if (n.getId()==6) common.outputtoScreen("#########--------commin diff="+diff+" diff1="+diff1);
		if (diff<0) diff+=(long)Math.pow(2,common.bkeyLength);
		if (diff1<0) diff1+=(long)Math.pow(2,common.bkeyLength);
		//if (n.getId()==6) common.outputtoScreen("#########--------commin diff="+diff+" diff1="+diff1);
		if (diff<diff1)
		{
			
			int start=lfilledB-1;
			if (lfilledB==common.sizeL) start--;
			for (int j=start;j>=0;j--)
			{
				ltableB[j+1]=ltableB[j];
			}
			setLtableB(0,n);
			done=1;
		}
		else
		{
			for(int i=0;i<lfilledB;i++)
			{
				diff1=common.compareKeys(getKey(),n.getKey());
				diff2=common.compareKeys(getKey(),getLtableB(i).getKey());
				if (diff1<0) diff1+=(long)Math.pow(2,common.bkeyLength);
				if (diff2<0) diff2+=(long)Math.pow(2,common.bkeyLength);
				if (n.getId()==6) common.outputtoScreen("#########--------comming diff1="+diff1+" diff2="+diff2);
				if(diff1<diff2)
				{
					int start=lfilledB-1;
					if (lfilledB==common.sizeL) start--;
					for (int j=start;j>=i;j--)
					{
						ltableB[j+1]=ltableB[j];
					}
					setLtableB(i,n);
					done=1;
					break;
				}
			}
		}
		if (lfilledB!=common.sizeL) 
		{
			//if (n.getId()==6) common.outputtoScreen("--------commin");
			if (done==0)
				setLtableB(lfilledB,n);
			lfilledB++;
		}
	}
	
	public void insertintoM(node n)
	{
		if (n.getId()==getId()) return;
		int i=0;
		int dist=common.edistance(n,this);
		int dist1;
		if (mfilled==0)
		{
			setMtable(0,n);
			mfilled++;
			return;
		}
		
		for (i=0;i<mfilled;i++)
			if (getMtable(i).getId()==n.getId())
				return;
		
		dist1=common.edistance(this,getMtable(0));
		i=0;
		while(dist>dist1)
		{
			i++;
			if (i==mfilled) break;
			dist1=common.edistance(this,getMtable(i));
		}
		if (i==mfilled)
		{
			if (i<common.sizeM)
			{
				setMtable(i,n);
				mfilled++;
			}
			return;
		}
		int start=mfilled-1;
		if (mfilled==common.sizeM) start--;
		for (int j=start;j>=i;j--)
		{
			mtable[j+1]=mtable[j];
		}
		setMtable(i,n);
		if (mfilled!=common.sizeM) mfilled++;
		
	}
	
	public void removeLtableF(int in)
	{
		for(int i=in+1;i<lfilledF;i++)
			ltableF[i-1]=ltableF[i];
		lfilledF--;
		setLtableF(lfilledF,null);
	}
	
	
	public void removeLtableB(int in)
	{
		for(int i=in+1;i<lfilledB;i++)
			ltableB[i-1]=ltableB[i];
		lfilledB--;
		setLtableB(lfilledB,null);
	}
	
	public void removeMtable(int in)
	{
		for(int i=in+1;i<mfilled;i++)
			mtable[i-1]=mtable[i];
		mfilled--;;
		setMtable(mfilled,null);
	}
	
	public void removeRtable(int i, int j)
	{
		setRtable(i,j,null);
	}
	
	public boolean updateContactN(node n,int a)	//a=0 L, a=1 M, a=2 R
	{
		boolean found=false;
		Iterator<cell> it = contactN.iterator();
		cell c;
		while (it.hasNext())
		{
			c=it.next();
			if (c.getN().getId()==n.getId())
			{
				c.update(a);
				found=true;
				break;
			}
		}
		return found;
	}
	
	public cell searchContactN(node n)
	{
		Iterator<cell> it=contactN.iterator();
		cell c;
		while (it.hasNext())
		{
			c=it.next();
			if (c.getN().getId()==n.getId()) return c;
		}
		return null;
	}
}
	
