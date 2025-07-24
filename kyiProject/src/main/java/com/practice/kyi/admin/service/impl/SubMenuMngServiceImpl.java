package com.practice.kyi.admin.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin.dao.MenuMngDAO;
import com.practice.kyi.admin.dao.vo.SubMenuVO;
import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.admin.service.SubMenuMngService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("SubMenuMngService")
public class SubMenuMngServiceImpl  extends EgovAbstractServiceImpl implements SubMenuMngService{
	
	@Autowired
	MenuMngDAO menuMngDAO;

	@Override
	public List<SubMenuVO> selectSubMenuList(SubMenuVO vo) {
		List<SubMenuVO> list = menuMngDAO.subMenuList(vo);
		return list;
	}

	@Override
	public List<TopMenuVO> topMenuNm(TopMenuVO vo) {
		List<TopMenuVO> list = menuMngDAO.topMenuNm(vo); 
		return list;
	}

	@Override
	public int registSubMenu(SubMenuVO vo) {
		return menuMngDAO.registSubMenu(vo);
	}

	@Override
	public int selectSubMenu(String subMenu) {
		return menuMngDAO.selectSubMenu(subMenu);
	}

	@Override
	public int selectSubMenuNm(String subMenuNm) {
		return menuMngDAO.selectSubMenuNm(subMenuNm);
	}

	@Override
	public SubMenuVO selectSubMenuForm(String subMenuSeq) {
		return menuMngDAO.subMenuForm(subMenuSeq);
	}

	@Override
	public int updateSubMenu(SubMenuVO vo) {
		return menuMngDAO.updateSubMenu(vo);
	}

	@Override
	public int activeSubMenu(SubMenuVO vo) {
		return menuMngDAO.activeSubMenu(vo);
	}

	@Override
	public int updateSubMenuOrder(List<Map<String, Object>> menuList) {
	    int result = 0;
	    for (Map<String, Object> menu : menuList) {
	        SubMenuVO vo = new SubMenuVO();
	        vo.setSubMenuSeq(String.valueOf(menu.get("subMenuSeq")));
	        vo.setSubMenuOrder((Integer) menu.get("subMenuOrder"));
	        
	        result += menuMngDAO.updateSubMenuOrder(vo);
	    }
	    return result;
	}

}
