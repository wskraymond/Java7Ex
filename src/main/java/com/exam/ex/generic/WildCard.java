package com.exam.ex.generic;

import java.util.*;

class InOutList{
	public static <T> void help(List<T> capture){
		capture.set(0, capture.get(0));
	}
	
	public static void readFromList(List<? extends Number> in){
		System.out.println(in.get(0).intValue());	//read
		//in.add(new Object());						//ERROR
		
		in.add(null);								//add null
		help(in);
		in.iterator().remove();				//remove
		System.out.println(in);
		in.clear();									//clear
		System.out.println(in);
	}
	
	public static <T extends Number> void writeToList(List<T> out){
		out.add((T) Integer.valueOf(12));
		out.add((T) Double.valueOf("12.2"));
		out.add((T) Long.valueOf(100L));

		
		out.add(null);								//add null
		help(out);
		out.iterator().remove();				//remove
		System.out.println(out);
		out.clear();									//clear
		System.out.println(out);
	}
}
class ACard<T>
{
	private T t;
	public void set(T t){ this.t = t; }
	public T get(){ return t; };
}

public class WildCard 
{
	public static void main(String[] args)
	{
		ACard a = new ACard();
		ACard<?> aa = a;
		ACard<? extends Number> ae = (ACard<? extends Number>)aa;
		ACard<? super Number> as = (ACard<? super Number>)aa;
		ACard<Number> an = (ACard<Number>)aa;
		
		Object obj = new Object();
		Number num = 1.5;
		Integer i = 5;
		
		a.set(obj);
		a.set(num);
		a.set(i);
		
		obj = a.get();
		//num = a.get();
		//i = a.get();
		
		
		/* <?>: according hierarchy, any <? extends/super> can be assigned to it without unchecked cast warning*/
		/* thus, only can return Object type*/
		//aa.set(obj);
		//aa.set(num);
		//aa.set(i);
		
		obj = aa.get();	
		//num = aa.get();
		//i = aa.get();
		
		//ae.set(obj);
		//ae.set(num);
		//ae.set(i);
		
		/* <? extends X>: according hierarchy, any <? extends X's subtype> can be assigned to it without unchecked cast warning*/
		/* where CAP#1 is a fresh type-variable:
			CAP#1 extends Number from capture of ? extends Number */
		/* thus, only can return type X (or X's superType)*/
		obj = ae.get();
		num = ae.get();
		//i = ae.get();
		
		/* <? super X>: according hierarchy, any <? super X's super type> can be assigned to it without unchecked cast warning*/
		/* thus, only can use X's sub type as argument*/
		//as.set(obj);
		as.set(num);
		as.set(i);
		
		//obj = as.get();
		//num = as.get();
		//i = as.get();
	}
}