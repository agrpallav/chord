package chord;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Level {
	
	static Level[] levels;
	
	public node startNode=null;
	public final int level;
	public List<node> nodes=new ArrayList<node>();
	
	public Level(int level) throws UnsupportedEncodingException, NoSuchAlgorithmException, MakeKeyException, pval_error {
		
		this.level=level;
		
		node n=new node(2+2*level,0,0,level);
		startNode=n;
		
		node m=new node(3+2*level,99,99,level);
		
		for (int i=0;i<common.keyLength;i++)
		{
			n.setFinger(i,n);
			m.setFinger(i,m);
		}
		int i=0;
		while(common.compareKeys(m.getKey(),n.getKey())>common.compareKeys(n.getStartIndex(i),n.getKey()))
		{
			n.setFinger(i,m);
			i++;
			if (i==common.keyLength) break;
		}

		i=0;
		while(common.compareKeys(n.getKey(),m.getKey())>common.compareKeys(m.getStartIndex(i),m.getKey()))
		{
			m.setFinger(i,n);
			i++;
			if (i==common.keyLength) break;
		}

		
		n.setPredec(m);
		m.setPredec(n);
		n.setActive(1);
		m.setActive(1);
		n.ffilled=common.keyLength;
		m.ffilled=common.keyLength;
	}
	
	void validate() throws validate_failed, pval_error, MakeKeyException
	{
		int i=1;
		node s=startNode;
		long diff;
		while((s=s.nextNode()).getId()!=startNode.getId())
		{
			i++;
			if (i>common.maxNodes) throw new validate_failed();

			for (int j=0;j<common.keyLength;j++)
				if (!common.inBetween(s.getKey(), s.getFinger(j).getKey(), s.getStartIndex(j))) throw new validate_failed();
		}
		i++;
		int ss=nodes.size();
		if (ss-i>1) throw new validate_failed();
	}
	
}
