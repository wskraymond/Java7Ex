package com.exam.ex.concurrency;


/*
 * @thread should be so busy that the problem for visibility could happen.@
 * So, it could never happen. 
 */
public class MemConsistency {
	private static int val = 0;
	private static int val2 = 0;
	
	
	public static void main(String[] args)
	{
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = val;
				while(i==0)
				{
//					System.out.println("get");
					i = val;
				}
				
				System.out.println("end");
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				val = 1;
				System.out.println("set");
			}
		}).start();
	}

}
