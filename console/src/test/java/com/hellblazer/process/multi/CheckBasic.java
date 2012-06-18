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
package com.hellblazer.process.multi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.smartfrog.services.anubis.partition.test.controller.gui.GraphicController;
import org.smartfrog.services.anubis.partition.test.controller.gui.TestControllerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sun.management.ConnectorAddressLink;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode1Cfg;
import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode2Cfg;
import com.hellblazer.jackal.testUtil.gossip.GossipNodeCfg;
import com.hellblazer.jackal.testUtil.gossip.GossipTestCfg;
import com.hellblazer.process.ProcessTest;
import com.sun.jmx.remote.internal.RMIExporter;

/**
 * @author Hal Hildebrand
 * 
 */
public class CheckBasic extends ProcessTest {
	private static AnnotationConfigApplicationContext controllerContext;
	private static GraphicController controller;

	protected static Class<?> getControllerConfig() {
		return TestControllerConfig.class;
	}
	
	
	

	private static final AtomicInteger id = new AtomicInteger(-1);

	static {
		GossipTestCfg.setTestPorts(24730, 24750);
	}

	
    public static void main(String[] argv) throws Exception {
    	System.out.println("this is working");
    	System.out.println("This is the class: "+argv[0]);
    	assertTrue(3==3);
    	Class<?> config = Class.forName(argv[0]);
		System.out.println("config: "+config.toString());
    	new AnnotationConfigApplicationContext(config);
    	
    	Thread.sleep(500000);

    }

   

}
