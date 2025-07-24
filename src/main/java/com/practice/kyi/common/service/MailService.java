package com.practice.kyi.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {
	
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);
	
	private final JavaMailSender javaMailSender;
	private static int number;
	
	public static void createNumber() {
		number = (int)(Math.random()*(9000))+10000;
	}
	
	public MimeMessage createMail(String recipientEmail, HttpServletRequest request) {
		if(recipientEmail == null || recipientEmail.isEmpty()) {
			throw new IllegalArgumentException("수신자 이메일 주소가 없습니다.");
		}
		createNumber();
		HttpSession session = request.getSession();
		session.setAttribute("codeNumber", number);
		
		MimeMessage message = javaMailSender.createMimeMessage();
		
		try {
			JavaMailSenderImpl senderImpl = (JavaMailSenderImpl) javaMailSender;
			String senderEmail = senderImpl.getUsername();
			
			MimeMessageHelper helper = new MimeMessageHelper(message,"UTF-8");
			helper.setFrom(new InternetAddress(senderEmail));
			helper.setTo(new InternetAddress(recipientEmail));
			helper.setSubject("인증");
			
			String body = "<h3>요청하신 인증 번호입니다.</h3><h1>" + number + "</h1><h3>감사합니다.</h3>";
			helper.setText(body, true);
			
			logger.info("송신 이메일 : "+senderEmail);
			logger.info("수신 이메일 : "+recipientEmail);
		} catch (Exception e) {
			 logger.error("이메일 생성 중 오류 발생", e);
			 throw new RuntimeException("이메일 생성 중 오류 발생", e);
		}
		
		return message;		
	}
	
	public int sendEmail(String recipientEmail, HttpServletRequest request) {
		MimeMessage message = createMail(recipientEmail, request);
		try {
			javaMailSender.send(message);
			logger.info("이메일 전송 성공");
		} catch (Exception e) {
			logger.error("이메일 전송 실패 ",e);
		}
		return number;
	}
	
	

}
