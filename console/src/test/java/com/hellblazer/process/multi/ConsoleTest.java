package com.hellblazer.process.multi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartfrog.services.anubis.partition.test.controller.gui.GraphicController;
import org.smartfrog.services.anubis.partition.test.controller.gui.TestControllerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.hellblazer.jackal.testUtil.TestController;
import com.hellblazer.jackal.testUtil.gossip.GossipTestCfg;
import com.hellblazer.process.CallingThis;
import com.hellblazer.process.HelloWorld;
import com.hellblazer.process.JavaProcess;
import com.hellblazer.process.ProcessTest;
import com.hellblazer.process.SecondLayerCalling;
import com.hellblazer.process.Utils;
import com.hellblazer.process.impl.JavaProcessImpl;
import com.hellblazer.process.impl.ManagedProcessFactoryImpl;

import static junit.framework.Assert.*;

public class ConsoleTest extends ProcessTest {

	protected static final String TEST_DIR = "test-dirs/java-process-test";
	protected static final String TEST_JAR = "test.jar";
	MBeanServerConnection connection;
	JMXConnector connector;
	ManagedProcessFactoryImpl processFactory = new ManagedProcessFactoryImpl();
	protected File testDir;

	final int numberOfProcesses = 20;

	static {
		GossipTestCfg.setTestPorts(24730, 24750);
	}

	private AnnotationConfigApplicationContext controllerContext;
	private GraphicController controller;
	static final String PROCESS_IDEN = "process.iden";

