package com.practice.kyi.common.dao;

import org.springframework.stereotype.Repository;

import com.practice.kyi.common.dao.vo.LoginVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class LoginDAO extends EgovAbstractMapper{
	
	//회원가입
	public int registMember(LoginVO vo) {
		return this.insert("loginDAO.registMember", vo);
	}
	//아이디 체크
	public int selectMember(String memberId) {
		return this.selectOne("loginDAO.selectMemberId", memberId);
	}
	//비밀번호 일치 여부
	public String memberPwChk(String memberId) {
		return this.selectOne("loginDAO.memberPwChk", memberId);
	}
	//saltKey 추출
	public String memberSaltKey(String memberId) {
		return this.selectOne("loginDAO.memberSaltKey",memberId);
	}
	//회원 인증 여부
	public String memberCertify(String memberId) {
		return this.selectOne("loginDAO.memberCertify",memberId);
	}
	//회원 승인 여부
	public String memberApprove(String memberId) {
		return this.selectOne("loginDAO.memberApprove",memberId);
	}
	//회원 차단 여부
	public String memberBlockYn(String memberId) {
		return this.selectOne("loginDAO.memberBlockYn",memberId);
	}
	//회원 정보 가져오기
	public LoginVO memberInfo(LoginVO vo) {
		return this.selectOne("loginDAO.memberInfo",vo);
	}
	//회원 인증 업데이트
	public String memberCertifyUpdate(String memberId) {
		return this.selectOne("loginDAO.memberCertifyUpdate",memberId);
	}
	//회원 승인 업데이트
	public String memberApproveUpdate(String memberId) {
		return this.selectOne("loginDAO.memberApproveUpdate",memberId);
	}
	
}
