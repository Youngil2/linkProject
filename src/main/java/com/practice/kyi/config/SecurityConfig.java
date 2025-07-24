package com.practice.kyi.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig{

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    	   http
           .authorizeHttpRequests(auth -> auth
               .requestMatchers("/", "/members/**", "/login", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/test-connection").permitAll()
               .anyRequest().authenticated()
           )
           .formLogin(login -> login
               .loginPage("/login")
               .defaultSuccessUrl("/main", true)
               .permitAll()
           )
           .exceptionHandling(ex -> ex
               .authenticationEntryPoint((request, response, authException) -> {
                   if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                       response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                   } else {
                       response.sendRedirect("/login");
                   }
               })
           )
           .logout(logout -> logout
               .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
               .invalidateHttpSession(true)
               .deleteCookies("JSESSIONID")
               .clearAuthentication(true)
               .logoutSuccessHandler((request, response, authentication) -> {
            	   response.setStatus(HttpServletResponse.SC_OK);
               })
               .permitAll()
           );
		
		return http.build();
	}
}

/*
 * http .authorizeHttpRequests(auth -> auth // 1. 정적 자원 및 공개 경로 허용
 * .requestMatchers("/", "/members/**", "/login", "/favicon.ico", "/css/**",
 * "/js/**", "/images/**").permitAll()
 * 
 * // 2. 로컬 IP만 허용 .requestMatchers(request -> { String ip =
 * request.getRemoteAddr(); return ip.equals("127.0.0.1") ||
 * ip.startsWith("192.168.") || ip.equals("0:0:0:0:0:0:0:1"); }).permitAll()
 * 
 * // 3. 나머지 요청은 모두 차단 .anyRequest().denyAll() ) .formLogin(login -> login
 * .loginPage("/login") .defaultSuccessUrl("/main", true) .permitAll() )
 * .logout(logout -> logout .logoutRequestMatcher(new
 * AntPathRequestMatcher("/logout")) .logoutSuccessUrl("/login") .permitAll() );
 */
  
