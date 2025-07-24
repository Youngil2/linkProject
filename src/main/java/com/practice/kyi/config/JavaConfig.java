package com.practice.kyi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import egovframework.rte.fdl.cmmn.trace.LeaveaTrace;
import egovframework.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import egovframework.rte.fdl.cmmn.trace.handler.TraceHandler;
import egovframework.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import egovframework.rte.fdl.cmmn.trace.manager.TraceHandlerService;

@Configuration
public class JavaConfig {

	@Bean
    LeaveaTrace leaveaTrace(DefaultTraceHandleManager traceHandlerService) {
    	LeaveaTrace leaveaTrace = new LeaveaTrace();
    	leaveaTrace.setTraceHandlerServices(new TraceHandlerService[] {traceHandlerService});
    	return leaveaTrace;
    }
	
	 @Bean
	 DefaultTraceHandleManager traceHandlerService(AntPathMatcher antPathMatcher, DefaultTraceHandler defaultTraceHandler) {
		 DefaultTraceHandleManager defaultTraceHandleManager = new DefaultTraceHandleManager();
		 defaultTraceHandleManager.setReqExpMatcher(antPathMatcher);
	     defaultTraceHandleManager.setPatterns(new String[]{"*"});
	     defaultTraceHandleManager.setHandlers(new TraceHandler[] {defaultTraceHandler});
	     return defaultTraceHandleManager;
	     }
	 @Bean
	 AntPathMatcher antPathMatcher() {
	    return new AntPathMatcher();
	    }
	 @Bean
	 DefaultTraceHandler defaultTraceHandler() {
		 return new DefaultTraceHandler();
		 }
}
