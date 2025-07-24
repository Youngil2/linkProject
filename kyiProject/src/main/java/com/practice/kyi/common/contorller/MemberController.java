package com.practice.kyi.common.contorller;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.common.dao.vo.LoginVO;
import com.practice.kyi.common.service.MailService;
import com.practice.kyi.common.service.MemberService;
import com.practice.kyi.config.Decrypt;
import com.practice.kyi.config.SHA256Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/members")
public class MemberController {
	
	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
	
	@Autowired
	MemberService memberService;
	
	//아이디 중복체크
	@PostMapping("/idChk")
	@ResponseBody
	public int idChk(@RequestBody Map<String, String> request) {
		String memberId = request.get("memberId");		
		int idChk = memberService.selectMember(memberId);
		return idChk;
	}
	//회원가입
	@PostMapping("/join")
	@ResponseBody
	public String joinMember(LoginVO vo, HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		HttpSession session = request.getSession();
		String encryptedPw = vo.getMemberPw();
		PrivateKey privateKey = (PrivateKey) session.getAttribute("_RSA_WEB_Key_");
		String userPw = Decrypt.decryptRsa(privateKey, encryptedPw);		
		
		// Salt 생성
	    String salt = SHA256Util.getSalt();
	    String hashedPw = SHA256Util.sha256WithSaltEncode(userPw, salt);
		vo.setMemberPw(hashedPw+salt);
		memberService.registMember(vo);
		logger.info(vo.getMemberId()+"으로 회원가입");
		return "login";
	}

}
