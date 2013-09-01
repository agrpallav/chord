package pastry;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;

import chord.simulate;

public class common {

	public static final int b=4;
	public static final int maxNodes=10000;
	public static final int bkeyLength=32;
	public static final int keyLength=bkeyLength/b;
	public static final int maxX=10000;
	public static final int maxY=10000;
	public static final int sizeM=15;	//size of neighborhood set
	public static final int sizeL=8;	//size of leaf set (keep it even)
	public static final int maxRow=(int) Math.ceil((float)bkeyLength/b), 
							maxCol=(int) Math.pow(2,b);
	public static final long maxN=(long)Math.pow(2,common.bkeyLength); 
	
	public static final ReentrantLock gLock=new ReentrantLock(); // used when simulation specific code requires the entire structure to remain constant
	
	public static byte[] md5(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] thedigest=null;
		byte[] ret=null;
		byte[] bytesOfMessage = s.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		thedigest = md.digest(bytesOfMessage);	//128 bit hash
		//outputtoScreen(thedigest.length+"");
		ret=new byte[bkeyLength];
		for (int i=0;i<bkeyLength/8;i++)			// extracting out the first leyLength bits 
			ret[i]=thedigest[i];
		return ret;
	}
	
	public static int shl(int[] k1, int[] k2)
	{
		int i=0;
		while(k1[i]==k2[i]) i++;
		return i;
	}
	
	public static int calcInBase(byte[] k,int b)		//assuming MSB in [3(or b)]
	{
		int ret=0,pval=1;
		for (int i=0;i<b;i++)
		{
			ret+=pval*k[i];
			pval=pval*2;
		}
		return ret;
	}
	static String out;
	public static void output(query q)
	{
		String l="";
		//if (q.getKey()!=null) l="("+simulate.shl(q.getCnode().getKey(),q.getKey())+")";
		out+="\n"+q.qid+"\t"+q.pqid+"\t"+simulate.tnodes+"\t"+q.qtype.name()+"\t"+q.getCnode().getId()+"\t"+q.getIter()+"\tstep="+q.step+"\tqfound=0";
		if (q.road.size()>1) out+="t"+q.road.get(q.road.size()-2).getId();
		if (out.length()>100000)
		{
			System.out.print(out);
			out="";
		}
	}
	
	public static void outputtoScreen(String s)
	{
		//System.out.println(s+"\n");
	}
	
	
	/**
	 * @param k the binary array
	 * @param b to be converted to base b
	 * @return
	 * @throws make_key_error 
	 * @throws Exception 
	 */
	public static final int[] makeKey(byte[] k1,int b) throws make_key_error
	{
		byte[] k = new byte[bkeyLength/8];
		for (int i=0;i<k.length;i++)
			k[i]=k1[i];
		int l=(int)Math.ceil((float)k.length*8/b);
		int[] key=new int[l];
//		byte[] tkey;
//		for (int i=0;i<l;i++)
//		{
//			tkey=new byte[b];
//			for(int j=0;j<b && b*i+j<k.length;j++)
//				tkey[j]=k[b*i+j];
//			key[i]=calcInBase(tkey,b);
//		}
//		return key;
		for (int i=0;i<l;i++)
		{
			if (i%2==0)
			{
				key[i]=k[i/2]>>4 & 0xf;
			}
			else
			{
				key[i]=k[i/2] & 0xf;
			}
			if (key[i]<0 || key[i]>15) 
			{
				outputtoScreen(key[i]+"");
				throw new make_key_error();
			}
		
		}
		return key;
	}
	
	/**
	 * validating that all nodes have different keys
	 * @param inode
	 */
	public static int validate(node inode)
	{	//TODO
		return 1;
	}
	
	/**
	 * Comparing keys	if key1 is bigger value +ve and vice versa.
	 * the absolute value gives the distance between keys.
	 * @param key1
	 * @param key2
	 * @return
	 * @throws pval_error 
	 * @throws make_key_error 
	 */
	public static long compareKeys(int[] key1, int[] key2) throws pval_error, make_key_error
	{
		int i=keyLength-1;
		long pval=1,dist=0;
		if (i==-1) return 0;
		long dec=(long)Math.pow(2,b);
		pval=(long)Math.pow(2,keyLength*b);
		//outputtoScreen("\nStarting pval="+pval);
		for (int j=0;j<=i;j++)
		{
			pval=pval/dec;
			if (pval==0)
			{
				System.out.println("\nError: pval = 0 j="+j+"keyl="+keyLength);
				//Thread.dumpStack();
				throw new pval_error();
			}
			if (key1[j]<0 || key2[j]<0) throw new make_key_error();
			dist+=pval*((long)(key1[j]-key2[j]));
			
		}
		if (pval!=1)
		{
			System.out.println("\nError: pval != 1 pval="+pval);
			//Thread.dumpStack();
			throw new pval_error();
		}
//		int half=(long)Math.pow(2,bkeyLength-1);
//		if (dist>half)
//			dist=(-1)*(dist%half);
//		else if (dist<(-1)*half)
//			dist=(-1*dist)%half;
		if (dist>common.maxN) throw new make_key_error();
		return dist;
	}
	
	public static int edistance (node a, node b)
	{
		return (int)Math.abs(a.getX()-b.getX())+(int)Math.abs(a.getY()-b.getY());
	}
}
