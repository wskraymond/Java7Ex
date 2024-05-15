package com.exam.ex.modifier;

class Base
{
	protected void method(){
		System.out.println("ABC");
	}
}

public class Modifier extends Base 
{
	protected void methodB()
	{
		method();
		new Base().method();
	}
	public static void main(String[] srgs){
		new Base();
	}
}