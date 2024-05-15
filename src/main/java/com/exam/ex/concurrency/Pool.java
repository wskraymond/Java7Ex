package com.exam.ex.concurrency;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Pool {
	private static int corePoolSize = 5;
	private static int maximumPoolSize=10;
	private static long keepAliveTime = 90;
	private static RejectedExecutionHandler rHanlder = new ThreadPoolExecutor.AbortPolicy();
	
	static class ScheduledInteger implements Delayed
	{
		private Integer val;
		public Integer getVal() {
			return val;
		}
		public void setVal(Integer val) {
			this.val = val;
		}

		private long time;
		
		public ScheduledInteger(long time, Integer val)
		{
			this.time = time;
			this.val  = val;
		}
		@Override
		public int compareTo(Delayed o) {
			return (int) (getDelay(null) - o.getDelay(null));
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return this.time - System.currentTimeMillis();
		}
		
	}
	
	/*Queue*/
	private static final MyConLinkedQueue mlq = new MyConLinkedQueue();
	private static final Queue<Integer> lq = new ConcurrentLinkedQueue<Integer>();
	
	/*Blocking Queue*/
	private static final Synchronizer.MyBlockingQueue mq = new Synchronizer.MyBlockingQueue(5);
	private static final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(10, true);
	//Synchronous
	private static final Synchronizer.SyncQueue msq = new Synchronizer.SyncQueue();
	private static final BlockingQueue<Integer> sbq = new SynchronousQueue<Integer>(true);
	//Priority
	private static final BlockingQueue<Integer> pbq = new PriorityBlockingQueue<Integer>();
	private static final BlockingQueue<ScheduledInteger> dbq = new DelayQueue<ScheduledInteger>();
	
	/*Transfer Queue*/
	private static final TransferQueue<Integer> tbq = new LinkedTransferQueue<Integer>();
	private static final AtomicInteger counter = new AtomicInteger(0);
	
	static interface Produce 
	{
		public void produce(Integer tmp) throws InterruptedException;
	}
	
	static interface Consume 
	{
		public Integer consume() throws InterruptedException;
	}
	static class Producer implements Runnable
	{
		private final Produce p;
		public Producer(Produce p)
		{
			this.p = p;
		}
		
		@Override
		public void run() {
			try {
				int tmp = counter.getAndIncrement();
				System.out.println("Starting Producer: " +Thread.currentThread().getName() + " produce => " + tmp);
				p.produce(tmp);
				System.out.println("End Producer: " +Thread.currentThread().getName() + " produce => " + tmp);
			} catch (InterruptedException e) {
				System.out.println("Producer: " +Thread.currentThread().getName() + "is interruped");
			}
		}
	}
	
	static class Consumer implements Callable<Integer>, Runnable
	{
		private final Consume c;
		public Consumer(Consume c){
			this.c = c;
		}
		
		@Override
		public Integer call() throws Exception {
			Integer tmp;
			try {
				System.out.println("Starting Consumer: " +Thread.currentThread().getName());
				tmp = c.consume();
				System.out.println("End Consumer: " +Thread.currentThread().getName() + " consume => " + tmp);
				return tmp;
			} catch (InterruptedException e) {
				System.out.println("Consumer: " +Thread.currentThread().getName() + "is interruped");
				throw e;
			}
		}
		
		@Override
		public void run() {
			try {
				call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
	
	private volatile static String result = "";
	private volatile static boolean flag = true;
	public static void main(String[] args) throws InterruptedException
	{
		//test();
//		testSync();
//		testTransfer();
//		testPriority();
//		testDelay();
		testQueue();
	}
	
	public static void testQueue() throws InterruptedException
	{
		Producer p = new Producer(new Produce() {
			@Override
			public void produce(Integer tmp) throws InterruptedException {
				mlq.add(tmp.intValue());
			}
		});
		
		ExecutorService es = Executors.newFixedThreadPool(50);
		
		for (int i = 0; i < 50; i++) {
			es.execute(p);
		}
		
		es.awaitTermination(10, TimeUnit.SECONDS);
		es.shutdownNow();
		System.out.println("queue content - " + mlq);
		System.out.println("queue size" + mlq.size());
		System.out.println("queue tail" + mlq.getTail());
		
	}
	
	public static void testDelay() throws InterruptedException 
	{
		Producer p = new Producer(new Produce() {
			
			@Override
			public void produce(Integer tmp) throws InterruptedException {
				dbq.put(new ScheduledInteger(System.currentTimeMillis() + 1000 + 900 - tmp.longValue()*100, tmp));
			}
		});
		final Consumer c = new Consumer(new Consume() {
			
			@Override
			public Integer consume() throws InterruptedException {
				return dbq.take().getVal();
			}
		});
		ExecutorService h = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		//Customized ExecutorService
		final ExecutorService es = new ThreadPoolExecutor(2, 2, keepAliveTime, TimeUnit.SECONDS, workQueue, Executors.defaultThreadFactory(), rHanlder);
		
		//Preconfigured ScheduledExecutorService
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);
		//Multiple Consumer
		final ScheduledFuture<?> sf = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		final ScheduledFuture<?> sf2 = ses.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				try {
					System.out.println("Examine head ...");
					ScheduledInteger item = dbq.peek();
					System.out.println("Examine head" + (item!=null ? item.getDelay(null) : item));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("starting shudtown service");
				sf.cancel(true);
				flag = false;
				es.shutdownNow();
				System.out.println("service end");
			}}
		, 1, TimeUnit.MINUTES);
		
		//Multi Producer
		int i = 0;
		while(flag && i++ < 10)
		{
			try {
				es.execute(p);
			} catch (RejectedExecutionException e) {
				System.out.println(e.getMessage());
				Thread.sleep(500);
			}
			finally{
//				Thread.sleep(100);
			}
			
		}
		
		System.out.println(((ThreadPoolExecutor)es).getQueue().toArray().toString());
//		System.out.println("Terminating");
//		es.awaitTermination(1, TimeUnit.MINUTES);
//		ses.awaitTermination(1, TimeUnit.SECONDS);
//		System.out.println("Done");
//		ses.shutdownNow();
//		h.shutdown();
//		System.out.println("Stoped");
//		System.out.println("Result => " + result);
//		System.out.println("Result's size => " + result.length());
	}
	
	public static void testPriority() throws InterruptedException 
	{
		Producer p = new Producer(new Produce() {
			
			@Override
			public void produce(Integer tmp) throws InterruptedException {
				pbq.put(100 - tmp);
			}
		});
		final Consumer c = new Consumer(new Consume() {
			
			@Override
			public Integer consume() throws InterruptedException {
				return pbq.take();
			}
		});
		ExecutorService h = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		//Customized ExecutorService
		final ExecutorService es = new ThreadPoolExecutor(2, 2, keepAliveTime, TimeUnit.SECONDS, workQueue, Executors.defaultThreadFactory(), rHanlder);
		
		//Preconfigured ScheduledExecutorService
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		//Multiple Consumer
		final ScheduledFuture<?> sf = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("starting shudtown service");
				sf.cancel(true);
				flag = false;
				es.shutdownNow();
				System.out.println("service end");
			}}
		, 1, TimeUnit.MINUTES);
		
		//Multi Producer
		int i = 0;
		while(flag && i++ < 10)
		{
			try {
				es.execute(p);
			} catch (RejectedExecutionException e) {
				System.out.println(e.getMessage());
				Thread.sleep(500);
			}
			finally{
//				Thread.sleep(100);
			}
			
		}
		
		System.out.println(((ThreadPoolExecutor)es).getQueue().toArray().toString());
