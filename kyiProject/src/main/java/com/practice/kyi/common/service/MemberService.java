package com.practice.kyi.common.service;

import com.practice.kyi.common.dao.vo.LoginVO;

public interface MemberService {
	
	//회원가입
	String registMember(LoginVO vo);
	//아이디 체크
	public int selectMember(String memberId);
	//비밀번호 체크
	String memberPwChk(String memberId);
	//saltKey 추출
	String memberSaltKey(String memeberId);
	//회원 인증 여부
	String memberCertify(String memberId);
	//회원 승인 여부
	String memberApprove(String memberId);
	//회원 차단 여부
	String memberBlockkYn(String memberId);
	//회원 정보 가져오기
	LoginVO memberInfo(LoginVO vo);
	//회원 인증 업데이트
	String memberCertifyUpdate(String memberId);
	//회원 승인 업데이트
	String memberApproveUpdate(String memberId);
	

}
