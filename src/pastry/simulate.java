package pastry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class simulate
{

	/**
	 * @param args
	 */

	final String inputF="Input";

	node startNode=null;

	BlockingQueue<query> queryq=new LinkedBlockingQueue<query>();
	List<query> qbuf=new ArrayList<query>();
	
	public query currentq=null,parentq=null;
	node curnode; //current node on which the query is being processed
	public static int tnodes=2;

	public static void main(String[] args) throws Exception
	{
		simulate obj = new simulate();
		obj.init();
		
		if (args.length>0 && args[0].equals("1"))
		{
			common.outputtoScreen("Making input file");
			obj.makeNodes();
		}
		common.outputtoScreen("Processing input");
		obj.processInput();
	}

	void start() throws valid_not_checked, node_not_found, validate_failed, not_implemented, pval_error, make_key_error, infinite_loop, UnsupportedEncodingException, NoSuchAlgorithmException, qtype_unrecognized 
	{
		common.outputtoScreen("Start called");
		int i=0;
		parentq=queryq.peek();
		while(!queryq.isEmpty())
		{
			i++;
			processq(queryq.poll());
			//if (i>10000) throw new infinite_loop();
		}
		i=0;
		if (parentq!=null && parentq.qtype==QTYPE.NODEADD) tnodes++;
		parentq=null;
		dumpstate();
		validate();
	}

	void makeNodes()
	{
		String out, comment,st="";
		comment="# 1. op		2. time		3. ni	4. sn(optional)	\n# arg1: op: na:node add, nd: node delete, nf:node find,	qa: querry add\n";
		comment+="# arg2: node id/ query id referred by the op \n# arg3: node id of the node on which the op is made\n";
		comment+="# arg4,5: x,y co-ordinates of of the node (used only when op=na)\n";
		Random r = new Random();

		for(int i=5;i<common.maxNodes;i++)
		{
			int x=r.nextInt(common.maxX);
			int y=r.nextInt(common.maxY);
			st+="na\tapc\t"+Integer.toString(i)+"\t"+Integer.toString(x)+"\t"+Integer.toString(y)+"\n";	//arg3 not used
		}
		st+="nf\tapc\t"+(-1)*r.nextInt(1000)+"\t"+r.nextInt(common.maxNodes)+"\n";
		out=st;

		try{
			File f = new File(inputF);
			if (!f.exists())
				f.createNewFile();

			BufferedWriter bw=new BufferedWriter(new FileWriter(f.getAbsoluteFile()));
			bw.write(out);
			bw.close();
		}
		catch(Exception e){e.printStackTrace();}
	}

	void processInput() throws NumberFormatException, UnsupportedEncodingException, NoSuchAlgorithmException, IOException, make_key_error, valid_not_checked, node_not_found, validate_failed, not_implemented, pval_error, infinite_loop, Exception
	{
		
			BufferedReader in = new BufferedReader(new FileReader(inputF));
			String text;
			int arg1,arg2,arg3;
			String[] word;
			node n;
			query q=null;
			common.outputtoScreen("Process Input starting loop");
			while ((text=in.readLine())!=null)
			{
				if (text.subSequence(0,1).equals("#"))
					continue;
				common.outputtoScreen("Processing: "+text);
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
					common.outputtoScreen("--------------"+c+"");
					
//					arg1=Integer.parseInt(word[2]); //id
//					arg2=Integer.parseInt(word[3]); //snid
//					getNodebyId(100);
//					n=getNodebyId(arg2); //find the node from snid
//					
//					q=new query(QTYPE.NODEFIND,common.makeKey(common.md5(word[2]), common.b), n);
				}
				else if(word[0].equals("nd"))
				{
					q=new query(QTYPE.NODEDEL,null,startNode);
					arg1=Integer.parseInt(word[2]); //id
					q.arg1=arg1;
				}
				else 
				{
					common.outputtoScreen(word[0]);
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
					common.outputtoScreen(word[1]);
					throw new invalid_input();
				}
			}
			start();
			
			in.close();
	}
	
	
	
	void init() throws UnsupportedEncodingException, NoSuchAlgorithmException, make_key_error, pval_error
	{
		node n=new node(2,0,0);
		node m=new node(3,99,99);
		n.insertintoLF(m);
		n.insertintoLB(m);
		n.insertintoM(m);
		
		m.insertintoLF(n);
		m.insertintoLB(n);
		m.insertintoM(n);
		
		startNode=n;
		
		int l = shl(n.getKey(),m.getKey());
		n.setRtable(l,m.getKey()[l],m);
		m.setRtable(l,n.getKey()[l],n);
	}

	node getNodebyId(int id) throws node_not_found
	{
		node n;
		if (startNode.getId()==id) return startNode;
		n=startNode.nextNode();
		while(n.getId()!=id && n!=startNode)
			n=n.nextNode();
		if (n==startNode) throw new node_not_found();
		return n;
	}
	/**
	 * Adds node to the network
	 * @param n
	 * @throws Exception 
	 */
	query addNode(node n) throws Exception
	{
		

		//common.gLock.lock();
		node nn=nearestNode(n);
		//common.gLock.unlock();

		query q= new query(QTYPE.NODEADD,n.getKey(),nn);
		q.extran=n;

		n.setMtable(0,nn);
		for(int i=0;i<nn.mfilled && i<common.sizeM-1;i++)
			n.setMtable(i+1,nn.getMtable(i));
		n.mfilled=nn.mfilled+1;
		if (n.mfilled>common.sizeM) n.mfilled--;
		return q;
	}

	void processq(query q) throws valid_not_checked, node_not_found, not_implemented, pval_error, make_key_error, UnsupportedEncodingException, NoSuchAlgorithmException, qtype_unrecognized 
	{
		q.incIter();
		common.output(q);
		q.step=0;
		this.curnode=q.getCnode();
		currentq=q;
		qbuf.clear();
		
		if (q.skip>0) 
		{
			q.skip--;
			enq(q);
			return;
		}
		if (q.qtype.equals(QTYPE.NODEFIND))
		{
			findNode(q);
			enq(q);
		}
		else if (q.qtype.equals(QTYPE.NODEDEL))
		{
			node n=getNodebyId(q.arg1);
			n.setValid(0);
		}
		else if (q.qtype.equals(QTYPE.NODEADD))
		{
			if (q.found==1)
			{
				query tq;
				node tn=q.extran,tnode,cnode=q.getCnode();
				tn.insertintoLB(cnode);
				tn.insertintoLF(cnode);
				tn.insertintoM(cnode);
				
				for (int i=0;i<cnode.lfilledF;i++)
				{
					tn.insertintoLF(cnode.getLtableF(i));
					tn.insertintoLB(cnode.getLtableF(i));
				}
				
				for (int i=0;i<cnode.lfilledB;i++)
				{
					tn.insertintoLF(cnode.getLtableB(i));
					tn.insertintoLB(cnode.getLtableB(i));
				}
				
				//send to all the nodes my state, and become online, yo
				for (int i=0;i<tn.mfilled;i++)
				{
					tq=new query(QTYPE.REGISTERNODE_M,null,tn);
					tnode=tn.getMtable(i);
					tq.setCnode(tnode);
					enq(tq);
					tn.insertintoLF(tnode);
					tn.insertintoLB(tnode);
				}
				for (int i=0;i<tn.lfilledF;i++)
				{
					tq=new query(QTYPE.REGISTERNODE_LF,null,tn);
					tnode=tn.getLtableF(i);
					tq.setCnode(tnode);
					enq(tq);
					tn.insertintoLF(tnode);
					tn.insertintoLB(tnode);
				}
				for (int i=0;i<tn.lfilledB;i++)
				{
					tq=new query(QTYPE.REGISTERNODE_LB,null,tn);
					tnode=tn.getLtableB(i);
					tq.setCnode(tnode);
					enq(tq);
					tn.insertintoLF(tnode);
					tn.insertintoLB(tnode);
				}
				for (int i=0;i<common.maxRow;i++)
				{
					for (int j=0;j<common.maxCol;j++)
					{
						tnode=tn.getRtable(i, j);
						if (tnode==null) continue;
						if (tnode.isValid()==0) rtabled(tn, i, j);
						tq=new query(QTYPE.REGISTERNODE_R,null,tn);
						tq.setCnode(tnode);
						enq(tq);
						tn.insertintoLF(tnode);
						tn.insertintoLB(tnode);
					}
				}
				return;
			}

			query d=new query(QTYPE.RTABLE,null,q.getCnode());
			d.setCnode(q.extran);
			enq(d);
			q.cmsgs++;
			
			findNode(q);
			enq(q);
		}

		//dummy query to simulate the sending of routing table entries
		else if (q.qtype.equals(QTYPE.RTABLE))
		{
			if (!q.getCnode().updateContactN(q.inode,2))
				q.getCnode().contactN.add(new cell(q.inode));
			
			int l=shl(q.getCnode().getKey(),q.inode.getKey()); //l matches => you can use upto [l] row of this node to fill your rtable
			if (q.getCnode().getRtable(l,q.inode.getKey()[l])==null) q.getCnode().setRtable(l,q.inode.getKey()[l],q.inode);
			for (int i=0;i<=l;i++)
				for(int j=0;j<common.maxCol;j++)
					if (q.getCnode().getRtable(i,j)==null && q.getCnode().getId()!=q.inode.getId())
						q.getCnode().setRtable(i,j,q.inode.getRtable(i,j));

			node tn,cnode=q.getCnode();
			query lastq=null,qn;
			if (q.arg1==1 && q.getCnode().getRtable(q.arg2,q.arg3)==null && q.arg4==1)
			{
				int found1=0,a=q.arg2,i=q.arg2,j=q.arg3;
				while (found1==0)
				{
					for(int b=0;b<common.maxCol;b++)
					{
						tn=q.getCnode().getRtable(a, b);
						if (tn==null) continue;
						if (tn.isValid()==0)
						{
							cnode.removeRtable(a,b);
							continue;
						}
						qn=new query(QTYPE.GIMMERTABLE,null,cnode);
						qn.setCnode(tn);
						qn.arg1=1;
						qn.arg2=i;
						qn.arg3=j;
						enq(qn);
						lastq=qn;
						found1=1;
					}
					a++;
					if (a==common.maxRow) break;
				}
				if (found1==1) lastq.arg4=1;
			}
			q.finish();
			return;
		}
		else if(q.qtype==QTYPE.MTABLE)
		{
			if (!q.getCnode().updateContactN(q.inode,1))
				q.getCnode().contactN.add(new cell(q.inode));
			
			node cnode = q.getCnode(),tn;
			cnode.insertintoM(q.inode);
			for (int i=0;i<q.inode.mfilled;i++)
			{
				tn=q.inode.getMtable(i);
				cnode.insertintoM(tn);
			}
		}
		else if(q.qtype==QTYPE.MRTABLE)
		{
			//M
			if (!q.getCnode().updateContactN(q.inode,1))
				q.getCnode().contactN.add(new cell(q.inode));
			
			node cnode = q.getCnode(),tn;
			cnode.insertintoM(q.inode);
			for (int i=0;i<q.inode.mfilled;i++)
			{
				tn=q.inode.getMtable(i);
				cnode.insertintoM(tn);
			}
			
			//R
			if (!q.getCnode().updateContactN(q.inode,2))
				q.getCnode().contactN.add(new cell(q.inode));
			
			int l=shl(q.getCnode().getKey(),q.inode.getKey()); //l matches => you can use upto [l] row of this node to fill your rtable
			if (q.getCnode().getRtable(l,q.inode.getKey()[l])==null) q.getCnode().setRtable(l,q.inode.getKey()[l],q.inode);
			for (int i=0;i<=l;i++)
				for(int j=0;j<common.maxCol;j++)
					if (q.getCnode().getRtable(i,j)==null && q.getCnode().getId()!=q.inode.getId())
						q.getCnode().setRtable(i,j,q.inode.getRtable(i,j));
		}
		else if(q.qtype==QTYPE.LTABLE)
		{
			if (!q.getCnode().updateContactN(q.inode,0))
				q.getCnode().contactN.add(new cell(q.inode));
			
			node cnode=q.getCnode();
//			boolean found=false;
//			for (int i=0;i<cnode.lfilledF;i++)
//				if (cnode.getLtableF(i).equals(cnode))
//				{
//					found=true;
//					break;
//				}
//			if (!found) return;
			
			for (int i=0;i<q.inode.lfilledF;i++)
			{
				cnode.insertintoLF(q.inode.getLtableF(i));
				cnode.insertintoLB(q.inode.getLtableF(i));
			}
			for (int i=0;i<q.inode.lfilledB;i++)
			{
				cnode.insertintoLF(q.inode.getLtableB(i));
				cnode.insertintoLB(q.inode.getLtableB(i));
			}
		}
//		else if(q.qtype==QTYPE.LTABLEB)
//		{
//			if (!q.getCnode().updateContactN(q.inode,0))
//				q.getCnode().contactN.add(new cell(q.inode));
//			node cnode=q.getCnode();
////			boolean found=false;
////			for (int i=0;i<cnode.lfilledB;i++)
////				if (cnode.getLtableB(i).equals(cnode))
////				{
////					found=true;
////					break;
////				}
////			if (!found) return;
//			
//			for (int i=0;i<q.inode.lfilledF;i++)
//			{
//				cnode.insertintoLF(q.inode.getLtableF(i));
//				cnode.insertintoLB(q.inode.getLtableF(i));
//			}
//			for (int i=0;i<q.inode.lfilledB;i++)
//			{
//				cnode.insertintoLF(q.inode.getLtableB(i));
//				cnode.insertintoLB(q.inode.getLtableB(i));
//			}
//		}
		else if(q.qtype==QTYPE.GIMMEMTABLE)
		{
			if (q.mtime==curnode.mtime) return;
			if (q.mtime==q.getCnode().mtime) return;
			query qn = new query(QTYPE.MTABLE,null,q.getCnode());
			qn.setCnode(q.inode);
			enq(qn);
		}
		else if(q.qtype==QTYPE.GIMMERTABLE)
		{
			if (q.rtime==curnode.rtime) return;
			if (q.rtime==q.getCnode().rtime) return;
			query qn = new query(QTYPE.RTABLE,null,q.getCnode());
			qn.setCnode(q.inode);
			qn.arg1=q.arg1;
			qn.arg2=q.arg2;
			qn.arg3=q.arg3;
			qn.arg4=q.arg4;
			enq(qn);
		}
		else if(q.qtype==QTYPE.GIMMEMRTABLE)
		{
			if (q.mtime==curnode.mtime) return;
			if (q.mtime==q.getCnode().mtime) return;
			query qn = new query(QTYPE.MRTABLE,null,q.getCnode());
			qn.setCnode(q.inode);
			enq(qn);
		}
//		else if(q.qtype.equals(QTYPE.GIMMELTABLEF))
//		{
//			if (q.ltime==curnode.ltime) return;
//			if (q.ltime==q.getCnode().ltime) return;
//			query qn = new query(QTYPE.LTABLEF,null,q.getCnode());
//			qn.setCnode(q.inode);
//			enq(qn);
//		}
		else if(q.qtype.equals(QTYPE.GIMMELTABLE))
		{
			if (q.ltime==curnode.ltime) return;
			if (q.ltime==q.getCnode().ltime) return;
			query qn = new query(QTYPE.LTABLE,null,q.getCnode());
			qn.setCnode(q.inode);
			enq(qn);
		}
		else if(q.qtype.equals(QTYPE.REGISTERNODE_M))
		{
			node cnode = q.getCnode(),tn;
			cnode.insertintoLF(q.inode);
			cnode.insertintoLB(q.inode);
			
			cnode.insertintoM(q.inode);
			for (int i=0;i<q.inode.mfilled;i++)
			{
				tn=q.inode.getMtable(i);
				cnode.insertintoM(tn);
			}
			
			query qn = new query(QTYPE.LTABLE,null,cnode);
			qn.setCnode(q.inode);
			enq(qn);
			
			//			if (cnode.mfilled<common.sizeM)
			//			{
			//				cnode.setMtable(cnode.mfilled,q.inode);
			//				cnode.mfilled++;
			//			}
			//			else
			//			{
			//				node tn;
			//				for (int i=0;i<cnode.mfilled;i++)
			//				{
			//					tn=cnode.getMtable(i);
			//					if (tn.isValid()==0)
			//						mtabled(cnode,i);
			//					if (i==cnode.mfilled) break;
			//					
			//					
			//					if (common.edistance(tn,cnode)>common.edistance(cnode,q.inode))
			//					{
			//						int start=cnode.mfilled-1;
			//						if (cnode.mfilled==common.sizeM) start=cnode.mfilled-2;
			//						for (int j=start;j<=i;j--)
			//							
			//						cnode.setMtable(i,q.inode);
			//					}
			//				}
			//			}
			q.finish();
			return;
		}

		else if(q.qtype.equals(QTYPE.REGISTERNODE_LF))
		{
			node cnode=q.getCnode();
			cnode.insertintoLF(q.inode);
			cnode.insertintoLB(q.inode);
			query qn = new query(QTYPE.LTABLE,null,cnode);
			qn.setCnode(q.inode);
			enq(qn);
			
			//			if (cnode.lfilledF<common.sizeL)
			//			{
			//				cnode.setLtableF(cnode.lfilledF,q.inode);
			//				cnode.lfilledF++;
			//			}
			//			else
			//			{
			//				node tn,in=null;
			//				boolean done=false;
			//				int max=0,t,tin=0;
			//				
			//				
			//				for (int i=0;i<cnode.lfilledF;i++)
			//				{
			//					tn=cnode.getLtableF(i);
			//					if (tn.isValid()==0)
			//					{
			//						cnode.setLtableF(i,q.inode);
			//						done=true;
			//						break;
			//					}
			//					t=common.compareKeys(cnode.getKey(),tn.getKey());
			//					if (t>max)
			//					{
			//						max=t;
			//						in=tn;
			//						tin=i;
			//					}
			//				}
			//				if (!done && Math.abs(common.compareKeys(cnode.getKey(),in.getKey()))>Math.abs(common.compareKeys(cnode.getKey(),q.inode.getKey())))
			//				{
			//					cnode.setLtableF(tin,q.inode);
			//				}
			//			}
			q.finish();
			return;
		}

		else if(q.qtype.equals(QTYPE.REGISTERNODE_LB))
		{
			node cnode=q.getCnode();
			cnode.insertintoLB(q.inode);
			cnode.insertintoLF(q.inode);
			query qn = new query(QTYPE.LTABLE,null,cnode);
			qn.setCnode(q.inode);
			enq(qn);
			//			if (cnode.lfilledB<common.sizeL)
			//			{
			//				cnode.setLtableB(cnode.lfilledB,q.inode);
			//				cnode.lfilledB++;
			//			}
			//			else
			//			{
			//				node tn,in=null;
			//				boolean done=false;
			//				int max=0,t,tin=0;
			//				
			//				
			//				for (int i=0;i<cnode.lfilledB;i++)
			//				{
			//					tn=cnode.getLtableB(i);
			//					if (tn.isValid()==0)
			//					{
			//						cnode.setLtableB(i,q.inode);
			//						done=true;
			//						break;
			//					}
			//					t=common.compareKeys(cnode.getKey(),tn.getKey());
			//					if (t>max)
			//					{
			//						max=t;
			//						in=tn;
			//						tin=i;
			//					}
			//				}
			//				if (!done && Math.abs(common.compareKeys(cnode.getKey(),in.getKey()))>Math.abs(common.compareKeys(cnode.getKey(),q.inode.getKey())))
			//				{
			//					cnode.setLtableB(tin,q.inode);
			//				}
			//			}
			q.finish();
			return;
		}

		else if(q.qtype.equals(QTYPE.REGISTERNODE_R))
		{
			
			node cnode=q.getCnode();
			
			cnode.insertintoLF(q.inode);
			cnode.insertintoLB(q.inode);
			
			int l = shl(q.getCnode().getKey(),q.inode.getKey());
			for (int i=0;i<l;i++)
				for (int j=0;j<common.maxCol;j++)
					if (cnode.getRtable(i,j)==null && q.getCnode().getId()!=q.inode.getId())
						cnode.setRtable(i,j,q.inode.getRtable(i,j));
			if (cnode.getRtable(l,q.inode.getKey()[l])==null)
				cnode.setRtable(l,q.inode.getKey()[l],q.inode);
			query qn = new query(QTYPE.LTABLE,null,cnode);
			qn.setCnode(q.inode);
			enq(qn);
			
			q.finish();
			return;
		}
		else throw new qtype_unrecognized();
		
	}

	/**find the node that should have the value for query q (just after 1 iteration)
	 * @throws valid_not_checked 
	 * @throws not_implemented 
	 * @throws pval_error 
	 * @throws make_key_error 
	 * @throws Exception 
	 */

	void findNode(query q) throws valid_not_checked, not_implemented, pval_error, make_key_error
	{
		node cnode=q.getCnode();
		long dist=common.compareKeys(q.getKey(),cnode.getKey());
		if (q.found==1)
		{
			nodeFound(q);
			return;
		}

		/** steps: 	1. compare and see if the key is among the leave set
		 * 			2. search the routing table for entry and send to it
		 * 			3. if not found there, search all the nodes that you have and send to the one with lesser digit difference than yours
		 */

		//Step 1
		q.step=1;
		long dist1,dist2,dist3,dist4,dist5,dist6;
		node tnode;

		dist1=common.compareKeys(cnode.getLtableF(cnode.lfilledF-1).getKey(),cnode.getKey());
		dist2=common.compareKeys(cnode.getKey(),cnode.getLtableB(cnode.lfilledB-1).getKey());
		
		dist3=common.compareKeys(cnode.getLtableF(0).getKey(),cnode.getKey());
		dist4=common.compareKeys(cnode.getKey(),cnode.getLtableB(0).getKey());
		
		if (dist1<0) dist1+=(long)Math.pow(2, common.bkeyLength);
		if (dist3<0) dist3+=(long)Math.pow(2, common.bkeyLength);
		if (dist<0) dist+=(long)Math.pow(2, common.bkeyLength);
		if (dist<dist3)
		{
			common.outputtoScreen("----------------enter1");
			q.found=1;
			dist5=common.compareKeys(cnode.getLtableF(0).getKey(),q.getKey());
			if (dist5<0) dist5+=(long)Math.pow(2, common.bkeyLength);
			if (dist>dist5)
			{
				q.setCnode(cnode.getLtableF(0));
			}
			return;
		}
		else if (dist<dist1)
		{
			tuple<node,Long> t = findInLF(q.getKey(),cnode);
			if (t.x==null) throw new NullPointerException();
			q.found=1;
			q.setCnode(t.x);
			return;
		}
		
		if (dist2<0) dist2+=(long)Math.pow(2, common.bkeyLength);
		if (dist4<0) dist4+=(long)Math.pow(2, common.bkeyLength);
		dist=common.compareKeys(cnode.getKey(),q.getKey());
		if (dist<0) dist+=(long)Math.pow(2, common.bkeyLength);
		if (dist==dist4)
		common.outputtoScreen("----------------dist="+dist+" dist4="+dist4);
		if (dist<dist4)
		{
			common.outputtoScreen("----------------enter2");
			q.found=1;
			dist5=common.compareKeys(q.getKey(),cnode.getLtableB(0).getKey());
			if (dist5<0) dist5+=(long)Math.pow(2, common.bkeyLength);
			if (dist>dist5)
			{
				q.setCnode(cnode.getLtableB(0));
			}
			return;
		}
		else if (dist<dist2)
		{
			tuple<node,Long> t = findInLB(q.getKey(),cnode);
			if (t.x==null) throw new NullPointerException();
			q.found=1;
			q.setCnode(t.x);
			return;
		}

		//Step 2
		q.step=2;
		int l=shl(q.getKey(),cnode.getKey());
		common.outputtoScreen(q.getKey()[l]+"");
		tnode=cnode.getRtable(l,q.getKey()[l]);
		if (tnode!=null)
		{
			q.setCnode(tnode);
			return;
		}

		//Step 3
		
		else
		{
			q.step=3;
			cnode=q.getCnode();
			query qn1,qn2;
			for (int i=0;i<cnode.mfilled;i++)
			{
				qn1=new query(QTYPE.GIMMEMRTABLE,null,cnode);
				qn1.setCnode(cnode.getMtable(i));
//				qn1.mtime=
				enq(qn1);
				
//				qn2=new query(QTYPE.GIMMERTABLE,null,cnode);
//				qn2.setCnode(cnode.getMtable(i));
//				enq(qn2);
			}
			
			@SuppressWarnings("unchecked")
			tuple<node,Long>[] vals = new tuple[4];
			dist=common.compareKeys(q.getKey(),q.getCnode().getKey());
			if (dist>common.maxN) throw new make_key_error();
			dist=((long)Math.abs(dist));
			if (dist>common.maxN/2) dist=common.maxN-dist;
			long min=-1,mincnode=dist;
			//Neighborhood set
			vals[0]=findInM(q.getKey(),cnode);

			//Routing table
			
			int t;
			//q.found=1;
//			min=common.compareKeys(cnode.getRtable(l,0).getKey(),q.getKey());
//			node minnode=cnode.getRtable(l, 0);
//			min=Math.abs(min);
			node minnode=null;
//			min=min%(long)Math.pow(2,common.keyLength-1);
			for (int i=0;i<common.maxCol;i++)
			{
				if (cnode.getRtable(l,i)==null) continue;
				dist=common.compareKeys(cnode.getRtable(l,i).getKey(),q.getKey());
				dist=(long)Math.abs(dist);
				if (dist>common.maxN/2) dist=common.maxN-dist;
				if (minnode==null || dist<min)
				{
					min=dist;
					minnode=cnode.getRtable(l,i);
				}
			}
//			node minnode=null;
//
//			for (int i=0;i<common.maxCol;i++)
//			{
//				tnode=cnode.getRtable(l,i);
//				if (tnode!=null)
//				{
//					dist1=common.compareKeys(q.getKey(), tnode.getKey());
//					if (dist1<min)
//					{
//						min=dist1;
//						minnode=tnode;
//					}
//				}
//			}
			vals[1]=new tuple<node,Long>(minnode,min);

			//Leaf set
			minnode=null;
			for (int i=0;i<cnode.lfilledF;i++)
			{
				dist=common.compareKeys(cnode.getLtableF(i).getKey(),q.getKey());
				dist=(long)Math.abs(dist);
				if (dist>common.maxN/2) dist=common.maxN-dist;
				if (minnode==null || dist<min)
				{
					min=dist;
					minnode=cnode.getLtableF(i);
				}
			}
			vals[2]=new tuple<node,Long>(minnode,min);
			minnode=null;
			for (int i=0;i<cnode.lfilledB;i++)
			{
				dist=common.compareKeys(cnode.getLtableB(i).getKey(),q.getKey());
				dist=(long)Math.abs(dist);
				if (dist>common.maxN/2) dist=common.maxN-dist;
				if (minnode==null || dist<min)
				{
					min=dist;
					minnode=cnode.getLtableB(i);
				}
			}
			vals[3]=new tuple<node,Long>(minnode,min);

			//find the minimum

			//min=(long)Math.abs(vals[0].y);
			t=-1;
			min=mincnode;
			for (int i=0;i<4;i++)
			{
				if (vals[i].x==null) continue;
				if (min>(long)Math.abs(vals[i].y))
				{
					min=(long)Math.abs(vals[i].y);
					t=i;
				}
			}

			if (t==-1) 
			{
				// ODO
				common.outputtoScreen("----------------step3 min="+min);
				throw new not_implemented();
//				System.out.println("Error in finding min dist");
//				throw new Exception();
			}
			else 
			{
				common.outputtoScreen("----------------step3 t="+t+"goig to nodeid="+vals[t].x.getId());
				q.setCnode(vals[t].x);
			}
		}



	}

	tuple<node,Long> findInM(int[] k, node cnode) throws valid_not_checked, pval_error, make_key_error
	{
		if (cnode.mfilled==0)
			return new tuple<node,Long>(null,(long)0);
		long dist,min;
		int in;
		node minnode,tnode;

		minnode=cnode.getMtable(0);
		min=common.compareKeys(k,minnode.getKey());
		min=(long)Math.abs(min);
		if (min>common.maxN/2) min=common.maxN-min;
		in=0;

		for(int i=1;i<cnode.mfilled;i++)
		{
			tnode=cnode.getMtable(i);
			dist=common.compareKeys(k,tnode.getKey());
			dist=(long)Math.abs(dist);
			if (dist>common.maxN/2) dist=common.maxN-dist;
			if (dist<min)
			{
				min=dist;
				minnode=tnode;
				in=i;
			}
		}

		if (minnode.isValid()==0)
		{
			mtabled(cnode, in);
			return findInM(k, cnode);
		}
		return new tuple<node,Long>(minnode,min);
	}
	
	//assuming k1 is forward of k2
