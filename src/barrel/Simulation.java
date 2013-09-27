package barrel;

import chord.MakeKeyException;
import chord.pval_error;
import chord.valid_not_checked;
import chord.validate_failed;
import chord.wrong_routing;

public class Simulation {

	Node curnode;
	Message curm; 


	public static void Main(String args[]) {
		Common.sim = new Simulation();
		Common.sim.go();
	}

	void go() {

	}

	void enq(Message m) {

	}

	void processm(Message m) throws MakeKeyException, valid_not_checked, wrong_routing, validate_failed, pval_error
	{
		m.incItr();
		Common.output(m);
		this.curnode=m.getCnode();
		curm=m;
		Node tn;
		//qbuf.clear();

		if(m.skip>0) 
		{
			m.skip--;
			enq(m);
			return;
		}

		if (m.mtype==MTYPE.QUERY)
		{
			Query q=(Query)m;
			if (!q.response) {
				if (!m.reached && q.getKey()!=curnode.getKey()) {
					tn = curnode.findPred(q.getKey());
					if (tn==null) {
						q.reached=true;
						if (q.qtype!=QTYPE.NODEADD || q.qtype!=QTYPE.REGISTER) q.setCnode(curnode.nextNode());
						enq(q);
						//						return;
					} else {
						q.setCnode(tn);
						enq(q);
						//						return;
					}
				} else {
					q.reached=true;
					if (q.qtype==QTYPE.KEYFIND) {
						if (curnode.hasKey(q.getKey())!=null) {
							q.setCnode(q.sendResposeTo.pop());
							q.response=true;
						} else {
							q.sendResposeTo.push(curnode);
							q.setCnode(curnode.uplevel);
							enq(q);
						}
					} else if (q.qtype==QTYPE.KEYADD) {
						curnode.addKey(q.getKey());
						if (curnode.uplevel!=null) {
							Query nm = new Query(QTYPE.KEYADD, q.getKey(), q, curnode.uplevel, curnode.uplevel);
							enq(nm);
						}
					} else if (q.qtype==QTYPE.KEYDEL) {
						curnode.addKey(q.getKey());
						if (curnode.uplevel!=null) {
							Query nm = new Query(QTYPE.KEYDEL, q.getKey(), q, curnode.uplevel, curnode.uplevel);
							enq(nm);
						}
					} else if (q.qtype==QTYPE.NODEADD) {
						Message nm=new Message(MTYPE.FINGER,q,curnode,q.inode);
						enq(nm);
						if (!Common.inBetween(q.inode.getKey(), curnode.nextNode().getKey(), q.inode.getStartIndex(q.fingerNum))) throw new validate_failed();
					} else if (q.qtype==QTYPE.REGISTER) {
						Node toset = q.inode;
						boolean changed=false;
						for (int i=0;i<Common.keyLength;i++) {
							if (curnode.getFinger(i)!=null) {
								if (Common.inBetween(curnode.getKey(), curnode.getFinger(i).getKey(), toset.getKey()) 
										&& Common.inBetween(curnode.getStartIndex(i), curnode.getFinger(i).getKey(), toset.getKey())) {
									curnode.setFinger(i,toset);
									changed=true;
								}
							} else {
								if (Common.inBetween(curnode.getKey(), toset.getKey(), curnode.getStartIndex(i))) {
									curnode.setFinger(i,toset);
									if (curnode.ffilled<i+1) curnode.ffilled=i+1;
									changed=true;
								}
							}
						}
						if (changed) {
							q.setCnode(curnode.getPredec());
							enq(q);
						}
					}
				}
			} else {

			}
		} else if(m.mtype==MTYPE.FINGER) {
			Node toset=m.inode.nextNode();
			// if (toset==null) toset=q.inode;

			if (curnode.ffilled==0) {
				curnode.setPredec(m.inode);
				curnode.setFinger(0,toset);
				curnode.ffilled++;
				nodeComplete(curnode);
			}

			for (int i=0;i<Common.keyLength;i++) {
				if (curnode.getFinger(i)!=null) {
					if (Common.inBetween(curnode.getKey(), curnode.getFinger(i).getKey(), toset.getKey()) 
							&& Common.inBetween(curnode.getStartIndex(i), curnode.getFinger(i).getKey(), toset.getKey()))
						curnode.setFinger(i,toset);
				} else {
					if (Common.inBetween(curnode.getKey(), toset.getKey(), curnode.getStartIndex(i))) {
						curnode.setFinger(i,toset);
						if (curnode.ffilled<i+1) curnode.ffilled=i+1;
					}
				}
			}

			if (curnode.ffilled != Common.keyLength)
			{
				long next=curnode.getStartIndex(curnode.ffilled);
				Query nq = new Query(QTYPE.NODEADD,next,curm,curnode,curnode.nextNode());
				enq(nq);
			}
			if (curnode.predec==null || 
					Common.inBetween(curnode.predec.getKey(), curnode.getKey(), m.inode.getKey()))
				curnode.predec=m.inode;
		}
	}

	void nodeComplete(Node n) throws pval_error, MakeKeyException, valid_not_checked
	{
		Message nm;
		Node tn;
		long k;

		for (int i=0;i<Common.keyLength;i++)
		{
			k=curnode.getRegisterIndex(i);
			tn=n.findPred(k);
			if (tn==null) continue;
			nm=new Query(QTYPE.REGISTER, k, curm, curnode, tn);
			enq(nm);
		}
		nm=new Message(MTYPE.FINGER,curm,curnode,curnode.getFinger(0));
		enq(nm);
	}
}
