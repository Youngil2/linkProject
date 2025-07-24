package com.practice.kyi.common.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.practice.kyi.common.dao.vo.ChatVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OpenAIService {
	
    @Value("${openai.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.openai.com/v1/chat/completions";
    
    public OpenAIService() {
        this.restTemplate = new RestTemplate();
    }
    
    public String getChatResponse(ChatVO chatVO) {
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatVO.getModel() != null ? chatVO.getModel() : "gpt-3.5-turbo");
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "user", "content", chatVO.getMessage())
            ));
            requestBody.put("max_tokens", chatVO.getMaxTokens() > 0 ? chatVO.getMaxTokens() : 500);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("choices")) {
                    java.util.List<Map<String, Object>> choices = 
                        (java.util.List<Map<String, Object>>) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        return (String) message.get("content");
                    }
                }
            }
            
            return "응답을 받을 수 없습니다.";
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 오류: ", e);
            return "죄송합니다. 응답을 생성할 수 없습니다.";
        }
    }
}
