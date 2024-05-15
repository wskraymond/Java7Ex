package com.exam.ex.generic;

import java.util.*;

class A<T> 
{
	public void doIt(T t){ System.out.println("doIt(T t) under A<T> ");}
	public void doIt(Set<T> s, List<T> l){ System.out.println("doIt(Set<T> s, List<T> l) under A<T>");}	
	//public void doIt(Set<T> s, List<? extends T> l){}	//sub-signature of above, Compilation-Error: "have the same erasure"
	//public void doIt(Set<T> s, List<? super T> l){}	//sub-signature of above, Compilation-Error: "have the same erasure"
}
interface MyIn<T>
{
	void hello(T t);
}

interface MyIn2<T>
{
	void work(T t);
}
class B<T> extends A<T> implements Comparable<Set<T>>, MyIn<T>, MyIn2<T>, Comparator
{
	@Override
	//public int compareTo(Object s) //Error: have the same erasure , yet neither overrides the other
	//public int compareTo(Set s)	//works also
	public int compareTo(Set<T> s)
	{
		System.out.println("comparaTo(Set)");
		return 1;
	}
	
	@Override
	public void hello(T t)
	{
		System.out.println("hello(T t) under MyIn<T>");
	}
	
	@Override
	//public void work(T t)	//formally
	public void work(Object t)	// this eliminate the effect of parameterized type , use this as B<T>'s signature
	{
		System.out.println("work(Object t) under MyIn2<T>");
	}
	
	@Override
	//public int compare(T o1,T o2)		//Error: have the same erasure , yet neither overrides the other
	public int compare(Object o1,Object o2)
	{
		System.out.println("compare(Object o1,Object o2) under Comparator");
		
		return 1;
	}
}
public class Generic 
{
	public static void main(String[] args)
	{
		B b = new B();
		b.compareTo(null);
		//b.compareTo((new ArrayList());   //Compilation-Error: "actual argument ArrayList cannot be converted to Set by method invocation conversion"
		//b.compareTo(new Object());		   //Compilation-Error: "actual argument Object cannot be converted to Set by method invocation conversion"

		B<int[]> bb = new B<int[]>();
		bb.hello(new int[0]);
		bb.work(new Object());
		bb.doIt(new HashSet(), new ArrayList());
		bb.doIt(new int[0]);
		B<boolean[]> bbb = (B<boolean[]>)(B<?>)bb;
		bbb.hello(new boolean[0]);
		B<?> bbbb = bb;		
		
		//see https://docs.oracle.com/javase/tutorial/java/generics/capture.html
		//bbbb.hello(new boolean[0]); //Compilation-Error:  actual argument boolean[] cannot be converted to CAP#1 by method invocation conversion	
		
		List<String> l = new ArrayList<String>();
		List raw = l;	// no warning , generic to raw type	 , but warning: [rawtypes] found raw type: List
		l = raw;		//unchecked conversion , raw type to generic type
		l.add("fuck");
		raw.add(123);	// it is ok, since Generic type is Object, but unchecked type
		System.out.println(l.get(0));
		//Integer i = l.get(0); //  error: incompatible types , type-safe checking
		System.out.println(l.get(0).length());	// (1)
		System.out.println(((String)l.get(0)).length()); // (1) is converted to (2) in type Erasure
		System.out.println(((String)l.get(1)).length());
	}
}