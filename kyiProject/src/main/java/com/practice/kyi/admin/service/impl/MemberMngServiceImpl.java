package com.practice.kyi.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin.dao.MemberMngDAO;
import com.practice.kyi.admin.service.MemberMngService;
import com.practice.kyi.common.dao.vo.LoginVO;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("MemberMngService")
public class MemberMngServiceImpl   extends EgovAbstractServiceImpl implements MemberMngService{
	
	@Autowired
	MemberMngDAO memberMngDAO;
	
	@Override
	public List<LoginVO> selectMemberList(LoginVO vo) {
		List<LoginVO> list = memberMngDAO.memberList(vo);
		return list;
	}

	@Override
	public int approveMember(LoginVO vo) {
		return memberMngDAO.approveMemberUpdate(vo);
	}

	@Override
	public int blockMember(LoginVO vo) {
		return memberMngDAO.blockMemberUpdate(vo);
	}

	@Override
	public int changeRole(LoginVO vo) {
		return memberMngDAO.changeRole(vo);
	}


}