	protected void copyTestClassFile() throws Exception {

		ArrayList<Class> classFiles = new ArrayList<Class>();
		classFiles.add(ChildProcess.class);
		classFiles.add(CallingThis.class);
		classFiles.add(SecondLayerCalling.class);
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
		String classFileName = ChildProcess.class.getCanonicalName().replace('.', '/')
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

	protected Class<?>[] getConfigs() {
		return new Class<?>[] { member1.class, member2.class, member.class,
				member.class, member.class, member.class, member.class,
				member.class, member.class, member.class, member.class,
				member.class, member.class, member.class, member.class,
				member.class, member.class, member.class, member.class,
				member.class };
	}

	protected Class<?> getControllerConfig() {
		return TestControllerConfig.class;
	}

	/**
	 * Set up a test method that spawns one process that contains only member1
	 * node.
	 * 
	 * @see You might have to go into your system monitor and manually kill the
	 *      process that is running this process.
	 * @throws Exception
	 */
	@Test
	public void testBasic() throws Exception {

		controllerContext = new AnnotationConfigApplicationContext(
				getControllerConfig());
		controller = controllerContext.getBean(GraphicController.class);
		assertNotNull(controller);

		copyTestClassFile();

		String className = "";

		for (Class<?> config : getConfigs()) {
			className = config.toString().replaceAll("class ", "");
			System.out.println("config: " + className);
			//If its member1.class, then it spawns the process
			if (className.equals("com.hellblazer.process.multi.member1")) {
				JavaProcess process = new JavaProcessImpl(
						processFactory.create());
				process.setVmOptions(new String[] { "-cp",
						System.getProperty("java.class.path"),
						"-D" + PROCESS_IDEN + "=" + 1 });
				process.setArguments(new String[] { className });
				process.setJavaClass(ChildProcess.class.getCanonicalName());
				assertNull("No jar file set", process.getJarFile());
				process.setDirectory(testDir);
				process.setJavaExecutable(javaBin);
				process.start();
				assertEquals("Process exited normally", 0, process.waitFor());
				assertTrue("Process not active", !process.isActive());

			}

		}
		Thread.sleep(500000);
	}



	// Try and create all 2 members on separate processes
	@Test
	public void testClassExecution() throws Exception {

		 controllerContext = new AnnotationConfigApplicationContext(
		 getControllerConfig());
		 controller = controllerContext.getBean(GraphicController.class);
		 assertNotNull(controller);
		
		 copyTestClassFile();
		 // copyCallingClassFile();
		 System.out.println("comes here holy crap");
		 JavaProcess process = new JavaProcessImpl(processFactory.create());
		
		 process.setVmOptions(new String[] { "-cp",
		 System.getProperty("java.class.path"),
		 "-D" + PROCESS_IDEN + "=" + 1 });
		
		 // System.out.println(System.getProperty(PROCESS_IDEN));
		
		 String[] args = new String[20];
		
		 int i = 0;
		 for (Class<?> config : getConfigs()) {
		 System.out.println("class:" + config.toString());
		 args[i] = config.toString().replaceFirst("class ", "");
		 // System.out.println(args[i]);
		
		 }
		
		 process.setArguments(new String[] {"com.hellblazer.process.multi.member1"});
		 process.setJavaClass(ChildProcess.class.getCanonicalName());
		 assertNull("No jar file set", process.getJarFile());
		 process.setDirectory(testDir);
		 process.setJavaExecutable(javaBin);
		 process.start();
		
		 System.out.println("comes here holy crap");
		 JavaProcess process1 = new JavaProcessImpl(processFactory.create());
		 process1.setVmOptions(new String[] { "-cp",
		 System.getProperty("java.class.path"),
		 "-D" + PROCESS_IDEN + "=" + 2 });
		 process1.setArguments(new String[] {"com.hellblazer.process.multi.member2"});
		 process1.setJavaClass(Trial1.class.getCanonicalName());
		 assertNull("No jar file set", process1.getJarFile());
		 process1.setDirectory(testDir);
		 process1.setJavaExecutable(javaBin);
		 process1.start();
		 // ////
		 BufferedReader reader = new BufferedReader(new InputStreamReader(
		 process.getStdOut()));
		 String line = reader.readLine();
		 while (line != null) {
		 System.out.println("line: " + line);
		 line = reader.readLine();
		
		 }
		
		 assertEquals("Process exited normally", 0, process1.waitFor());
		 assertTrue("Process not active", !process1.isActive());
		
		 assertEquals("Process exited normally", 0, process.waitFor());
		 assertTrue("Process not active", !process.isActive());
		
		 Thread.sleep(500000);

		// set up the basics of the gui to show the nodes interacting with each
		// other
	}

	public void testPrint() throws Exception {
		controllerContext = new AnnotationConfigApplicationContext(
				getControllerConfig());
		controller = controllerContext.getBean(GraphicController.class);
		assertNotNull(controller);
		int i = 0;
		copyTestClassFile();
		String className = "";
		for (Class<?> config : getConfigs()) {
			className = config.toString().replace("class ", "");
			JavaProcess process = new JavaProcessImpl(processFactory.create());

			process.setVmOptions(new String[] { "-cp",
					System.getProperty("java.class.path"),
					"-D" + PROCESS_IDEN + "=" + i });
			process.setArguments(new String[] { className });
			process.setJavaClass(CheckBasic.class.getCanonicalName());
			assertNull("No jar file set", process.getJarFile());
			process.setDirectory(testDir);
			process.setJavaExecutable(javaBin);
			process.start();
			// assertEquals("Process exited normally", 0, process.waitFor());
			// assertTrue("Process not active", !process.isActive());

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getStdOut()));
			String line = reader.readLine();
			while (line != null) {
				System.out.println("line: " + line);
				line = reader.readLine();
			}
			i++;
		}
	}
	@Test
	public void testMultiProcesses() throws Exception
	{
		controllerContext = new AnnotationConfigApplicationContext(
				getControllerConfig());
		controller = controllerContext.getBean(GraphicController.class);
		assertNotNull(controller);

		copyTestClassFile();
		// copyCallingClassFile();
		System.out.println("comes here holy crap");

		// System.out.println(System.getProperty(PROCESS_IDEN));

		String[] args = new String[20];
		String className = "";
		int i = 0;
		for (Class<?> config : getConfigs()) {
			System.out.println("class:" + config.toString());
			className = config.toString().replace("class ", "");

			JavaProcess process = new JavaProcessImpl(processFactory.create());
			process.setVmOptions(new String[] { "-cp",
					System.getProperty("java.class.path"),
					"-D" + PROCESS_IDEN + "=" + i });
			process.setArguments(new String[] { className });
			process.setJavaClass(CheckBasic.class.getCanonicalName());
			assertNull("No jar file set", process.getJarFile());
			process.setDirectory(testDir);
			process.setJavaExecutable(javaBin);
			process.start();
			i++;

		}
		Thread.sleep(500000);

	}
	
}
