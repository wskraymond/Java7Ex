import java.lang.*;
import java.io.*;
import java.util.*;

class SuperHandle {
	
	void mySuperHandle(){
		System.out.println("mySuperHandle");
	}
	
	protected final Collection<Integer> superHandle0(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle0");
		return new LinkedHashSet<Integer>();
	}
	
	private Collection<Integer> superHandle1(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle1" + "super reference");
		return new LinkedHashSet<Integer>();
	}
	
	protected Collection<Integer> superHandle2(final int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle2");
		return new LinkedHashSet<Integer>();
	}
	
	protected HashSet<Integer> superHandle3(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle3");
		return new HashSet<Integer>();
	}
	
	protected HashSet<Integer> superHandle4(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle4");
		return new HashSet<Integer>();
	}
	
	protected SuperHandle superHandle5(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle5" + "superClass Impl");
		return new HandleException();
	}
	
	class SubHandle extends SuperHandle
	{
		/* Since superHandle1 is private , it cannot be overriden by its subclasses
		@Override
		protected Collection<Integer> superHandle1(int i) throws IOException
		{
			return new HashSet<Integer>();
		}
		*/
	}
}
public class HandleException extends SuperHandle
{
	/* Since superHandle0 is final, CANNOT give its own definition of such a method, which may
	have the same signature
	protected Collection<Integer> superHandle0(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle1");
		return new HashSet<Integer>();
	}
	*/
	
	/* Since superHandle1 is private (Unaccessible outside class), 
		it cannot be overriden by its subclasses
	@Override
	protected Collection<Integer> superHandle1(int i) throws IOException
	{
		return new HashSet<Integer>();
	}
	*/
	
	void myHandleException(){
		System.out.println("myHandleException");
	}
	
	protected Collection<Integer> superHandle1(int i) throws IOException
	{
		System.out.println(this.getClass() + "superHandle1" + "subtype reference");
		return new HashSet<Integer>();
	}
	
	@Override
	public HashSet<Integer> superHandle2(int i) throws FileNotFoundException
	{
		System.out.println(this.getClass() + "superHandle2");
		return new HashSet<Integer>();
	}
	
	/* Since return type only allow covariance (subtyping), so upcasting is not allowed
	 * Remark: upcasting is not Type-safe. i.e Collection -> HashSet
	@Override
	protected Collection<Integer> superHandle3(final int i) throws Exception
	{
		System.out.println(this.getClass() + "superHandle3");
		return new HashSet<Integer>();
	}
	*/
	
	/* Since upcasting Exception is not Type-safe. i.e Exception -> IOException
	@Override
	protected HashSet<Integer> superHandle4(int i) throws Exception
	{
		System.out.println(this.getClass() + "superHandle4");
		return new HashSet<Integer>();
	}
	*/
	
	@Override
	protected HandleException superHandle5(int i)
	{
		System.out.println(this.getClass() + "superHandle5" + "subClass Impl");
		return new HandleException();
	}
	
	public static void handle1(int i) throws IOException {
		switch(i)
		{
			case 1:
				throw new FileNotFoundException();
			//case 2:
			//	throw new ClassNotFoundException();
			//case 3:
			//	throw new Exception();
			default:
				throw new IOException();
		}
	}
	public static void main(String[] args) throws Exception {
		
		try{
			HandleException h = new HandleException();
			Collection<Integer> c = h.superHandle2(1);
			System.out.println(c.getClass());
		}
		catch(IOException e){
			
		}
		
		//superHandle5 -> overriden with less Exception
		// 1. instance of HandleException 2. HandleException Reference
		HandleException h2 = new HandleException();
		h2.superHandle5(1).myHandleException();
		
		
		// 1. instance of HandleException 2. SuperHandle Reference
		SuperHandle h3 = new HandleException();
		h3.superHandle5(1).mySuperHandle();
		//h3.superHandle5(1).myHandleException(); //Compilation Error : becoz return type is SuperHandle
		
		// 1. instance of SuperHandle 2. SuperHandle Reference
		SuperHandle h4 = new SuperHandle();
		//h4.superHandle5(1).myHandleException(); //Compilation Error : becoz return type is SuperHandle and Instance is SuperHandle
	}
}