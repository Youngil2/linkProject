package com.practice.kyi.admin_link.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("admin")
public class ApiTestController {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiTestController.class);

	/**
	 * 서비스 키 처리 개선 - 공공데이터포털 특화
	 */
	private String processServiceKey(String serviceKey) {
	    try {
	        logger.info("원본 서비스키 길이: {}, 내용: {}", serviceKey.length(), serviceKey);
	        
	        // 1. 공백 제거
	        String trimmedKey = serviceKey.trim();
	        
	        // 2. 이미 인코딩된 키인지 확인 (%가 포함되어 있으면 인코딩된 것으로 간주)
	        if (trimmedKey.contains("%")) {
	            // 이미 인코딩된 경우, 디코딩 후 재인코딩
	            try {
	                String decoded = URLDecoder.decode(trimmedKey, StandardCharsets.UTF_8);
	                String reEncoded = URLEncoder.encode(decoded, StandardCharsets.UTF_8);
	                logger.info("재인코딩된 서비스키: {}", reEncoded);
	                return reEncoded;
	            } catch (Exception e) {
	                logger.warn("서비스키 디코딩 실패, 원본 사용: {}", e.getMessage());
	                return trimmedKey;
	            }
	        } else {
	            // 인코딩되지 않은 경우, 인코딩 적용
	            String encoded = URLEncoder.encode(trimmedKey, StandardCharsets.UTF_8);
	            logger.info("새로 인코딩된 서비스키: {}", encoded);
	            return encoded;
	        }
	        
	    } catch (Exception e) {
	        logger.error("서비스키 처리 중 오류: {}", e.getMessage(), e);
	        return serviceKey; // 오류 시 원본 반환
	    }
	}

	/**
	 * 테스트용 URL 생성 - 디버깅 강화 버전 (수정됨)
	 */
	private String buildTestUrl(String baseUrl, String serviceKey, List<Map<String, String>> parameters) {
	    try {
	        StringBuilder urlBuilder = new StringBuilder();
	        urlBuilder.append(baseUrl.trim());
	        
	        // 서비스키 처리
	        String processedServiceKey = processServiceKey(serviceKey).trim();
	        
	        // URL에 쿼리 파라미터 추가
	        char separator = baseUrl.contains("?") ? '&' : '?';
	        urlBuilder.append(separator).append("serviceKey=").append(processedServiceKey);
	        
	        // 추가 파라미터 처리
	        if (parameters != null && !parameters.isEmpty()) {
	            for (Map<String, String> param : parameters) {
	                String paramName = param.get("paramName");
	                String paramValue = param.get("paramValue");
	                
	                if (paramName != null && !paramName.trim().isEmpty()) {
	                    urlBuilder.append("&").append(URLEncoder.encode(paramName.trim(), StandardCharsets.UTF_8));
	                    
	                    if (paramValue != null && !paramValue.trim().isEmpty()) {
	                        urlBuilder.append("=").append(URLEncoder.encode(paramValue.trim(), StandardCharsets.UTF_8));
	                    } else {
	                        urlBuilder.append("=");
	                    }
	                }
	            }
	        }
	        
	        // 잘못된 부분 제거: RestTemplate 호출을 제거하고 URL 문자열만 반환
	        String finalUrl = urlBuilder.toString();
	        
	        logger.info("=== URL 생성 디버깅 정보 ===");
	        logger.info("원본 baseUrl: {}", baseUrl);
	        logger.info("원본 serviceKey: {}", serviceKey);
	        logger.info("처리된 serviceKey: {}", processedServiceKey);
	        logger.info("최종 URL: {}", finalUrl);
	        logger.info("URL 길이: {}", finalUrl.length());
	        
	        return finalUrl;
	        
	    } catch (Exception e) {
	        logger.error("URL 생성 중 오류 발생", e);
	        throw new RuntimeException("URL 생성 실패: " + e.getMessage(), e);
	    }
	}

	/**
	 * API 연결 테스트 - 응답 분석 강화
	 */
	@PostMapping("/test-connection")
	@ResponseBody
	public Map<String, Object> testApiConnection(@RequestBody Map<String, Object> request) {
	    Map<String, Object> result = new HashMap<>();
	    
	    try {
	        // 입력값 검증
	        String baseUrl = (String) request.get("baseUrl");
	        String serviceKey = (String) request.get("serviceKey");
	        
	        logger.info("=== API 연결 테스트 시작 ===");
	        logger.info("baseUrl: {}", baseUrl);
	        logger.info("serviceKey 길이: {}", serviceKey != null ? serviceKey.length() : "null");
	        
	        if (baseUrl == null || baseUrl.trim().isEmpty()) {
	            result.put("success", false);
	            result.put("message", "기본 URL이 필요합니다.");
	            return result;
	        }
	        
	        if (serviceKey == null || serviceKey.trim().isEmpty()) {
	            result.put("success", false);
	            result.put("message", "서비스 키가 필요합니다.");
	            return result;
	        }
	        
	        @SuppressWarnings("unchecked")
	        List<Map<String, String>> parameters = (List<Map<String, String>>) request.get("parameters");
	        
	        // URL 조립
	        String testUrl = buildTestUrl(baseUrl, serviceKey, parameters);
	        logger.info("테스트 URL: {}", testUrl);
	        
	        // HttpURLConnection으로 API 호출
	        URL url = new URL(testUrl);
	        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
	        
	        try {
	            // 요청 설정
	            urlConn.setRequestMethod("GET");
	            urlConn.setConnectTimeout(10000);
	            urlConn.setReadTimeout(30000);
	            urlConn.setDoOutput(false); // GET 요청이므로 false
	            
	            // 헤더 설정
	            urlConn.setRequestProperty("User-Agent", "Java/" + System.getProperty("java.version"));
	            urlConn.setRequestProperty("Accept", "*/*");
	            
	            logger.info("=== 전송할 헤더 정보 ===");
	            logger.info("User-Agent: {}", urlConn.getRequestProperty("User-Agent"));
	            logger.info("Accept: {}", urlConn.getRequestProperty("Accept"));
	            
	            // 연결 및 응답 코드 확인
	            int responseCode = urlConn.getResponseCode();
	            logger.info("응답 코드: {}", responseCode);
	            
	            // 응답 본문 읽기
	            StringBuilder responseBody = new StringBuilder();
	            
	            try (BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(
	                        responseCode >= 200 && responseCode < 300 
	                            ? urlConn.getInputStream() 
	                            : urlConn.getErrorStream(), 
	                        StandardCharsets.UTF_8))) {
	                
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    responseBody.append(line).append('\n');
	                }
	            }
	            
	            String response = responseBody.toString();
	            
	            logger.info("=== API 응답 정보 ===");
	            logger.info("응답 상태: {}", responseCode);
	            logger.info("응답 본문 길이: {}", response.length());
	            logger.info("응답 본문 시작 부분: {}", 
	                response.length() > 200 ? response.substring(0, 200) + "..." : response);
	            
	            // 응답 분석 및 결과 처리
	            if (responseCode >= 200 && responseCode < 300) {
	                result.put("success", true);
	                result.put("statusCode", responseCode);
	                
	                // 응답 헤더 정보 추가
	                Map<String, String> responseHeaders = new HashMap<>();
	                urlConn.getHeaderFields().forEach((key, values) -> {
	                    if (key != null && !values.isEmpty()) {
	                        responseHeaders.put(key, String.join(", ", values));
	                    }
	                });
	                result.put("responseHeaders", responseHeaders);
	                
	                if (response != null && !response.trim().isEmpty()) {
	                    // 다양한 에러 패턴 체크
	                    if (response.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR") || 
	                        response.contains("SERVICE ERROR") ||
	                        response.contains("INVALID_REQUEST_PARAMETER_ERROR") ||
	                        response.contains("서비스키가 유효하지 않습니다") ||
	                        response.contains("등록되지 않은 서비스키") ||
	                        response.contains("SERVICE_KEY_IS_NOT_REGISTERED")) {
	                        
	                        result.put("success", false);
	                        result.put("message", "서비스 키 오류: 등록되지 않은 서비스 키이거나 권한이 없습니다.");
	                        result.put("errorCode", "SERVICE_KEY_ERROR");
	                        
	                        // 상세 에러 정보 추가
	                        if (response.length() > 500) {
	                            result.put("errorDetails", response.substring(0, 500) + "...");
	                        } else {
	                            result.put("errorDetails", response);
	                        }
	                    } else {
	                        result.put("message", "API 연결 테스트 성공");
	                        if (response.length() > 1000) {
	                            result.put("response", response.substring(0, 1000) + "... (응답 생략)");
	                        } else {
	                            result.put("response", response);
	                        }
	                    }
	                } else {
	                    result.put("message", "API 연결 성공했지만 응답 본문이 비어있습니다.");
	                }
	            } else {
	                result.put("success", false);
	                result.put("message", "API 응답 오류: HTTP " + responseCode);
	                result.put("statusCode", responseCode);
	                
	                // 에러 응답 본문도 포함
	                if (response != null && !response.trim().isEmpty()) {
	                    result.put("errorResponse", response.length() > 500 
	                        ? response.substring(0, 500) + "..." 
	                        : response);
	                }
	            }
	            
	        } catch (IOException e) {
	            logger.error("API 연결 중 IOException 발생: {}", e.getMessage());
	            result.put("success", false);
	            result.put("message", "API 연결 오류: " + e.getMessage());
	            
	        } catch (Exception e) {
	            logger.error("API 연결 중 예상치 못한 오류: {}", e.getMessage(), e);
	            result.put("success", false);
	            result.put("message", "API 연결 테스트 중 오류 발생: " + e.getMessage());
	            
	        } finally {
	            // 연결 해제
	            if (urlConn != null) {
	                urlConn.disconnect();
	            }
	        }
	        
	    } catch (Exception e) {
	        logger.error("API 연결 테스트 중 예상치 못한 오류", e);
	        result.put("success", false);
	        result.put("message", "API 연결 테스트 중 오류 발생: " + e.getMessage());
	    }
	    
	    logger.info("=== API 연결 테스트 완료 ===");
	    return result;
	}
}