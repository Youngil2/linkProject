package com.practice.kyi.admin.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.practice.kyi.common.dao.vo.LoginVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class MemberMngDAO extends EgovAbstractMapper{
	
	public List<LoginVO> memberList(LoginVO vo){
		return this.selectList("memberDAO.memberList", vo);
	}
	
	public int approveMemberUpdate(LoginVO vo) {
		return this.update("memberDAO.approveMemberUpdate",vo);
	}
	public int blockMemberUpdate(LoginVO vo) {
		return this.update("memberDAO.blockMemberUpdate",vo);
	}
	public int changeRole(LoginVO vo) {
		return this.update("memberDAO.changeRole",vo);
	}
}
