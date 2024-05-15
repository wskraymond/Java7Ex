package com.exam.ex.inheritance;

import java.util.*;
import java.text.*;

class Base
{
	static double field2 = 1.2;
}

class Superclass extends Base
{
	private int a = 1;
	
	protected static String field = "super";
	
	public Superclass()
	{
		//System.out.println(Superclass.class + " - default constructor");
	}
	
	
	public Superclass(String a)
	{
		//System.out.println(Superclass.class + " : " + a);
	}
	
	static Object method(){
		System.out.println("Super method");
		return "123";
	}
}

class Subclass extends Superclass
{
	public static String s = "hello";
	
	public int field = 123;
	
	public Subclass()
	{
		//System.out.println(Subclass.class + " - default constructor");
	}
	
	public static String method(){
		System.out.println("Subclass method");
		return "123";
	}
	public void hiding(){
		System.out.println(field);
		System.out.println(this.field);
		System.out.println(super.field);
		System.out.println(new Subclass().field);
		System.out.println(((Superclass)new Subclass()).field);
		//System.out.println(Subclass.field);
		method();
		new Subclass().method();
		new Superclass().method();
		Subclass.method();
		Superclass.method();
		
		System.out.println(field2);
		System.out.println(this.field2);
		System.out.println(super.field2);
		System.out.println(new Subclass().field2);
	}
	
	
	public Subclass(String a)
	{
		//super(s);	//otherwise, default constructor of superclass
		System.out.println(Subclass.class + " : " + s);
	}
}
public class MyApp extends Thread {
	private String s;
	public MyApp(String a)
	{
		this.s = a;
		System.out.println(this.getClass().getName() + " : " + this.s);
	}
	
	public static void main(String[] args) throws Exception {
		//Class.forName("com.company.ABC");
		//int division = 1/0;
		/*
		char[] str = new char[]{'A','B'};
		List<Character> charList = new ArrayList<Character>();
		charList.add('A');
		System.out.println(charList.get(2));
		*/
		
		new Subclass().hiding();
		int i = 0;
		for(String s = "a"; i < 5; i++) ; 
		Integer o = new Integer(1);
		Integer sh = (Integer)o;
		//System.out.println((Short)o);
	
		Thread t = new Thread(new MyApp("thread"));
		t.sleep(23);
		t.start();
		System.out.println("HIHI");
		same();
		
		Map<MyApp,String> m = new HashMap<MyApp,String>();
		MyApp a = new MyApp("A");
		MyApp b = new MyApp("B");
		m.put(a, "As");
		m.put(b, "Bs");
		
		System.out.println(m + ":" + a + ":" + b);
		System.out.println("A:" + a.hashCode());
		System.out.println("B:" + b.hashCode());
		System.out.println(m.get(a));
		System.out.printf("Boolean %b", true);
		System.out.println();
		Subclass sc = new Subclass("fuck");
		//System.out.println(sc.a);
		
		NumberFormat nf = NumberFormat.getInstance();
		//nf.setMaximumFractionDigits(2);
		System.out.println(nf.parse("1xyz"));
		System.out.println(nf.format("123.456"));
	}
	
	public static void same(){
		System.out.println("same");
	}
}