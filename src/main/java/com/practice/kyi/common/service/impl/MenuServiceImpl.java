package com.practice.kyi.common.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.common.dao.MenuDAO;
import com.practice.kyi.common.service.MenuService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("MenuService")
public class MenuServiceImpl extends EgovAbstractServiceImpl implements MenuService{
	
	@Autowired
	private MenuDAO menuDAO;
	
	
	@Override
	public List<Map<String, Object>> selectTopMenuList(TopMenuVO vo) {
		return menuDAO.selectTopMenuList(vo);
	}


	@Override
	public List<Map<String, Object>> selectSubMenuList(String topMenuNm) {
		return menuDAO.selectSubMenuList(topMenuNm);
	}

}