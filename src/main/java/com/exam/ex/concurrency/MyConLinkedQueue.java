package com.exam.ex.concurrency;

import java.util.concurrent.atomic.AtomicReference;

public class MyConLinkedQueue {
	public static class Node
	{
		private volatile int val;
		protected final AtomicReference<Node> next;
		
		public Node(int val,AtomicReference<Node> next) {
			super();
			this.val = val;
			this.next = next;
		}
		
		public int getVal() {
			return val;
		}
		public void setVal(int val) {
			this.val = val;
		}
	}
	
	private final Node dummy = new Node(0, new AtomicReference<Node>(null)); //dummy node {0, next=-> null}
	private final AtomicReference<Node> head = new AtomicReference<Node>(dummy);	
	private final AtomicReference<Node> tail = new AtomicReference<Node>(dummy);

	//http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/util/concurrent/ConcurrentLinkedQueue.java#ConcurrentLinkedQueue.add%28java.lang.Object%29
	public boolean add(int val) throws InterruptedException{
		Node newNode = new Node(val,new AtomicReference<Node>(null));
		Node txTail;
		Node txNext;
		
		while(true)
		{
			txTail = tail.get();
			txNext = tail.get().next.get();
			if(txTail == tail.get())
			{
				if(txNext==null)
				{
					if(tail.get().next.compareAndSet(null, newNode))
					{
						Thread.sleep(300);
						if(tail.compareAndSet(txTail, newNode))
						{
							System.out.println("Reset Tail!!!!!!!!!");
						}
						return true;
					}
				}
				else
				{
					if(tail.compareAndSet(txTail, txNext))
					{
						System.out.println("Shift Tail!!!!!!!!!");
					}
				}
			}
		}
	}
	
	protected void resetTail()
	{
		Node txTail;
		Node txNext;
		
		while(true)
		{
			txTail = tail.get();
			txNext = tail.get().next.get();
			if(txTail == tail.get())
			{
				if(txNext!=null)
				{
					if(tail.compareAndSet(txTail, txNext))
					{
						System.out.println("Shift Tail!!!!!!!!!");
					}
				}
				else
				{
					return;
				}
			}
		}
	}
	
	public int getTail()
	{
		resetTail();
		return tail.get().getVal();
	}

	public int size()
	{
		int i = 0;
		for(Node n = head.get(); n!=null; n = n.next.get())
		{
			i++;
		}
		
		return i;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		
		for(Node n = head.get(); n!=null; n = n.next.get())
		{
			s.append(n.val + " ");
		}
		return s.toString();
	}

	
}
