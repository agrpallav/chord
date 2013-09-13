package barrel;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.management.openmbean.InvalidKeyException;


public class Common {

	static Simulation sim;
	
	static int keyLength=60;
	static int levelCount=4;

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

}
