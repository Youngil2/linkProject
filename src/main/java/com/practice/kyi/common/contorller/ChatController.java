package com.practice.kyi.common.contorller;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.common.dao.vo.ChatVO;
import com.practice.kyi.common.service.ChatResponseService;
import com.practice.kyi.common.service.OpenAIService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class ChatController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
	
	@Autowired
	OpenAIService openAIService;
	
    @Autowired
    ChatResponseService chatResponseService;
	
    /**
     * 채팅 요청 처리 - UTF-8 인코딩 지원
     */
    @PostMapping(value = "/chat", 
                 produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
                 consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, String> request, 
            HttpServletRequest req,
            HttpServletResponse response) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 응답 헤더에 UTF-8 인코딩 설정
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try {
            String message = request.get("message");
            
            // 메시지 입력 검증
            if (message == null || message.trim().isEmpty()) {
                result.put("success", false);
                result.put("response", "메시지를 입력해주세요.");
                result.put("message", "입력값 오류");
                return ResponseEntity.badRequest().body(result);
            }
            
            // UTF-8 인코딩 안전 처리
            message = ensureUTF8(message.trim());
            logger.info("수신된 메시지: {}", message);

            HttpSession session = req.getSession();
            String currentUserId = (String) session.getAttribute("memberId");
            
            // 세션이 없는 경우 임시 ID 생성
            if (currentUserId == null) {
                currentUserId = "anonymous_" + System.currentTimeMillis();
                session.setAttribute("memberId", currentUserId);
                logger.info("임시 사용자 ID 생성: {}", currentUserId);
            }

            // ChatVO 생성
            ChatVO chatVO = new ChatVO();
            chatVO.setMessage(message);
            chatVO.setSessionId(currentUserId);
            chatVO.setMessageType("USER");
            chatVO.setModel("gpt-3.5-turbo");
            chatVO.setMaxTokens(500);

            String botResponse;
            String responseSource = ""; // 응답 출처 추적용
            
            // 1. 먼저 사전 정의된 기본 응답 확인 (인사말, 감사 등)
            String predefinedResponse = chatResponseService.getPredefinedResponse(message);
            
            if (predefinedResponse != null) {
                botResponse = predefinedResponse;
                responseSource = "PREDEFINED";
                logger.info("사전 정의된 응답 사용: {}", message);
            } else {
                // 2. FAQ 데이터베이스에서 응답 찾기
                String faqResponse = chatResponseService.getFaqResponse(message);
                
                if (faqResponse != null) {
                    botResponse = faqResponse;
                    responseSource = "FAQ";
                    logger.info("FAQ 응답 사용: {}", message);
                } else {
                    // 3. 키워드 기반 기본 응답 확인
                    String keywordResponse = chatResponseService.getKeywordBasedResponse(message);
                    
                    if (keywordResponse != null) {
                        botResponse = keywordResponse;
                        responseSource = "KEYWORD";
                        logger.info("키워드 응답 사용: {}", message);
                    } else {
                        // 4. 마지막으로 OpenAI API 호출
                        try {
                            botResponse = openAIService.getChatResponse(chatVO);
                            responseSource = "OPENAI";
                            logger.info("OpenAI API 응답 사용: {}", message);
                        } catch (Exception openAiException) {
                            logger.error("OpenAI API 호출 실패: ", openAiException);
                            // OpenAI 실패시 기본 응답
                            botResponse = chatResponseService.getDefaultResponse();
                            responseSource = "DEFAULT";
                        }
                    }
                }
            }
            
            // UTF-8 안전 처리
            botResponse = ensureUTF8(botResponse);
            logger.info("최종 응답: {}", botResponse);

            // 응답 ChatVO 생성 (필요시 DB 저장용)
            ChatVO responseVO = new ChatVO();
            responseVO.setMessage(message);
            responseVO.setResponse(botResponse);
            responseVO.setSessionId(currentUserId);
            responseVO.setMessageType("BOT");

            // 결과 반환
            result.put("success", true);
            result.put("response", botResponse);
            result.put("source", responseSource); // 개발/디버그용
            result.put("message", "성공");
            result.put("userId", currentUserId); // 디버그용

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            logger.error("채팅 처리 오류: ", e);
            result.put("success", false);
            result.put("response", "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            result.put("message", "서버 오류");
            result.put("error", e.getMessage()); // 개발환경에서만 사용
            
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);
        }
    }
    
    /**
     * UTF-8 인코딩 안전 처리
     */
    private String ensureUTF8(String text) {
        if (text == null) return null;
        
        try {
            // UTF-8로 인코딩 확인 및 변환
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("UTF-8 인코딩 처리 중 오류: {}", e.getMessage());
            return text;
        }
    }

}
