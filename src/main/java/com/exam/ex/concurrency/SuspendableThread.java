package com.exam.ex.concurrency;


class SuspendableThread implements Runnable{   //Yes, you can say this class' instance is a monitor itself for isSuspended
    private boolean isSuspended;    //!!!impotant -> this is a shared instance variable , access through ThreadCtrl
    private String name;
    private Thread t;
    
    public SuspendableThread(){ this("Thread"); }
    
    public SuspendableThread(String name){     
        this.name = name;
        isSuspended = false;
    }

    public void start(){
        if(t==null)
            t = new Thread(this, name);
        t.start();
    }
    
    @Override
    public void run() {
        try {
            for(int i=20;i>0; i--)
            {
                System.out.println("[" + this.name + "]" + i );
                Thread.sleep(100);
                synchronized(this){ // this monitor will be owned by this thread
                    while(isSuspended) //Access lock -> while loop is used to prevent spurious wakeup
                        this.wait();   //that makes the current thread to wait 
                    
                    Thread.sleep(100);
                    System.out.println("[" + this.name + "]" + "going to next round");
                    
                    Thread.sleep(100);
                    System.out.println("[" + this.name + "]" + "going to next round");
                    
                    Thread.sleep(100);
                    System.out.println("[" + this.name + "]" + "going to next round");
                }
            }
        } catch (InterruptedException ex) {
            return;
        }
    }
    
    public void suspend(){
        isSuspended = true;
    }
    
    public synchronized void resume(){  
        isSuspended = false;    //Access lock 
        //Beware, this is called by parent thread, the (child thread itself) monitor's lock is owned by parent (NOT Child!!!)
        this.notify(); 
        
        try {
			System.out.println("resume(): interleave it");
			
			Thread.sleep(10);
			System.out.println("resume(): interleave it");
			
			Thread.sleep(10);
			System.out.println("resume(): interleave it");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public Thread thread(){
        return this.t;
    }
    
    /*
    [Thread A]20
 	resume(): interleave it	//awaken thread waits for notifying thread(main thread) to release lock
 	resume(): interleave it
 	resume(): interleave it	//release lock
 	Main thread: interleave it	//but both main thread and awaken thread attempts to acquire lock, main thread succeeds. 
 	Main thread: interleave it
 	Main thread: interleave it	//main thread release lock
 	[Thread A]going to next round //this time for awaken thread 
 	[Thread A]going to next round
 	[Thread A]going to next round
 	[Thread A]19
     */
    public static void main(String[] args){
    	try {
            SuspendableThread a = new SuspendableThread("Thread A");
            a.start();
            a.suspend();
            Thread.sleep(555);
            a.resume();
            synchronized(a){
            	System.out.println("Main thread: interleave it");
            	System.out.println("Main thread: interleave it");
            	System.out.println("Main thread: interleave it");
            } 
            
            Thread.sleep(555);
            a.suspend();
            Thread.sleep(555);
            a.resume();
            a.thread().join(55);
        } catch (InterruptedException ex) {
        }
    }
}
