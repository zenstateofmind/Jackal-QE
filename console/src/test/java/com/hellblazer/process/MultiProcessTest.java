package com.hellblazer.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.junit.Before;
import org.junit.Test;

import com.hellblazer.process.JavaProcess;
import com.hellblazer.process.impl.JavaProcessImpl;
import com.hellblazer.process.impl.ManagedProcessFactoryImpl;

public class MultiProcessTest extends ProcessTest {

	protected static final String TEST_DIR       = "test-dirs/multi-process-test";
    protected static final String TEST_JAR       = "test.jar";
    MBeanServerConnection         connection;
	JMXConnector connector;
    ManagedProcessFactoryImpl     processFactory = new ManagedProcessFactoryImpl();
    protected File                testDir;

	final int numberOfProcesses = 20;
	
	@Before
    public void setUp() {
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        System.setProperty("javax.net.debug", "all");
        Utils.initializeDirectory(TEST_DIR);
        testDir = new File(TEST_DIR);
    }

    protected void copyTestClassFile() throws Exception {
        String classFileName = HelloWorld.class.getCanonicalName().replace('.','/') + ".class";
        URL classFile = getClass().getResource("/" + classFileName);
        assertNotNull(classFile);
        File copiedFile = new File(testDir, classFileName);
        assertTrue(copiedFile.getParentFile().mkdirs());
        FileOutputStream out = new FileOutputStream(copiedFile);
        InputStream in = classFile.openStream();
        byte[] buffer = new byte[1024];
        for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.close();
    }
    
    private JavaProcess getJavaProcess() {
    	JavaProcess process = new JavaProcessImpl(processFactory.create());
        process.setArguments(new String[] { "-echo", "foo", "bar", "baz" });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        assertNull("No jar file set", process.getJarFile());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        return process;
    }

	private void startAndAssertProcessBehavior(JavaProcess process) throws IOException, InterruptedException {
		process.start();
        assertEquals("Process exited normally", 0, process.waitFor());
        assertTrue("Process not active", !process.isActive());
        BufferedReader reader = new BufferedReader( new InputStreamReader( process.getStdOut()));

        String line;

        try {
            line = reader.readLine();

            assertEquals("foo", line);
            line = reader.readLine();
            assertEquals("bar", line);
            line = reader.readLine();
            assertEquals("baz", line);
            line = reader.readLine();

            assertNull(line);
        } finally {
            reader.close();
        }

        try {
            reader = new BufferedReader( new InputStreamReader( process.getStdErr()));
            line = reader.readLine();
            assertEquals("foo", line);
            line = reader.readLine();
            assertEquals("bar", line);
            line = reader.readLine();
            assertEquals("baz", line);
            line = reader.readLine();
            assertNull(line);
        } finally {
            reader.close();
        }
	}
    
    /**
     * Spawn multiple instances of HelloWorld.class and have them run serially.
     * @throws Exception
     */
	@Test
    public void testSerialMultipleClassExecution() throws Exception {
        copyTestClassFile();
        
    	final List<JavaProcess> processes = new ArrayList<JavaProcess>();

    	for (int i = 0; i < numberOfProcesses; i++) {
    		processes.add(getJavaProcess());
    	}
        
    	for (JavaProcess process: processes) {
            startAndAssertProcessBehavior(process);
    	}
        
    }
    
    /**
     * Spawn multiple instances of HelloWorld.class and have them run concurrently
     * @throws Exception
     */
	@Test
    public void testConcurrentMultipleClassExecution() throws Exception {
    	copyTestClassFile();
    	
    	final CyclicBarrier barrier = new CyclicBarrier(numberOfProcesses);
    	final CountDownLatch latch = new CountDownLatch(numberOfProcesses);
    	final Exception[] exceptions = new Exception[numberOfProcesses];
    	
    	final List<JavaProcess> processes = new ArrayList<JavaProcess>();
    	
    	for (int i = 0; i < numberOfProcesses; i++) {
    		final int count = i;
    		Runnable runnable = new Runnable() {
    			@Override
    			public void run() {
    				try {
    					// setup Process
    					JavaProcess process = getJavaProcess();
    					processes.add(process);
    					
    					barrier.await();
    					
    					// begin process
    					startAndAssertProcessBehavior(process);
    					
    				} catch (InterruptedException ignore) {
    					
    				} catch (Exception e) {
    					exceptions[count] = e;
    				} finally {
    					latch.countDown();
    				}
    			}
    		};
    		new Thread(runnable).start();
    	}
    	assertTrue("Threads did not synch up in 30 seconds", latch.await(30, TimeUnit.SECONDS));
    	
    	for (Exception e: exceptions) {
    		if (e!= null) {
    			throw e;
    		}
    	}
    }
    /**
     * Spawn multiple instances of HelloWorld.class and have them run concurrently. Assert messages can be passed between processes.
     * @throws Exception
     */
	@Test
    public void testAsynchStablizationAndCommunication() throws Exception {
	}
}