//		System.out.println("Terminating");
//		es.awaitTermination(1, TimeUnit.MINUTES);
//		ses.awaitTermination(1, TimeUnit.SECONDS);
//		System.out.println("Done");
//		ses.shutdownNow();
//		h.shutdown();
//		System.out.println("Stoped");
//		System.out.println("Result => " + result);
//		System.out.println("Result's size => " + result.length());
	}
	
	/*
	Starting Producer: pool-1-thread-1 produce => 0
	transfer - 0
	Starting Producer: pool-1-thread-2 produce => 1
	End Producer: pool-1-thread-2 produce => 1
	Starting Producer: pool-1-thread-2 produce => 2
	End Producer: pool-1-thread-2 produce => 2
	Starting Producer: pool-1-thread-2 produce => 3
	End Producer: pool-1-thread-2 produce => 3
	Starting Producer: pool-1-thread-2 produce => 4
	transfer - 4
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 0
	End Producer: pool-1-thread-1 produce => 0
	Starting Producer: pool-1-thread-1 produce => 5
	End Producer: pool-1-thread-1 produce => 5
	Starting Producer: pool-1-thread-1 produce => 6
	End Producer: pool-1-thread-1 produce => 6
	Starting Producer: pool-1-thread-1 produce => 7
	End Producer: pool-1-thread-1 produce => 7
	Starting Producer: pool-1-thread-1 produce => 8  | pool-1-thread-2 Producer:blocking for 4 === <8,7,6,5,4,3,2,1> ===== Consumer
	transfer - 8
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 1
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 2
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 3
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 4
	End Producer: pool-1-thread-2 produce => 4			 
	Starting Producer: pool-1-thread-2 produce => 9
	End Producer: pool-1-thread-2 produce => 9
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 5
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 6
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 7
	Starting Consumer: pool-2-thread-1
	End Producer: pool-1-thread-1 produce => 8
	End Consumer: pool-2-thread-1 consume => 8
	Starting Consumer: pool-2-thread-1
	End Consumer: pool-2-thread-1 consume => 9
	Starting Consumer: pool-2-thread-1
	*/
	public static void testTransfer() throws InterruptedException 
	{
		Producer p = new Producer(new Produce() {
			
			@Override
			public void produce(Integer tmp) throws InterruptedException {
				if(tmp % 4 == 0)
				{
					System.out.println("transfer - " + tmp);
					tbq.transfer(tmp);
				}
				else
				{
					tbq.put(tmp);
				}
			}
		});
		final Consumer c = new Consumer(new Consume() {
			
			@Override
			public Integer consume() throws InterruptedException {
				return tbq.take();
			}
		});
		ExecutorService h = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		//Customized ExecutorService
		final ExecutorService es = new ThreadPoolExecutor(2, 2, keepAliveTime, TimeUnit.SECONDS, workQueue, Executors.defaultThreadFactory(), rHanlder);
		
		//Preconfigured ScheduledExecutorService
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		//Multiple Consumer
		final ScheduledFuture<?> sf = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("starting shudtown service");
				sf.cancel(true);
				flag = false;
				es.shutdownNow();
				System.out.println("service end");
			}}
		, 1, TimeUnit.MINUTES);
		
		//Multi Producer
		int i = 0;
		while(flag && i++ < 10)
		{
			try {
				es.execute(p);
			} catch (RejectedExecutionException e) {
				System.out.println(e.getMessage());
				Thread.sleep(500);
			}
			finally{
//				Thread.sleep(100);
			}
			
		}
		
		System.out.println(((ThreadPoolExecutor)es).getQueue().toArray().toString());
