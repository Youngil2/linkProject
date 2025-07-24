package com.practice.kyi.common.contorller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.common.service.MailService;
import com.practice.kyi.common.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MailController {
	
	private final MailService mailService;
	private final MemberService memberService;
	
	@GetMapping("/certify")
	public String certify(Model model, HttpServletRequest request) {
		HttpSession session = request.getSession();
		String email = (String) session.getAttribute("email");
		
		if(email == null || email.isEmpty()) {
			throw new IllegalArgumentException("로그인된 사용자의 이메일 주소가 없습니다");
		}		
		model.addAttribute("email",email);
		return "certify";
	}
	
	@PostMapping("certifyMember")
	@ResponseBody
	public String MailSend(String email, HttpServletRequest request) {
		HttpSession session = request.getSession();
		email = (String) session.getAttribute("email");
		if(email == null || email.isEmpty()) {
			throw new IllegalArgumentException("로그인 된 사용자의 이메일이 주소가 없습니다.");
		}
		int number = mailService.sendEmail(email, request);
		return String.valueOf(number);
	}
	
	@PostMapping("verify")
	public ResponseEntity<Map<String, String>> verifyCode(@RequestParam String number, HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		Map<String, String> response = new HashMap<>();
		
		if (session == null || session.getAttribute("codeNumber") == null) {
			response.put("message", "인증번호가 전송되지 않았습니다. 인증번호 요청 버튼을 클릭하세요.");
			return ResponseEntity.badRequest().body(response);
		}
		
		Integer codeNumber = (Integer) session.getAttribute("codeNumber");
		String memberId = (String) session.getAttribute("memberId");		
		
		
		if(codeNumber.equals(Integer.parseInt(number))){
			session.removeAttribute("codeNumber");
			memberService.memberCertifyUpdate(memberId);
			response.put("message", "인증되었습니다. 관리자에게 2차 인증을 받으세요.\n연락처 : 042-1111-1111");
			response.put("redirectUrl", "/login"); // 성공 시 리다이렉트할 URL 포함
		    return ResponseEntity.ok(response);
		}else {
	        response.put("message", "인증번호가 일치하지 않습니다.");
	        return ResponseEntity.badRequest().body(response);
	    }
	}
}
