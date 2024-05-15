package com.exam.ex.concurrency;

import java.util.*;
import java.util.concurrent.*;

public class MySynCollection<E> implements Collection<E> {
	private Collection<E> c;
	public MySynCollection(Collection<E> c){
		this.c = c;
	}
	
	@Override
	public synchronized int size() {
		return this.c.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return this.c.isEmpty();
	}

	@Override
	public synchronized boolean contains(Object o) {
		return this.c.contains(o);
	}

	@Override
	public synchronized Iterator<E> iterator() {
		return this.c.iterator();
	}

	@Override
	public synchronized Object[] toArray() {
		return this.c.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] a) {
		return this.c.toArray(a);
	}

	@Override
	public synchronized boolean add(E e) {
		return this.c.add(e);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return this.c.remove(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c) {
		return this.c.containsAll(c);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> c) {
		return this.c.addAll(c);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return this.c.removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		return this.c.retainAll(c);
	}

	@Override
	public synchronized void clear() {
		this.c.clear();
	}
	
	static class Traversal implements Callable<String>
	{
		private String name;
		private Collection<String> c;
		
		public Traversal(String name, Collection<String> c) {
			super();
			this.name = name;
			this.c = c;
		}

		@Override
		public String call() throws Exception {
			try {
				for(String item : c)
				{
					System.out.println(name + " - " + item);
					Thread.sleep(3000);
				}
			} catch (Exception e) {
				throw new Exception(name, e);
			}
			return null;
		}
		
	}
	
	static class SyncTraversal implements Callable<String>
	{
		private String name;
		private Collection<String> c;
		
		public SyncTraversal(String name, Collection<String> c) {
			super();
			this.name = name;
			this.c = c;
		}

		@Override
		public String call() throws Exception {
			try {
				synchronized(c){
					for(String item : c)
					{
						System.out.println(name + " - " + item);
						Thread.sleep(3000);
					}
				}
			} catch (Exception e) {
				throw new Exception(name, e);
			}
			return null;
		}
		
	}
	
	static class Modifier implements Callable<String>
	{
		private String name;
		private Collection<String> c;
		
		public Modifier(String name, Collection<String> c) {
			super();
			this.name = name;
			this.c = c;
		}

		@Override
		public String call() throws Exception {
			Thread.sleep(3000);
			c.remove("B");
			System.out.println(name + " - " + "remove B");
			Thread.sleep(3000);
			c.add("E");
			System.out.println(name + " - " + "add E");
			Thread.sleep(3000);
			c.remove("D");
			System.out.println(name + " - " + "remove D");
			Thread.sleep(3000);
			c.add("F");
			System.out.println(name + " - " + "add F");
			return null;
		}
		
	}
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		final List<String> l = new ArrayList<String>(Arrays.asList("A","B","C","D")); 
/*		for(int i = 0 ; i < 100000; i++)
		{
			l.add(String.valueOf(i));
		}*/
		final Collection<String> msc = new MySynCollection<String>(l);
		final List<String> sl = Collections.synchronizedList(l);
		final List<String> cl = new CopyOnWriteArrayList<String>(l);
		final Set<String> cs = new ConcurrentSkipListSet<String>(l);
		
		long start, end;
		int size;
		
		ExecutorService es = Executors.newFixedThreadPool(10);
		Future<String> f1 = es.submit(new Modifier("l", l));
		Future<String> f2 = es.submit(new Modifier("l", l));
		Future<String> f4 = es.submit(new Modifier("l", l));
		Future<String> f5 = es.submit(new Modifier("l", l));
/*		Future<String> lf = es.submit(new Modifier("sl", sl));
		Future<String> rclf = es.submit(new Modifier("cl", cl));
		Future<String> rcsf = es.submit(new Modifier("cs", cs));*/
////		Future<String> mscf = es.submit(new SyncTraversal("msc", msc));
//		Future<String> slf = es.submit(new SyncTraversal("sl", sl));
//		Future<String> clf = es.submit(new Traversal("cl", cl));
//		Future<String> csf = es.submit(new Traversal("cs", cs));
		
/*		Thread.sleep(3000);
		start = System.currentTimeMillis();
		size = cs.size();
		end = System.currentTimeMillis();
		System.out.println("cs - " + (end - start) + " size - " + size);
		
		Thread.sleep(3000);
		start = System.currentTimeMillis();
		size = cl.size();
		end = System.currentTimeMillis();
		
		System.out.println("cl - " + (end - start) + " size - " + size);
		
		Thread.sleep(3000);
		start = System.currentTimeMillis();
		size = sl.size();
		end = System.currentTimeMillis();
		
		System.out.println("sl - " + (end - start) + " size - " + size);*/
		
		int i = 0;
		for(Future f : new Future[]{f1,f2,f4,f5})
		{
			try {
				System.out.println(++i);
				f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Future<String> f3 = es.submit(new Traversal("l", l));
		f3.get();
		
		System.out.println("ending");
		es.shutdownNow();
	}

}