//	int inbetweenF(int[] search, int[] k1, int[] k2)
//	{
//		
//	}

	tuple<node,Long> findInLF(int[] k, node cnode) throws valid_not_checked, pval_error, make_key_error
	{
		if (cnode.lfilledF==0)
			return new tuple<node,Long>(null,(long)0);
		long dist,min,dist1,dist2,dist3;
		int in;
		node minnode,tnode;

		minnode=null;
		min=0;
		if (min<0) min+=(long)Math.pow(2,common.bkeyLength);
		in=0;
		for(int i=0;i<cnode.lfilledF-1;i++)
		{
			dist1=common.compareKeys(k,cnode.getLtableF(i).getKey());
			if (dist1<0)
				dist1+=(long)Math.pow(2,common.bkeyLength);
			
			dist2=common.compareKeys(cnode.getLtableF(i+1).getKey(),cnode.getLtableF(i).getKey());
			if (dist2<0)
				dist2+=(long)Math.pow(2,common.bkeyLength);
			
			if (dist1<dist2)
			{
				dist3=common.compareKeys(cnode.getLtableF(i+1).getKey(),k);
				if (dist3<0)
					dist3+=(long)Math.pow(2,common.bkeyLength);
				if (dist1<dist3)
				{
					min=dist1;
					minnode=cnode.getLtableF(i);
				}
				else
				{
					min=dist3;
					minnode=cnode.getLtableF(i+1);
				}
			}
			
//			tnode=cnode.getLtableF(i);
//			dist=common.compareKeys(k,tnode.getKey());
//			if (dist<min)
//			{
//				min=dist;
//				minnode=tnode;
//				in=i;
//			}
		}
		if (minnode!=null && minnode.isValid()==0)
		{
			ltableFd(cnode,in);
			return findInLF(k, cnode);
		}
		return new tuple<node,Long>(minnode,min);
	}

	tuple<node,Long> findInLB(int[] k, node cnode) throws valid_not_checked, pval_error, make_key_error
	{
		if (cnode.lfilledB==0)
			return new tuple<node,Long>(null,(long)0);
		long dist,min,dist1,dist2,dist3;
		int in;
		node minnode,tnode;

		minnode=null;
		min=0;
		if (min<0) min+=(long)Math.pow(2,common.bkeyLength);
		in=0;
		for(int i=0;i<cnode.lfilledB-1;i++)
		{
			dist1=common.compareKeys(k,cnode.getLtableB(i).getKey());
			if (dist1>0)
				dist1-=(long)Math.pow(2,common.bkeyLength);
			
			dist2=common.compareKeys(cnode.getLtableB(i+1).getKey(),cnode.getLtableB(i).getKey());
			if (dist2>0)
				dist2-=(long)Math.pow(2,common.bkeyLength);
			
			if (dist1>dist2)
			{
				dist3=common.compareKeys(cnode.getLtableB(i+1).getKey(),k);
				if (dist3>0)
					dist3-=(long)Math.pow(2,common.bkeyLength);
				if (dist1>dist3)
				{
					min=dist1;
					minnode=cnode.getLtableB(i);
				}
				else
				{
					min=dist3;
					minnode=cnode.getLtableB(i+1);
				}
			}
		}
		return new tuple<node,Long>(minnode,min);
	}

	void rtabled(node cnode, int i, int j) throws valid_not_checked
	{
		cnode.removeRtable(i, j);
		query q,lastq=null;
		node tn;
		int found1=0,a=i;
		while (found1==0)
		{
			for(int b=0;b<common.maxCol;b++)
			{
				tn=cnode.getRtable(a, b);
				if (tn==null) continue;
				if (tn.isValid()==0)
				{
					cnode.removeRtable(a,b);
					continue;
				}
				q=new query(QTYPE.GIMMERTABLE,null,cnode);
				q.setCnode(tn);
				q.arg1=1;
				q.arg2=i;
				q.arg3=j;
				enq(q);
				lastq=q;
				found1=1;
			}
			a++;
			if (a==common.maxRow) break;
		}
		if (found1==1) lastq.arg4=1;
	}

	void mtabled(node cnode, int in) throws valid_not_checked
	{
		cnode.removeMtable(in);
		node tn;
		query q;

		for(int i=0;i<cnode.mfilled;i++)
		{
			tn=cnode.getMtable(i);
			if (tn.isValid()==0)
			{
				cnode.removeMtable(i);
				i--;
				continue;
			}
			q=new query(QTYPE.GIMMEMTABLE,null,cnode);
			q.setCnode(tn);
			enq(q);
		}
	}

	void ltableFd(node cnode, int in) throws valid_not_checked
	{
		cnode.removeLtableF(in);
		node tn;
		query q = new query(QTYPE.GIMMELTABLE,null,cnode);
		tn=cnode.getLtableF(cnode.lfilledF-1);
		while(tn.isValid()==0)
		{
			cnode.removeLtableF(cnode.lfilledF-1);
			tn=cnode.getLtableF(cnode.lfilledF-1);
		}
		q.setCnode(tn);
		enq(q);
	}

	void ltableBd(node cnode, int in) throws valid_not_checked
	{
		cnode.removeLtableB(in);
		node tn;
		query q = new query(QTYPE.GIMMELTABLE,null,cnode);
		tn=cnode.getLtableF(cnode.lfilledB-1);
		while(tn.isValid()==0)
		{
			cnode.removeLtableF(cnode.lfilledB-1);
			tn=cnode.getLtableF(cnode.lfilledB-1);
		}
		q.setCnode(tn);
		enq(q);
	}
	
	void enq(query q)
	{
		if (q.getCnode()==null) 
		{
			common.outputtoScreen("Enq query is null");
			Thread.dumpStack();
			System.exit(0);
		}
		
		if (parentq==null) q.pqid=q.qid;
		else q.pqid=parentq.qid;
		query tq;
		for (int i=0;i<qbuf.size();i++)
		{
			tq=qbuf.get(i);
			if (tq.getCnode().getId()==q.getCnode().getId() && tq.inode.getId()==q.inode.getId())
			{
				if (tq.qtype==q.qtype || (tq.qtype==QTYPE.MRTABLE && (q.qtype==QTYPE.MTABLE || q.qtype==QTYPE.RTABLE)))
					return;
			}
		}
//		q.pq=currentq;
		node cn;
		cell resc;
		if (curnode!=null)
		{
			cn=q.getCnode();
			q.sender=curnode;
			resc=curnode.searchContactN(cn);
			if (resc!=null)
			{
				q.ltime=resc.ltime;
				q.rtime=resc.rtime;
				q.mtime=resc.mtime;
			}
		}
		queryq.add(q);
		qbuf.add(q);
	}

	static int shl(int[] k1, int[] k2)
	{
		int i=0;
		while(k1[i]==k2[i]) i++;
		return i;
	}

	void nodeFound(query q)
	{
		if (q.qtype.equals(QTYPE.NODEFIND))
			q.finish();
		else if(q.qtype.equals(QTYPE.NODEADD))
		{

		}
	}

	/**
	 * Finds the geographically closest node to the given node
	 * @param n
	 * @return
	 */
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

	//eucledian distance
	int maxtol=0;
	int tol=maxtol;
	
	void validate() throws validate_failed
	{
		int i=0;
		node s=startNode;
		boolean success=true;
		while((s=s.nextNode()).getId()!=startNode.getId())
		{
			if (s.lfilledF>2 && s.getLtableF(1).getId()!=s.nextNode().nextNode().getId()) 
			{
				common.outputtoScreen("@@@@@@@@see node id="+s.getId());
				dumpstate();
				throw new validate_failed();
			}
			i++;
			if (i>common.maxNodes) 
			{
				if (tol==0)throw new validate_failed();
				else tol--;
				success=false;
				break;
			}
		}
		if (success) tol=maxtol;
		checklf();
		//verifyDist();
	}
	
	void dumpstate()
	{
		node s=startNode;
		int i;
		Iterator<node> it=node.allnodes.iterator();
		while(it.hasNext())
		{
			s=it.next();
			common.outputtoScreen("NODEid="+s.getId());
			common.outputtoScreen("\tLF");
			for (int j=0;j<s.lfilledF;j++)
				common.outputtoScreen("\t\t["+j+"]="+s.getLtableF(j).getId());
			common.outputtoScreen("\tLB");
			for (int j=0;j<s.lfilledB;j++)
				common.outputtoScreen("\t\t["+j+"]="+s.getLtableB(j).getId());
		}
	}
	
	void checklf() throws validate_failed
	{
		Iterator<node> it=node.allnodes.iterator();
		node n,tn;
		if (node.allnodes.size()==2) return;
		while(it.hasNext())
		{
			n=it.next();
			tn=n.getLtableF(0);
			for(int i=1;i<n.lfilledF;i++)
			{
				if (n.getLtableF(i)!=tn.getLtableF(i-1)) throw new validate_failed();
			}
		}
	}
	
	void checklb() throws validate_failed
	{
		Iterator<node> it=node.allnodes.iterator();
		node n,tn;
		if (node.allnodes.size()==2) return;
		while(it.hasNext())
		{
			n=it.next();
			tn=n.getLtableB(0);
			for(int i=1;i<n.lfilledB;i++)
			{
				if (n.getLtableB(i)!=tn.getLtableB(i-1)) throw new validate_failed();
			}
		}
	}
	
	void verifyDist() throws pval_error, make_key_error
	{
		Iterator<node> it=node.allnodes.iterator();
		long[] dist=new long[10];
		long d1,d2;
		node n=it.next(),tn;
		while(it.hasNext())
		{
			tn=it.next();
			d1=common.compareKeys(n.getKey(),tn.getKey());
			d2=common.compareKeys(tn.getKey(),n.getKey());
			if (d1<0) d1+=(long)Math.pow(2,common.bkeyLength);
			if (d2<0) d2+=(long)Math.pow(2,common.bkeyLength);
			common.outputtoScreen("!!!!!~~~~~~   d1+d2="+(d1+d2));
		}
	}
}
