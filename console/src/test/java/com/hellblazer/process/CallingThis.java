package com.hellblazer.process;

public class CallingThis {
	public CallingThis()
	{
		SecondLayerCalling sc = new SecondLayerCalling();
		sc.test();
		System.out.println("constructor of CallingThis has been created");
	}
	public void printThis()
	{
		System.out.println("print this from inside calling this");
	}
}
