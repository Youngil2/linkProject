package com.practice.kyi.common.contorller;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.common.dao.vo.LoginVO;
import com.practice.kyi.common.service.MemberService;
import com.practice.kyi.config.Decrypt;
import com.practice.kyi.config.SHA256Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	MemberService memberService;
	
    @Value("${server.servlet.session.timeout}")
    private Duration sessionTimeout; 
	
	@GetMapping("/login")
	public String Login(HttpServletRequest request, Model model) throws Exception {
	    HttpSession session = request.getSession(true);
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
	    generator.initialize(2048);
	    
	    KeyPair keyPair = generator.genKeyPair();
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    
	    PublicKey publicKey = keyPair.getPublic();
	    PrivateKey privateKey = keyPair.getPrivate();

	    session.setAttribute("_RSA_WEB_Key_", privateKey);

	    RSAPublicKeySpec publicSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);

	    String publicKeyModulus = publicSpec.getModulus().toString(16);
	    String publicKeyExponent = publicSpec.getPublicExponent().toString(16);

	    model.addAttribute("RSAModulus", publicKeyModulus);
	    model.addAttribute("RSAExponent", publicKeyExponent);
	    logger.info(request.getRemoteAddr()+"에서 로그인 페이지");
	    return "login";
	}
	
	@PostMapping("/members/login")
	@ResponseBody
	public Map<String, Object> loginMember(HttpServletRequest request,LoginVO vo, Model model) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		
		Map<String, Object> params = new HashMap<>();
		
		String memberId = request.getParameter("memberId");
		String memberPw = request.getParameter("memberPw");
		HttpSession session = request.getSession();
		
		
		PrivateKey privateKey = (PrivateKey) session.getAttribute("_RSA_WEB_Key_");
    	if(privateKey == null) {
    		params.put("state", false);
    	}else {
    		try {
    			String _memberId = Decrypt.decryptRsa(privateKey, memberId);
    			String _memberPw = Decrypt.decryptRsa(privateKey, memberPw);

    			int memberIdChk = memberService.selectMember(_memberId);
    			vo.setMemberId(_memberId);
    			if(memberIdChk != 0) {
    				String encryptedMemberPw = memberService.memberPwChk(_memberId);
    				String memberSaltKey = memberService.memberSaltKey(_memberId);
    				String hashMemberPw = SHA256Util.sha256WithSaltEncode(_memberPw, memberSaltKey);
    				String comparePwKey = hashMemberPw+memberSaltKey;
    				
    				String memberCertify = memberService.memberCertify(_memberId);
    				String memberApprove = memberService.memberApprove(_memberId);
    				String memberBlockYn = memberService.memberBlockkYn(_memberId); 
    				

    				LoginVO memberInfo = memberService.memberInfo(vo);
    				
    				if(!comparePwKey.equals(encryptedMemberPw)) {
    					params.put("state", "pwNon");
    				}else if(memberCertify.equals("N")) {
    					session.setAttribute("memberId", memberInfo.getMemberId());
    					session.setAttribute("email", memberInfo.getEmail());
    					params.put("state", "nonCertify");
    				}else if(memberApprove.equals("N")){
    					params.put("state", "nonApprove");
    				}else if(memberBlockYn.equals("Y")) {
    					params.put("state", "block");
    				}else {
    					session.setAttribute("memberId", memberInfo.getMemberId());
    					session.setAttribute("memberRole", memberInfo.getMemberRole());
    					session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    					long sessionTimeoutSeconds = sessionTimeout.getSeconds();
    					session.setAttribute("sessionTimeoutSeconds", sessionTimeoutSeconds);
    					List<GrantedAuthority> authorities = new ArrayList<>();
    					authorities.add(new SimpleGrantedAuthority("ROLE_" + memberInfo.getMemberRole()));
    					Authentication authentication = new UsernamePasswordAuthenticationToken(
        				        memberInfo.getMemberId(),  // principal (사용자 식별자)
        				        null,                      // credentials (비밀번호는 이미 인증되었으므로 null)
        				        authorities
        				        // 권한 목록
        				    );
        				// 시큐리티 컨텍스트에 인증 객체 저장
        				SecurityContextHolder.getContext().setAuthentication(authentication);
        				logger.info(request.getRemoteAddr()+" 에서 "+memberInfo.getMemberId()+" 으로 로그인");
        				params.put("state", true);
    				}
    			}else {
    				params.put("state","nonId");
    			}
    		}catch (Exception e) {
				logger.error("로그인 에러 발생 : loginMember " , e);
			}
    		
    		
    	}
		
		return params;
	}
	
	@GetMapping("/main")
	public String index() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		logger.debug(" user authorities: " + auth.getAuthorities());
		return "main";
	}

	
    @GetMapping("/favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // 요청을 처리하지만 아무것도 반환하지 않음.
    	// 로그인 중복 호출로 키가 지속적으로 바뀌는 현상을 막기위함
    }

}
