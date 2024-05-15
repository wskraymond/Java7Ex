package com.exam.ex.concurrency;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Synchronizer {
	static class SyncQueue
	{
		private Integer item = null;	//shared item for inter-Thread com.
		
		//Synchronization for the hand-off
		//Producer waits for Consumer to complete the hand-off ('Received' ACK) 
		//0: not yet
		//1: complete ( 'Received' ACK )
		private Semaphore sync = new Semaphore(0, true);	
		
		//Consumer waits for Producer to send out item 
		//0: not yet
		//1: already sent
		private Semaphore recv = new Semaphore(0, true);
		
		//Only allow one pair(P&C) to producer and consume in the head & tail of the queue
		//1: Allow one
		//0: block others
		//If No this semaphore, then new producer may put another element into item
		//then Consumer will take the next one But not the previous one
		//the orignal element will get lost
		private Semaphore send = new Semaphore(1, true);
		
		public void put(Integer i) throws InterruptedException
		{	
			System.out.println("Before " + Thread.currentThread().getName() + " - send: " + send.availablePermits());
								//@@@Element in Producer@@@
								//Start hand-off element
			send.acquire();		//Producer waits for inserting element 'i' into the queue
			System.out.println(Thread.currentThread().getName() + " - send: " + send.availablePermits());
			item = i;			//Sending: set element 'i' to shared item
			recv.release();		//Notifying: consumer to consume it
			sync.acquire();		//wait for ACK from Consumer to complete put()
								//End hand-off element
		}
		
		public Integer take() throws InterruptedException
		{
			System.out.println("Before " + Thread.currentThread().getName() +" - recv: " + recv.availablePermits());
								//Start hand-off element
			recv.acquire();		//Consumer waits for removing element '1' from the queue
			System.out.println(Thread.currentThread().getName() +" - recv: " + recv.availablePermits());
			Integer i = item;	//Receiving: assign shared item to local variable
			item = null;		//Removing: optional for GC
			sync.release();		//ACK to Producer , $$say that item has been removed$$$
								//End hand-off element  
								//@@@Element in Consumer@@@
			send.release();		//Notifying: Next producer can do insertion to the queue 
			return i;
		}		
	}
	
	static class MyBlockingQueue
	{
		private final Lock qLock = new ReentrantLock(true);
		private final Condition fullCond = qLock.newCondition(); 
		private final Condition emptyCond = qLock.newCondition();
		
		private final Queue<Integer> queue;
		private final int LIMIT;
		
		public MyBlockingQueue(int limit){
			this.queue = new LinkedList<Integer>();
			this.LIMIT = limit;
		}
		
		public Integer take() throws InterruptedException{
			qLock.lock();
			System.out.println("Consumer: " +Thread.currentThread().getName() + " take: lock");
			try{
				while(queue.size() == 0)
				{
					System.out.println("Consumer: " +Thread.currentThread().getName() + " take: wait");
					emptyCond.await();
				}
				return queue.poll();
			}finally{
				fullCond.signalAll();
				qLock.unlock();
				System.out.println("Consumer: " +Thread.currentThread().getName() + " take: unlock");
			}
		}
		
		public void put(Integer i) throws InterruptedException{
			qLock.lock();
			System.out.println("Producer: " +Thread.currentThread().getName() + " put: lock");
			try{
				while(queue.size() == LIMIT)
				{
					System.out.println("Producer: " +Thread.currentThread().getName() + " put: wait");
					fullCond.await();
				}
				queue.offer(i);
			}finally{
				emptyCond.signalAll();
				qLock.unlock();
				System.out.println("Producer: " +Thread.currentThread().getName() + " put: unlock");
			}
		}

	}
	
	public void main(String[] args)
	{
		
	}
}
