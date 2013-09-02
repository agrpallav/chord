package chord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



public class simulate
{
	final String inputF="Input";

	node startNode=null;
	BlockingQueue<query> queryq=new LinkedBlockingQueue<query>();
	public query currentq=null;
	node curnode;
	int totalNodes=2;
	List<query> qbuf=new ArrayList<query>();
	query parentq;
	public static int tnodes=2;

	public static void main(String[] args) throws Exception
	{
		simulate obj = new simulate();
		obj.init();

		//		if (args.length>0 && args[0].equals("1"))
		//		{
		//			common.outputtoScreen("Making input file");
		//			obj.makeNodes();
		//		}
		common.out("Processing input");
		obj.processInput();
	}

	void processInput() throws NoSuchAlgorithmException, make_key_error, NumberFormatException, IOException, invalid_input, valid_not_checked, pval_error, infinite_loop, wrong_routing, validate_failed
	{
		@SuppressWarnings("resource")
		BufferedReader in = new BufferedReader(new FileReader(inputF));

		String text;
		int arg1,arg2,arg3;
		String[] word;
		node n;
		query q=null;
		common.out("Process Input starting loop");

		while ((text=in.readLine())!=null)
		{
			if (text.subSequence(0,1).equals("#")) continue;
			//common.out("\nProcessing: "+text);

			word=text.split("\t");

			if (word[0].equals("na"))
			{
				arg1=Integer.parseInt(word[2]); //id
				arg2=Integer.parseInt(word[3]); //x
				arg3=Integer.parseInt(word[4]); //y
				n=new node(arg1,arg2,arg3);
				q=addNode(n);
			}
			else if(word[0].equals("nf"))
			{
				int[] done=new int[common.maxNodes];
				node s=startNode;
				done[s.getId()]=1;
				while((s=s.nextNode()).getId()!=startNode.getId()){done[s.getId()]=1;}
				int c=0;
				for (int i=1;i<common.maxNodes;i++)
					if (done[i]==0) c++;
				common.out("--------------"+c+"");

				//					arg1=Integer.parseInt(word[2]); //id
				//					arg2=Integer.parseInt(word[3]); //snid
				//					getNodebyId(100);
				//					n=getNodebyId(arg2); //find the node from snid
				//					
				//					q=new query(QTYPE.NODEFIND,common.makeKey(common.md5(word[2]), common.b), n);
			}
			else if(word[0].equals("nd"))
			{
				q=new query(QTYPE.NODEDEL,0,startNode);
				arg1=Integer.parseInt(word[2]); //id
				q.arg1=arg1;
			}
			else 
			{
				common.out(word[0]);
				throw new invalid_input();
			}

			//common.outputtoScreen("Checking time");

			if (word[1].equals("apc"))
			{
				start();
				enq(q);
			}
			else if(word[1].equals("ap"))
			{
				arg1=Integer.parseInt(word[word.length-1]); //delay
				q.skip=arg1;
				enq(q);
			}
			else if(word[1].equals("wp"))
			{
				enq(q);
			}
			else 
			{
				common.out(word[1]);
				throw new invalid_input();
			}
		}
		start();

		in.close();
	}

	void init() throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error, pval_error, valid_not_checked, infinite_loop, wrong_routing, validate_failed
	{
		node n=new node(2,0,0);
		node m=new node(3,99,99);
		//		n.setFinger(0,m);
		//		n.setPredec(m);
		//		n.ffilled++;
		//		m.setFinger(0,n);
		//		m.setPredec(n);
		//		m.ffilled++;
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

		startNode=n;
		n.setPredec(m);
		m.setPredec(n);
		n.setActive(1);
		m.setActive(1);
		n.ffilled=common.keyLength;
		m.ffilled=common.keyLength;
		//		query q = new query(QTYPE.NODEADD,m.getKey().and(BigInteger.ONE),m);
		//		q.setCnode(n);
		//		q.arg1=0;
		//		enq(q);
		//start();

	}

	query addNode(node n) throws valid_not_checked 
	{
		//common.gLock.lock();
		node nn=nearestNode(n);
		//common.gLock.unlock();

		query q= new query(QTYPE.NODEADD,n.getKey()+1,n);
		q.setCnode(nn);
		q.arg1=0;

		//		n.setMtable(0,nn);
		//		for(int i=0;i<nn.mfilled && i<common.sizeM-1;i++)
		//			n.setMtable(i+1,nn.getMtable(i));
		//		n.mfilled=nn.mfilled+1;
		//		if (n.mfilled>common.sizeM) n.mfilled--;
		return q;
	}

