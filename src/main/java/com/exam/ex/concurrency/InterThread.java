/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.exam.ex.concurrency;

import java.util.Random;

/**
 *
 * @author raymond
 */

class Chat{
    private boolean flag; //condition for lock , it is a shared instance
    
    public synchronized void question(String tName,String msg) throws InterruptedException{ //Access Lock -> Yes, monitor(Chat)'s lock will be owned by only one thread
        while(flag){    //condition to test <- need Access Lock <- so , accessing thread will take/own this monitor 
            try{
                System.out.println("[" + tName + "] waits for " + 10000 + "milliseconds");
                this.wait(10000);    //the calling thread will go into block(timed-wait) state, AND also relinquish(give up) the lock / synchronization on this monitor
            }catch(InterruptedException e){
                System.out.println("[" + tName + "] " + e.getMessage());
                throw e;         //if intterrupted then throw exception until it is catched by the run() method to handle it
            }catch(IllegalMonitorStateException e){ //!!Important -> The thread must own monitor's lock before calling <monitor>.wait().
                System.out.println("[" + tName + "] " + e.getMessage());
                return;
            }
            finally{
                System.out.println("[" + tName + "] " + "wakes up");
            }
        }
        
        System.out.println("[" + tName + "][Question] " + msg);
        flag = true;   //let let question to wait , but answer to run
        this.notifyAll();   //wake up all threads waiting on this monitor -> the awakened threads enjoy no reliable privilege or disadvantage in being the next thread to lock this object.
    } 
    
    public synchronized void answer(String tName,String msg) throws InterruptedException{ //Access Lock -> Yes, monitor(Chat)'s lock will be owned by only one thread
        while(!flag){    //condition to test <- need Access Lock <- so , accessing thread will take/own this monitor 
            try{
                System.out.println("[" + tName + "] waits for " + 10000 + "milliseconds");
                this.wait(10000);    //the calling thread will go into block(timed-wait) state, AND also relinquish(give up) the lock / synchronization on this monitor
            }catch(InterruptedException e){
                System.out.println("[" + tName + "] " + e.getMessage());
                throw e;         //if intterrupted then throw exception until it is catched by the run() method to handle it
            }catch(IllegalMonitorStateException e){ //!!Important -> The thread must own monitor's lock before calling <monitor>.wait().
                System.out.println("[" + tName + "] " + e.getMessage());
                return;
            }
            finally{
                System.out.println("[" + tName + "] " + "wakes up");
            }
        }
        
        System.out.println("[" + tName + "][Question] " + msg);
        flag = false;   //let let answer to wait , but question to run
        this.notifyAll();   //wake up all threads waiting on this monitor -> the awakened threads enjoy no reliable privilege or disadvantage in being the next thread to lock this object.
    } 
    
}

class SynchronizedCounter {
    private int counter;    //this shared variable is thread-safe
    
    SynchronizedCounter(){ this(0); }
    
    SynchronizedCounter(int initValue){
        this.counter = initValue;
    }
    
    public synchronized void increment(){
        this.increment(1);  //Re-entrant Synchronization for "this" object
    }
    
    public synchronized void increment(int step){
        this.counter+= step;
    }
    
    public synchronized void decrement(){
        this.decrement(1);  //Re-entrant Synchronization for "this" object
    }
    
    public synchronized void decrement(int step){
        this.counter-= step;
    }
    
    public synchronized int value(){
        return this.counter;
    }
    
    @Override
    public String toString(){
        return "[SynchronizedCounter]'s value = " + this.value();
    }
}

//Yes, The Class containg the main method is just as normal as other classes are
//Thus, it can extends any classes including Thread Class
//Also, 
public class InterThread extends Thread 
{
    //private Concurrency c = new Concurrency();  //That cause stackoverflow if and only 'new' this class
    
    //Composition Approach, use it when just want to use the Thread class 
    public static class TestThread implements Runnable{
        private Thread t;
        private String name;
        private final SynchronizedCounter[] shared; //best practice to set it final for monitor
        private final Object masterMonitor;         //best practice to set it final for monitor
        
        public TestThread(){
            this("TestThread",null,null);
        }
        
        public TestThread(String name, SynchronizedCounter[] shared, Object masterMonitor){
            this.name = name;
            this.shared = shared;
            this.masterMonitor = masterMonitor;
        }
        
