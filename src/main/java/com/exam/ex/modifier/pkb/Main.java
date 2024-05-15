package com.exam.ex.modifier.pkb;
import com.exam.ex.modifier.pka.*;

class Child extends Base
{
	protected void methodB(){
		System.out.println("method B");
		methodA();
		
		//new Base().methodA(); //unable to access
		new Child().methodA();
	}
}


public class Main 
{
	public static void main(String[] args)
	{
//		new Child().methodA(); //unable to access
	}
}