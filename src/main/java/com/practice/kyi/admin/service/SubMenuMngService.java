package com.practice.kyi.admin.service;

import java.util.List;
import java.util.Map;

import com.practice.kyi.admin.dao.vo.SubMenuVO;
import com.practice.kyi.admin.dao.vo.TopMenuVO;

public interface SubMenuMngService {
	
	List<SubMenuVO> selectSubMenuList(SubMenuVO vo);
	
	 //셀렉트 박스처리용
	 List<TopMenuVO> topMenuNm(TopMenuVO vo);
	 
	 int registSubMenu(SubMenuVO vo);
	 public int selectSubMenu(String subMenu);
	 public int selectSubMenuNm(String subMenuNm);
	 
	 SubMenuVO selectSubMenuForm(String subMenuSeq);
	 int updateSubMenu(SubMenuVO vo);
	 
	 int activeSubMenu(SubMenuVO vo);
	 
	 int updateSubMenuOrder(List<Map<String, Object>> menuList);

}
