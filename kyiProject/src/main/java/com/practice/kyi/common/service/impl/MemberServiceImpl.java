package com.practice.kyi.common.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.common.dao.LoginDAO;
import com.practice.kyi.common.dao.vo.LoginVO;
import com.practice.kyi.common.service.MemberService;

@Service("MemberService")
public class MemberServiceImpl implements MemberService{
	
	@Autowired
	private LoginDAO loginDAO;
	
	@Override
	public String registMember(LoginVO vo) {
		loginDAO.registMember(vo);
		return vo.getMemberId();
	}

	@Override
	public int selectMember(String memberId) {
		return loginDAO.selectMember(memberId);
		
	}

	@Override
	public String memberPwChk(String memberId) {
		return loginDAO.memberPwChk(memberId);
	}

	@Override
	public String memberSaltKey(String memeberId) {
		return loginDAO.memberSaltKey(memeberId);
	}

	@Override
	public String memberCertify(String memberId) {
		return loginDAO.memberCertify(memberId);
	}

	@Override
	public LoginVO memberInfo(LoginVO vo) {
		return loginDAO.memberInfo(vo);
	}

	@Override
	public String memberCertifyUpdate(String memberId) {
		return loginDAO.memberCertifyUpdate(memberId);
	}

	@Override
	public String memberApprove(String memberId) {
		return loginDAO.memberApprove(memberId);
	}

	@Override
	public String memberApproveUpdate(String memberId) {
		return loginDAO.memberApproveUpdate(memberId);
	}

	@Override
	public String memberBlockkYn(String memberId) {
		return loginDAO.memberBlockYn(memberId);
	}

}
