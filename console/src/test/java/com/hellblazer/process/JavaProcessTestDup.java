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
import java.util.Iterator;
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

/**
 * @author Hal Hildebrand
 * 
 */
public class JavaProcessTestDup extends ProcessTest {
    protected static final String TEST_DIR       = "test-dirs/java-process-test";
    protected static final String TEST_JAR       = "test.jar";
    MBeanServerConnection         connection;
	JMXConnector connector;
    ManagedProcessFactoryImpl     processFactory = new ManagedProcessFactoryImpl();
    protected File                testDir;

    protected void copyTestClassFile() throws Exception {
        String classFileName = HelloWorld.class.getCanonicalName().replace('.',
                                                                           '/')
                               + ".class";
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

    protected void copyTestJarFile() throws Exception {
        String classFileName = HelloWorld.class.getCanonicalName().replace('.',
                                                                           '/')
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
        copyTestClassFile();
        JavaProcess process = new JavaProcessImpl(processFactory.create());
        process.setArguments(new String[] { "-echo", "foo", "bar", "baz" });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        assertNull("No jar file set", process.getJarFile());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();
        assertEquals("Process exited normally", 0, process.waitFor());
        assertTrue("Process not active", !process.isActive());
        BufferedReader reader = new BufferedReader(
                                                   new InputStreamReader(
                                                                         process.getStdOut()));

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
            reader = new BufferedReader(
                                        new InputStreamReader(
                                                              process.getStdErr()));
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

    @Test
    public void testExitValue() throws Exception {
        copyTestClassFile();
        JavaProcess process = new JavaProcessImpl(processFactory.create());
        process.setArguments(new String[] { "-errno", "66" });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();
        assertEquals("Process exited abnormally", 66, process.waitFor());
        assertTrue("Process not active", !process.isActive());
    }

    @Test
    public void testJarExecution() throws Exception {
        copyTestJarFile();
        JavaProcess process = new JavaProcessImpl(processFactory.create());
        process.setArguments(new String[] { "-echo", "foo", "bar", "baz" });
        process.setJarFile(new File(testDir, TEST_JAR));
        assertNull("No java class set", process.getJavaClass());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();
        assertEquals("Process exited normally", 0, process.waitFor());
        assertTrue("Process not active", !process.isActive());
        BufferedReader reader = new BufferedReader(
                                                   new InputStreamReader(
                                                                         process.getStdOut()));
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

        reader = new BufferedReader(new InputStreamReader(process.getStdErr()));
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
    }

    @Test
    public void testLocalMBeanServerConnection() throws Exception {
        copyTestClassFile();
        final JavaProcess process = new JavaProcessImpl(processFactory.create());
        int sleepTime = 60000;
        process.setArguments(new String[] { "-jmx", Integer.toString(sleepTime) });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();

        assertTrue("process is active", process.isActive()); 
        Condition condition = new Condition() {
            @Override
            public boolean isTrue() {
                try {
                	connector = process.getLocalJmxConnector();
                		
                    return true;
                } catch (NoLocalJmxConnectionException e) {
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.flush();
                    System.err.flush();
                    System.out.println("error");
                    fail("error retrieving JMX connection: " + e);
                    return false;
                }
            }

        };
        
        try {
            assertTrue("JMX connection established",
                       Utils.waitForCondition(60 * 1000, condition));

            connection = connector.getMBeanServerConnection();
            Set<ObjectName> names = connection.queryNames(null, null);
            Iterator iter = names.iterator();
            while(iter.hasNext())
            {
            	System.out.println("Name :"+iter.next().toString());
            }
            assertTrue(names.size() > 1);
            
            connector.connect();
        } finally {
            process.stop();
        }
        assertTrue("Process not active", !process.isActive());
    }

    /**
     * Verify that JMX connect is retrievable from an active process. Assert that it'a valid jmx connector.
     * @throws Exception
     */
    @Test
    public void testLocalJmxConnecor() throws Exception {
    	
        copyTestClassFile();
        final JavaProcess process = new JavaProcessImpl(processFactory.create());
        int sleepTime = 60000;
        process.setArguments(new String[] { "-jmx", Integer.toString(sleepTime) });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        String name = "crap";
//        HelloWorld.class.getMethod(name, int);
        System.out.println("constructor: "+HelloWorld.class.getConstructors().toString());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();

        assertTrue("process is active", process.isActive());

        Condition condition = new Condition() {
            @Override
            public boolean isTrue() {
                try {
                	connector = process.getLocalJmxConnector();
                		
                    return true;
                } catch (NoLocalJmxConnectionException e) {
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.flush();
                    System.err.flush();
                    System.out.println("error");
                    fail("error retrieving JMX connection: " + e);
                    return false;
                }
            }

        };
        try {
            assertTrue("JMX connection established",
                       Utils.waitForCondition(60 * 1000, condition));

            connection = connector.getMBeanServerConnection();
            Set<ObjectName> names = connection.queryNames(null, null);
            assertTrue(names.size() > 1);
            
            connector.connect();
        } finally {
            process.stop();
        }
        assertTrue("Process not active", !process.isActive());
    }

    @Test
    public void testStdIn() throws Exception {
        copyTestClassFile();
        JavaProcess process = new JavaProcessImpl(processFactory.create());
        String testLine = "hello";
        process.setArguments(new String[] { "-readln", testLine });
        process.setJavaClass(HelloWorld.class.getCanonicalName());
        process.setDirectory(testDir);
        process.setJavaExecutable(javaBin);
        process.start();
        PrintWriter writer = new PrintWriter(
                                             new OutputStreamWriter(
                                                                    process.getStdIn()));
        try {
            writer.println(testLine);
            writer.flush();
        } finally {
            writer.close();
        }

        assertEquals("Process exited normally", 0, process.waitFor());
        assertTrue("Process not active", !process.isActive());
        BufferedReader reader = new BufferedReader(
                                                   new InputStreamReader(
                                                                         process.getStdOut()));
        try {
            String line = reader.readLine();
            assertEquals(testLine, line);
        } finally {
            reader.close();
        }
    }
}
