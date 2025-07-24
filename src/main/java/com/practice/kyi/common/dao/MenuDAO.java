package com.practice.kyi.common.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.practice.kyi.admin.dao.vo.TopMenuVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class MenuDAO extends EgovAbstractMapper{
	
	public List<Map<String, Object>> selectTopMenuList(TopMenuVO vo){
		return this.selectList("MenuDAO.selectTopMenuList", vo);
	}
	public List<Map<String, Object>> selectSubMenuList(String topMenuNm) {
		return this.selectList("MenuDAO.selectSubMenuList",topMenuNm);
	}
}