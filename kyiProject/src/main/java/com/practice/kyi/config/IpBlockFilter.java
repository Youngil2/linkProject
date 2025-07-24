package com.practice.kyi.config;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class IpBlockFilter extends OncePerRequestFilter{
	
	private static final Logger logger = LoggerFactory.getLogger(IpBlockFilter.class);
	
    // ✅ 허용할 IP 목록만 지정 (로컬, 내부망 등)
    private static final Set<String> ALLOWED_IPS = Set.of(
        "127.0.0.1",
        "0:0:0:0:0:0:0:1",
        "192.168.0.10"     
    );

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		 	String clientIp = request.getRemoteAddr();
		 
	        if (!ALLOWED_IPS.contains(clientIp)) {
	            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied for IP: " + clientIp);
	            return;
	        }

	        filterChain.doFilter(request, response);		
	}
}
