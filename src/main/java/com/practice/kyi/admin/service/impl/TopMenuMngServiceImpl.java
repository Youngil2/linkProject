package com.practice.kyi.admin.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin.dao.MenuMngDAO;
import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.admin.service.TopMenuMngService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("TopMenuMngService")
public class TopMenuMngServiceImpl  extends EgovAbstractServiceImpl implements TopMenuMngService{
	
	@Autowired
	MenuMngDAO menuMngDAO;

	@Override
	public List<TopMenuVO> selectTopMenuList(TopMenuVO vo) {
		List<TopMenuVO> list = menuMngDAO.topMenuList(vo);
		return list;
	}

	@Override
	public int registTopMenu(TopMenuVO vo) {
		return menuMngDAO.registTopMenu(vo);
	}

	@Override
	public int selectTopMenu(String topMenu) {
		return menuMngDAO.selectTopMenu(topMenu);
	}

	@Override
	public int selectTopMenuNm(String topMenuNm) {
		return menuMngDAO.selectTopMenuNm(topMenuNm);
	}

	@Override
	public TopMenuVO selectTopMenuForm(String topMenuSeq) {
		return menuMngDAO.topMenuForm(topMenuSeq);
	}

	@Override
	public int updateTopMenu(TopMenuVO vo) {
		return menuMngDAO.updateTopMenu(vo);

	}

	@Override
	public int activeTopMenu(TopMenuVO vo) {
		return menuMngDAO.activeTopMenu(vo);
	}

	@Override
	public int updateTopMenuOrder(List<Map<String, Object>> menuList) {
	    int result = 0;
	    for (Map<String, Object> menu : menuList) {
	        TopMenuVO vo = new TopMenuVO();
	        vo.setTopMenuSeq(String.valueOf(menu.get("topMenuSeq")));
	        vo.setTopMenuOrder((Integer) menu.get("topMenuOrder"));
	        
	        result += menuMngDAO.updateTopMenuOrder(vo);
	    }
	    return result;
	}

}
