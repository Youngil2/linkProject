package com.practice.kyi.common.contorller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.common.dao.vo.ChatVO;
import com.practice.kyi.common.service.OpenAIService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class ChatController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
	
	@Autowired
	OpenAIService openAIService;
	
	@PostMapping("/chat")
	@ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, String> request, HttpServletRequest req) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String message = request.get("message");
            
    	    HttpSession session = req.getSession();
    	    String currentUserId = (String) session.getAttribute("memberId");
            
            // ChatVO 생성
            ChatVO chatVO = new ChatVO();
            chatVO.setMessage(message);
            chatVO.setSessionId(currentUserId);
            chatVO.setMessageType("USER");
            chatVO.setModel("gpt-3.5-turbo");
            chatVO.setMaxTokens(500);
            
            // OpenAI API 호출
            String response = openAIService.getChatResponse(chatVO);
            
            // 응답 ChatVO 생성
            ChatVO responseVO = new ChatVO();
            responseVO.setMessage(message);
            responseVO.setResponse(response);
            responseVO.setSessionId(currentUserId);
            responseVO.setMessageType("BOT");

            
            result.put("success", true);
            result.put("response", response);
            result.put("message", "성공");
            
        } catch (Exception e) {
            logger.error("채팅 처리 오류: ", e);
            result.put("success", false);
            result.put("response", "죄송합니다. 오류가 발생했습니다.");
            result.put("message", "오류 발생");
        }
        
        return result;
    }
}
