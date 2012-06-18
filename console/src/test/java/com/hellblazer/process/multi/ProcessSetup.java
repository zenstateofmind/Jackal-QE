package com.hellblazer.process.multi;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ProcessSetup {
    public static void main(String[] argv) throws Exception {
    	//get a string with the class of the member which has to be configured
    	String className = argv[0];
    	System.out.println("CLASSNAME:  "+className);
    	Class<?> config = Class.forName(className);
		new AnnotationConfigApplicationContext(config);
		Thread.sleep(500000000);
    }
}