	node nearestNode(node n)
	{
		node cnode=startNode,minNode=startNode;
		int min=common.edistance(n,cnode),dist;
		while ((cnode=cnode.nextNode()).getId()!=startNode.getId())
		{
			dist=(int)Math.abs(n.getX()-cnode.getX())+(int)Math.abs(n.getY()-cnode.getY());
			if (min>dist && cnode.isValid()==1) 
			{
				min=dist;
				minNode=cnode;
			}
		}
		return minNode;
	}

	void start() throws valid_not_checked, pval_error, make_key_error, UnsupportedEncodingException, NoSuchAlgorithmException, infinite_loop, wrong_routing, validate_failed 
	{
		//common.out("Start called");
		int i=0;
		parentq=queryq.peek();
		while(!queryq.isEmpty())
		{
			i++;
			processq(queryq.poll());
			//if (i>10000) throw new infinite_loop();

		}validate();
		i=0;
		if (parentq!=null && parentq.qtype==QTYPE.NODEADD) tnodes++;
		parentq=null;
		//		dumpstate();

	}


	void enq(query q)
	{
		if (q.getCnode()==null) 
		{
			common.out("Enq query is null");
			Thread.dumpStack();
			System.exit(0);
		}

		if (parentq==null) q.pqid=q.qid;
		else q.pqid=parentq.qid;
		query tq;
		for (int i=0;i<qbuf.size();i++)
		{
			tq=qbuf.get(i);
			if (tq.getCnode().getId()==q.getCnode().getId() && tq.inode.getId()==q.inode.getId() && tq.arg1==q.arg1)
			{
				if (tq.qtype==q.qtype)
					return;
			}
		}
		qbuf.add(q);
		queryq.add(q);
	}