//		System.out.println("Terminating");
//		es.awaitTermination(1, TimeUnit.MINUTES);
//		ses.awaitTermination(1, TimeUnit.SECONDS);
//		System.out.println("Done");
//		ses.shutdownNow();
//		h.shutdown();
//		System.out.println("Stoped");
//		System.out.println("Result => " + result);
//		System.out.println("Result's size => " + result.length());
	}
	
	public static void testSync() throws InterruptedException 
	{
		Producer p = new Producer(new Produce() {
			
			@Override
			public void produce(Integer tmp) throws InterruptedException {
//				sbq.put(tmp);
//				msq.put(tmp);
				sbq.put(tmp);
//				sbq.put(tmp);
//				sbq.offer(tmp,1000L, TimeUnit.MILLISECONDS);
			}
		});
		final Consumer c = new Consumer(new Consume() {
			
			@Override
			public Integer consume() throws InterruptedException {
//				return sbq.take();
//				return msq.take();
				return sbq.poll();
//				return sbq.poll(800L, TimeUnit.MILLISECONDS);
//				return sbq.poll(800L, TimeUnit.MILLISECONDS);
			}
		});
		ExecutorService h = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		//Customized ExecutorService
		final ExecutorService es = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, Executors.defaultThreadFactory(), rHanlder);
		
		//Preconfigured ScheduledExecutorService
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
		//Multiple Consumer
		final ScheduledFuture<?> sf = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		final ScheduledFuture<?> sf2 = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		final ScheduledFuture<?> sf3 = ses.scheduleAtFixedRate(c, 50, 100, TimeUnit.MILLISECONDS);
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("starting shudtown service");
				sf.cancel(true);
				sf2.cancel(true);
				sf3.cancel(true);
				flag = false;
				es.shutdownNow();
				System.out.println("service end");
			}}
		, 1, TimeUnit.MINUTES);
		
		//Multi Producer
		int i = 0;
		while(flag && i++ < 10)
		{
			try {
				es.execute(p);
			} catch (RejectedExecutionException e) {
//				System.out.println(e.getMessage());
				Thread.sleep(500);
			}
			finally{
//				Thread.sleep(100);
			}
			
		}
		
		System.out.println("Terminating");
		es.awaitTermination(1, TimeUnit.SECONDS);
		ses.awaitTermination(1, TimeUnit.SECONDS);
		System.out.println("Done");
		ses.shutdownNow();
		h.shutdown();
		System.out.println("Stoped");
		System.out.println("Result => " + result);
		System.out.println("Result's size => " + result.length());
	}
	
	public static void test() throws InterruptedException 
	{
		Producer p = new Producer(new Produce() {
			
			@Override
			public void produce(Integer tmp) throws InterruptedException {
				mq.put(tmp);
			}
		});
		Consumer c = new Consumer(new Consume() {
			
			@Override
			public Integer consume() throws InterruptedException {
				return mq.take();
			}
		});
		ExecutorService h = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		
		
		//Customized ExecutorService
		final ExecutorService es = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, Executors.defaultThreadFactory(), rHanlder);
		
		//Preconfigured ScheduledExecutorService
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
		final ScheduledFuture<?> sf = ses.scheduleAtFixedRate(p, 50, 100, TimeUnit.MILLISECONDS);
		ses.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("starting shudtown service");
				sf.cancel(true);
				flag = false;
				es.shutdownNow();
				System.out.println("service end");
			}}
		, 1, TimeUnit.MINUTES);
		
		//consuming
		while(flag)
		{
			try {
				final Future<Integer> f = es.submit((Callable<Integer>)c);
				h.submit(new Callable<String>(){
					@Override
					public String call() throws Exception {
						return result = result + f.get() + " ";
					}
				});
			} catch (RejectedExecutionException e) {
				System.out.println(e.getMessage());
				Thread.sleep(200);
			}
			finally{
//				Thread.sleep(100);
			}
			
		}
		
		System.out.println("Terminating");
		es.awaitTermination(1, TimeUnit.SECONDS);
		System.out.println("Done");
		ses.shutdownNow();
		h.shutdown();
		System.out.println("Stoped");
		System.out.println("Result => " + result);
		System.out.println("Result's size => " + result.length());
	}
}
