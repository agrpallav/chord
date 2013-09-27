package chord;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;

public class common {

	public static final int b=4;
	public static final int maxNodes=10000;
	public static final int keyLength=60;
	public static final int maxX=10000;
	public static final int maxY=10000;
	public static final int sizeL=8;	//size of left and right leaf set 
	public static final long maxN=(long)Math.pow(2,keyLength)-1;
	public static final ReentrantLock gLock=new ReentrantLock(); // used when simulation specific code requires the entire structure to remain constant
	
	public static byte[] md5(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] thedigest=null;
		byte[] ret=null;
		byte[] bytesOfMessage = s.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		thedigest = md.digest(bytesOfMessage);	//128 bit hash
		//outputtoScreen(thedigest.length+"");
		ret=new byte[keyLength/8];
		for (int i=0;i<keyLength/8;i++)			// extracting out the first keyLength bits 
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
	static String out="";
	public static void output(query q)
	{
		String l="";
		//if (q.getKey()!=null) l="("+simulate.shl(q.getCnode().getKey(),q.getKey())+")";
		out+="\nqid="+q.qid+"\tpqid="+q.pqid+"\ttotal="+simulate.tnodes+"\t"+q.qtype.name()+"\tcnode="+q.getCnode().getId()+"\tinode="+q.inode.getId()+"\titer="+q.getIter()+"\tstep="+q.step+"\tqfound="+q.getFound();
//		if (q.road.size()>1) out+="t"+q.road.get(q.road.size()-2).getId(); 
		if (out.length()>1)
		{
			System.out.print(out);
			out="";
		}
	}
	
	public static void out(String s)
	{
		System.out.println(s+"\n");
	}
	
	
	/**
	 * @param k the binary array
	 * @param b to be converted to base b
	 * @return
	 * @throws MakeKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws Exception 
	 */
	public static final long makeKey(String s) throws MakeKeyException, UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] k = md5(s);
		
		long key=(long)Math.abs(k[0]);
		for(int i=1;i<keyLength/8;i++) {
			key=key<<8;
			key+=(long)Math.abs(k[i]);
		
			if (key<0 || key > maxN) throw new MakeKeyException();
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
	 * @throws MakeKeyException 
	 */
	public static long compareKeys(long key1, long key2) throws pval_error, MakeKeyException
	{
		long dist = key1-key2;
		if (dist<0)
			dist=dist+maxN;
		if (dist<0 || dist > maxN) throw new MakeKeyException();
		return dist;
	}
	
	public static int edistance (node a, node b)
	{
		return (int)Math.abs(a.getX()-b.getX())+(int)Math.abs(a.getY()-b.getY());
	}
	
	synchronized void write(String s){
		
	}
	
	synchronized public void loge(String s) {
		
	}
	
	static boolean inBetween(long i, long e, long k) throws pval_error, MakeKeyException
	{
		if (compareKeys(k, i+1)<=compareKeys(e,i+1)) return true;
		return false;
	}
}
