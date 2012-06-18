/** 
 * (C) Copyright 2011 Hal Hildebrand, all rights reserved.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.hellblazer.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.Before;
import org.junit.Test;

import com.hellblazer.process.JavaProcess;
import com.hellblazer.process.NoLocalJmxConnectionException;
import com.hellblazer.process.impl.JavaProcessImpl;
import com.hellblazer.process.impl.ManagedProcessFactoryImpl;
import com.hellblazer.process.multi.Trial;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * @author Hal Hildebrand
 * 
 */
public class JavaProcessTestDupMine extends ProcessTest {
	protected static final String TEST_DIR = "test-dirs/java-process-test";
	protected static final String TEST_JAR = "test.jar";
	MBeanServerConnection connection;
	JMXConnector connector;
	ManagedProcessFactoryImpl processFactory = new ManagedProcessFactoryImpl();
	protected File testDir;

	final int numberOfProcesses = 20;
	protected void copyTestClassFile() throws Exception {

		ArrayList<Class> classFiles = new ArrayList<Class>();
		classFiles.add(Trial.class);
		classFiles.add(CallingThis.class);
		classFiles.add(SecondLayerCalling.class);
//		classFiles.add(AnnotationConfigApplicationContext.class);
		for (int i = 0; i < classFiles.size(); i++) {
			Class curr = classFiles.get(i);
			String classFileName = curr.getCanonicalName().replace('.', '/')
					+ ".class";
			URL classFile = getClass().getResource("/" + classFileName);
			assertNotNull(classFile);
			File copiedFile = new File(testDir, classFileName);
			if (i == 0) {
				assertTrue(copiedFile.getParentFile().mkdirs());
			}
			FileOutputStream out = new FileOutputStream(copiedFile);
			InputStream in = classFile.openStream();
			byte[] buffer = new byte[1024];
			for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
				out.write(buffer, 0, read);
			}
			in.close();
			out.close();
		}

	}

	protected void copyTestJarFile() throws Exception {
		String classFileName = Trial.class.getCanonicalName().replace('.', '/')
				+ ".class";
		URL classFile = getClass().getResource("/" + classFileName);
		assertNotNull(classFile);

		Manifest manifest = new Manifest();
		Attributes attributes = manifest.getMainAttributes();
		attributes.putValue("Manifest-Version", "1.0");
		attributes.putValue("Main-Class", HelloWorld.class.getCanonicalName());

		FileOutputStream fos = new FileOutputStream(new File(testDir, TEST_JAR));
		JarOutputStream jar = new JarOutputStream(fos, manifest);
		JarEntry entry = new JarEntry(classFileName);
		jar.putNextEntry(entry);
		InputStream in = classFile.openStream();
		byte[] buffer = new byte[1024];
		for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
			jar.write(buffer, 0, read);
		}
		in.close();
		jar.closeEntry();
		jar.close();
	}

	@Before
	public void setUp() {
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
		System.setProperty("javax.net.debug", "all");
		Utils.initializeDirectory(TEST_DIR);
		testDir = new File(TEST_DIR);
	}

	@Test
	public void testClassExecution() throws Exception {
		System.out.println(System.getProperty("java.class.path"));
		copyTestClassFile();
		// copyCallingClassFile();
		
		JavaProcess process = new JavaProcessImpl(processFactory.create());
		
		process.setVmOptions(new String[]{"-cp",System.getProperty("java.class.path")} );
		
		process.setArguments(new String[] { "-echo", "foo", "bar", "baz" });
		process.setJavaClass(Trial.class.getCanonicalName());
		assertNull("No jar file set", process.getJarFile());
		process.setDirectory(testDir);
		process.setJavaExecutable(javaBin);
		process.start();
		assertEquals("Process exited normally", 0, process.waitFor());
		assertTrue("Process not active", !process.isActive());
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				process.getStdOut()));
		String line = reader.readLine();
		while (line != null) {
			System.out.println("line: " + line);
			line = reader.readLine();
		}

	}

//	private JavaProcess getJavaProcess() {
//		JavaProcess process = new JavaProcessImpl(processFactory.create());
//		process.setArguments(new String[] { "-echo", "foo", "bar", "baz" });
//		process.setJavaClass(Trial.class.getCanonicalName());
//		assertNull("No jar file set", process.getJarFile());
//		process.setDirectory(testDir);
//		process.setJavaExecutable(javaBin);
//		return process;
//	}
//
//	private void startAndAssertProcessBehavior(JavaProcess process)
//			throws IOException, InterruptedException {
//		process.start();
//		assertEquals("Process exited normally", 0, process.waitFor());
//		assertTrue("Process not active", !process.isActive());
//	}
//	
//    public void testSerialMultipleClassExecution() throws Exception {
//        copyTestClassFile();
//        
//    	final List<JavaProcess> processes = new ArrayList<JavaProcess>();
//
//    	for (int i = 0; i < numberOfProcesses; i++) {
//    		processes.add(getJavaProcess());
//    	}
//        
//    	for (JavaProcess process: processes) {
//            startAndAssertProcessBehavior(process);
//    	}
//        
//    }
}
