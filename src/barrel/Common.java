package barrel;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.management.openmbean.InvalidKeyException;

import chord.MakeKeyException;
import chord.pval_error;
import chord.query;
import chord.simulate;


public class Common {

	static Simulation sim;
	
	static int keyLength=60;
	static int levelCount=4;

	public static final int keyTTL = 10; 
	public static final long maxN=(long)Math.pow(2,keyLength)-1;
	static Level[] levels = new Level[Common.levelCount];


	public static final long makeKey(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] k = md5(s);

		long key=(long)Math.abs(k[0]);
		for(int i=1;i<keyLength/8;i++) {
			key=key<<8;
			key+=(long)Math.abs(k[i]);
			if (key<0 || key > maxN) throw new InvalidKeyException();
		}
		return key;
	}

	public static byte[] md5(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		byte[] thedigest=null;
		byte[] ret=null;
		byte[] bytesOfMessage = s.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		thedigest = md.digest(bytesOfMessage);	//128 bit hash
		//outputtoScreen(thedigest.length+"");
		ret=new byte[keyLength/8];
		for (int i=0;i<keyLength/8;i++)			
			ret[i]=thedigest[i];
		return ret;
	}
	
	public static void output(Message m)
	{
		String s;
		//if (q.getKey()!=null) l="("+simulate.shl(q.getCnode().getKey(),q.getKey())+")";
		s="qid="+m.mid+"\tpqid="+m.pmid+"\ttotal="+simulate.tnodes+"\t"+q.qtype.name()+"\tcnode="+q.getCnode().getId()+"\tinode="+q.inode.getId()+"\titer="+q.getIter()+"\tstep="+q.step+"\tqfound="+q.getFound();
		out(s+"\n");
//		if (q.road.size()>1) out+="t"+q.road.get(q.road.size()-2).getId(); 
//		if (out.length()>1)
//		{
//			System.out.print(out);
//			out="";
//		}
	}
	
	public static void out(String s)
	{
		System.out.println(s+"\n");
	}
	
	static boolean inBetween(long i, long e, long k) throws pval_error, MakeKeyException
	{
		if (compareKeys(k, i+1)<compareKeys(e,i+1)) return true;
		return false;
	}
	
	public static long compareKeys(long key1, long key2) throws pval_error, MakeKeyException
	{
		long dist = key1-key2;
		if (dist<0)
			dist=dist+maxN;
		if (dist<0 || dist > maxN) throw new MakeKeyException();
		return dist;
	}

}
