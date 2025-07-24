package com.practice.kyi.common.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.practice.kyi.common.dao.vo.FaqVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class ChatDAO extends EgovAbstractMapper{
	
	public List<FaqVO> findFaq(FaqVO vo){
		return this.selectList("BoardDAO.findFaq", vo);
	}
	

}
