package pastry;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class cell
{
	node n;
	int ltime,mtime,rtime;
	
	cell(node n) throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error
	{
		this.n=n;
		ltime=n.ltime;
		mtime=n.mtime;
		rtime=n.rtime;
	}

	public node getN()
	{
		return n;
	}
	void update(int a)
	{
		if (a==0) ltime=n.ltime;
		else if (a==1) mtime=n.mtime;
		else if (a==2) rtime=n.rtime;
	}
//	
//	public void setN(node n)
//	{
//		this.n=n;
//		ltime=n.ltime;
//		mtime=n.mtime;
//		rtime=n.rtime;
//	}
}
