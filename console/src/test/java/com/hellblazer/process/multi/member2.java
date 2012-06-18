package com.hellblazer.process.multi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode2Cfg;

@Configuration class member2 extends GossipDiscoveryNode2Cfg {
	@Override
	@Bean
	public int node() {
		System.out.println("comes into member2");
		return Integer.parseInt(System.getProperty(ConsoleTest.PROCESS_IDEN));
	}
}