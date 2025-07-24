package com.practice.kyi.admin.service;

import java.util.List;

import com.practice.kyi.common.dao.vo.LoginVO;

public interface MemberMngService {
	
	List<LoginVO> selectMemberList(LoginVO vo);
	int approveMember(LoginVO vo);
	int blockMember(LoginVO vo);
	int changeRole(LoginVO vo);
}
