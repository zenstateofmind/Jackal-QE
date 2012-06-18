package com.hellblazer.process.multi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hellblazer.jackal.testUtil.gossip.GossipNodeCfg;

@Configuration class member extends GossipNodeCfg {
	
	@Override
	@Bean
	public int node() {
//		System.out.println("comes into member");
		return Integer.parseInt(System.getProperty(ConsoleTest.PROCESS_IDEN));
	}
}