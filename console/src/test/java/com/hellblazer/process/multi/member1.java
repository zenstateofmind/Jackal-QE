package com.hellblazer.process.multi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hellblazer.jackal.testUtil.gossip.GossipDiscoveryNode1Cfg;

@Configuration class member1 extends GossipDiscoveryNode1Cfg {
	@Override
	@Bean
	public int node() {
		System.out.println("it comes into member1");
		System.out.println("ID of member1 is: "+(Integer.parseInt(System.getProperty(ConsoleTest.PROCESS_IDEN)+4)));
		return Integer.parseInt(System.getProperty(ConsoleTest.PROCESS_IDEN));
	}
}