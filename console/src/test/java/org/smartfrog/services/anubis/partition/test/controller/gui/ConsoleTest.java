package org.smartfrog.services.anubis.partition.test.controller.gui;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hellblazer.jackal.testUtil.TestController;
import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode1Cfg;
import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode2Cfg;
import com.hellblazer.jackal.testUtil.gossip.GossipNodeCfg;
import com.hellblazer.jackal.testUtil.gossip.GossipTestCfg;
import static junit.framework.Assert.*;

public class ConsoleTest {
	@Configuration
	static class member extends GossipNodeCfg {

		@Override
		@Bean
		public int node() {
			return id.incrementAndGet();
		}
	}

	@Configuration
	static class member1 extends GossipDiscoveryNode1Cfg {
		@Override
		public int node() {
			return id.incrementAndGet();
		}
	}

	@Configuration
	static class member2 extends GossipDiscoveryNode2Cfg {
		@Override
		public int node() {
			return id.incrementAndGet();
		}
	}

	private static final AtomicInteger id = new AtomicInteger(-1);

	static {
		GossipTestCfg.setTestPorts(24730, 24750);
	}

	private AnnotationConfigApplicationContext controllerContext;
	private GraphicController controller;

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

	@Test
	public void testMe() throws Exception {
		controllerContext = new AnnotationConfigApplicationContext(
				getControllerConfig());
		controller = controllerContext.getBean(GraphicController.class);
		assertNotNull(controller);
		
//		ArrayList<AnnotationConfigApplicationContext> contexts = new ArrayList<AnnotationConfigApplicationContext>();
        for (Class<?> config : getConfigs()) {
        	System.out.println("class:" +config.toString());
            new AnnotationConfigApplicationContext(config);
        }
		
		Thread.sleep(500000);
	}
}