        public Thread thread(){
            return this.t;
        }
        //The Best practice to have start()
        public void start(){
            //only exec once for start() 
            if(t==null){
                t = new Thread(this,this.name);
                t.start();
            }
        }
        public void printState(){
            System.out.println(this.name + "[state] " + t.getState());
        }
        @Override
        public void run() 
        {
            printState();
            System.out.println(this.name + "'s run method is called on a thread");
            
            //although SynchronizedCounter's objects are thread-safe themself
            //, here two counter objects (shared[0],shared[1]) are interleaved each other
            //Thus, there is still a race condition problem for the following block of statements
            //Then, we need to synchronize the following block through "shared" array as monitor 
            //,when two shared counters are access by one of them.
            synchronized(this.shared)
            {
                this.shared[0].increment(); //lock for shared[0]
                Random rand = new Random();
                int  n = rand.nextInt(1);
                if(n==0)
                {
                    long start = System.currentTimeMillis();
                    try {
                        System.out.println(this.name + " sleeps");
                        Thread.sleep(1000); //The thread does not lose ownership of any monitors.
                        System.out.println(this.name + " wakes up (elapsed time = " + (System.currentTimeMillis() - start ) + ")");
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(TestThread.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println(this.name + " received interruptedXxception - " + ex.getMessage());
                        return;
                    }
                }
                this.shared[1].increment(this.shared[0].value());   //lock for shared[0] , shared[1]
                //No interleave, so thread-safe counters has no race condition here
                //But may print it asyncrhonizedly,if there are other thread print out sth (here , no neccessary)
                synchronized(this.masterMonitor){
                    System.out.println(this.name + " shared[0] "+this.shared[0]);
                    System.out.println(this.name + " shared[1] " + this.shared[1]);
                }
            }
          
        }
    }
    
    /* test 1 - possible result */
  //Thread 1[state] RUNNABLE
  //Thread 1's run method is called on a thread
  //Thread 1 sleeps
  //Thread 3[state] RUNNABLE
  //Thread 3's run method is called on a thread
  //Thread 2[state] RUNNABLE
  //Thread 2's run method is called on a thread
  //Thread 3will receive interruptedException if some call support that
  //Thread 1received interruptedXxception - sleep interrupted
  //Thread 1will receive interruptedException if some call support that
  //Thread 2 sleeps
  //Thread 2 wakes up (elapsed time = 1015)
  //Thread 2 shared[0] [SynchronizedCounter]'s value = 2
  //Thread 2 shared[1] [SynchronizedCounter]'s value = 2
  //Thread 3 sleeps
  //Thread 3received interruptedXxception - sleep interrupted
    /**
     * @param args the command line arguments
     */
    public static void test1(){
        // TODO code application logic here
        //Shared Objects
        SynchronizedCounter[] shared = new SynchronizedCounter[]{new SynchronizedCounter(), new SynchronizedCounter()};
        //Master Monitor is used to occupy the CPU resource for general purpose
        Object masterMonitor = new Object();    
        
        //Threads
        TestThread[] ts = new TestThread[]{
            new TestThread("Thread 1",shared,masterMonitor),
            new TestThread("Thread 2",shared,masterMonitor),
            new TestThread("Thread 3",shared,masterMonitor)
        };
        
        long[] startTime = new long[ts.length];
        for(int i=0; i < ts.length;i++)
        {
            startTime[i] = System.currentTimeMillis();
            ts[i].start();
            
            //Beware: <thread>.join() v.s <monitor>.wait()
            //<thread>.join() is called by parent thread , then parent is waiting child thread to be killed
            //So join means establish a parent-children relationship
            //<monitor>.wait() is called by current owning thread 
            //ts[i].thread().join();        //-> then all thread are synchronized , in series
        }
        
        try{
            for (TestThread t : ts) {
                t.thread().join(1000); //-> then all thread are runned in parallel , wait for all thread to be killed
            }
        }
        catch(InterruptedException e)
        {
            return; //Yes, since the parent thread (main thread here) is in waiting state , best practice to provide interrupt handling!!! 
        }
        
        
        boolean isLoop = true;
        
        while(isLoop)
        {
            isLoop = false;
            for(int i=0; i < ts.length;i++)
            {
                if(ts[i].thread().isAlive() && ts[i].thread().getState() != State.RUNNABLE)
                {
                    isLoop = true;
                    if(System.currentTimeMillis() - startTime[i] > 555)
                    {
                      ts[i].thread().interrupt();
                      System.out.println(ts[i].name + " will receive interruptedException if some call support that");
                      //!!!cannot do that since once thread get intterrupted ,then interrupt state will be reset!!
                      //while(ts[i].thread().isInterrupted()); //Halt problem will heppen here, so be careful to use isInterrupted()
                    }
                }
            }          
        }
        
        System.out.println( "free RAM = " +Runtime.getRuntime().freeMemory());
        
        //Concurrency c = new Concurrency();  //To trigger chain effect on recursive type
        
    }
    
    public static void main(String[] args){
        test1();
    }
}