	void processq(query q) throws pval_error, make_key_error, valid_not_checked, wrong_routing, validate_failed
	{
		q.incIter();
		common.output(q);
		this.curnode=q.getCnode();
		currentq=q;
		node tn;
		qbuf.clear();

		if (q.skip>0) 
		{
			q.skip--;
			enq(q);
			return;
		}

		if (q.qtype==QTYPE.NODEFIND)
		{
			if (q.step==0)
			{
				tn = curnode.findPred(q.getKey());
				if (tn==null)
				{
					q.step=1;
					q.setCnode(curnode.nextNode());
					enq(q);
					return;
				}
				else
				{
					q.setCnode(tn);
					enq(q);
					return;
				}
			}
		}
		else if (q.qtype==QTYPE.NODEADD)
		{
			tn = curnode.findPred(q.getKey());
			if (tn==null)
			{
				query tq=new query(QTYPE.FINGER,0,curnode);
				tq.setCnode(q.inode); //q.inode is the node we are trying to add.
				tq.arg1=q.arg1;
				enq(tq);

				if (!common.inBetween(q.inode.getKey(), curnode.nextNode().getKey(), q.inode.getStartIndex(q.arg1))) throw new validate_failed();

				return;
			}
			else
			{
				q.setCnode(tn);
				enq(q);
				return;
			}
		}
		else if (q.qtype==QTYPE.FINGER)
		{
			node toset=q.inode.nextNode();
			// if (toset==null) toset=q.inode;

			if (curnode.ffilled==0) {
				curnode.setPredec(q.inode);
				curnode.setFinger(0,toset);
				curnode.ffilled++;
				nodeComplete(curnode);
			}

			int in=q.arg1;
			while (// q.arg1==curnode.ffilled ||
					in<common.keyLength &&
					(in==curnode.ffilled &&
					common.compareKeys(curnode.getStartIndex(in),curnode.getKey()+1)<=common.compareKeys(toset.getKey(),curnode.getKey()+1))
					|| (in<curnode.ffilled &&
							common.compareKeys(toset.getKey(),curnode.getStartIndex(in)+1)<=common.compareKeys(curnode.getFinger(in).getKey(),curnode.getStartIndex(in)+1)))
			{
				curnode.setFinger(in,toset);
				if (in==curnode.ffilled) curnode.ffilled++;
				if (in<common.keyLength) in++;
			}


			if (curnode.isActive()==0) 
			{
				if (curnode.ffilled==common.keyLength)
				{
					curnode.setActive(1);
					return;
				}
				else
				{
					long next=curnode.getStartIndex(curnode.ffilled);
					query tq = new query(QTYPE.NODEADD,next,curnode);
					tq.setCnode(curnode.nextNode());
					tq.arg1=curnode.ffilled;
					enq(tq);
				}
			}
			//			
			//			while (common.compareKeys(q.getKey(),curnode.getKey()).compareTo(common.compareKeys(curnode.getFinger(curnode.ffilled-1).getKey(),curnode.getKey()))<0)
			//			{
			//				curnode.setFinger(curnode.ffilled,toset);
			//				curnode.ffilled++;
			//				if (curnode.ffilled==common.bkeyLength) 
			//				{
			//					nodeComplete(curnode);
			//					return;
			//				}
			//			}
		}

		else if (q.qtype==QTYPE.REGISTER)
		{
			if (q.arg1==-1)
			{
				curnode.setPredec(q.inode);
				return;
			}
			if (q.step==0)
			{
				tn=curnode.findPred(q.getKey());
				if (tn==null)
				{
					q.step=1;
					if (q.arg1>=curnode.ffilled) {
						q.setCnode(curnode.getPredec());
						enq(q);
						return;
					}
					if (curnode.getFinger(q.arg1).getKey()== q.inode.getKey()) return;
					if (curnode.getFinger(q.arg1)!=null && common.inBetween(curnode.getStartIndex(q.arg1), curnode.getFinger(q.arg1).getKey(), q.inode.getKey())) {
						curnode.setFinger(q.arg1,q.inode);
						q.setCnode(curnode.getPredec());
						enq(q);
					}
					
					//else throw new wrong_routing();
					return;
				}
				q.setCnode(tn);
				enq(q);
				return;
			}
			else
			{
				if (curnode.getKey()==q.getKey()) return;
				if (q.arg1>=curnode.ffilled) {
					q.setCnode(curnode.getPredec());
					enq(q);
					return;
				}
				if (curnode.getFinger(q.arg1).getKey()== q.inode.getKey()) return;
				if (curnode.getFinger(q.arg1)!=null && common.inBetween(curnode.getStartIndex(q.arg1), curnode.getFinger(q.arg1).getKey(), q.inode.getKey())) {
					curnode.setFinger(q.arg1,q.inode);
					q.setCnode(curnode.getPredec());
					enq(q);
				}
				
				return;
			}
		}
	}

	void nodeComplete(node n) throws pval_error, make_key_error, valid_not_checked
	{
		query tq;
		node tn;
		long k;

		//n.setActive(1);
		for (int i=0;i<common.keyLength;i++)
		{
			k=curnode.getRegisterIndex(i);
			tn=n.findPred(k);
			if (tn==null) continue;
			tq=new query(QTYPE.REGISTER,k,curnode);
			tq.setCnode(tn);
			tq.arg1=i;
			enq(tq);
		}
		tq=new query(QTYPE.REGISTER,0,curnode);
		tq.setCnode(curnode.getFinger(0));
		tq.arg1=-1;
		enq(tq);
	}

	void stabilize()
	{

	}

	static int tol=0;
	int prev=2;
	void validate() throws validate_failed, pval_error, make_key_error
	{
		//if (tol==0) throw new validate_failed();
		int i=1;
		node s=startNode;
		boolean success=true;
		long diff;
		while((s=s.nextNode()).getId()!=startNode.getId())
		{
			i++;
			if (i>common.maxNodes) 
			{
				if (tol==0)throw new validate_failed();
				else tol--;
				success=false;
				break;
			}
			for (int j=0;j<common.keyLength;j++)
			{
				//if (tol==0) throw new validate_failed();
				diff=common.compareKeys(s.getFinger(j).getKey(),s.getKey());
				if (diff==0) diff=common.maxN;
				if (diff<common.compareKeys(s.getStartIndex(j),s.getKey())) throw new validate_failed();
			}
		}
		i++;
		int ss=node.allnodes.size();
		if (ss-i>1) throw new validate_failed();
		//		if (i<prev) throw new validate_failed();
		//		prev++;
	}
}
