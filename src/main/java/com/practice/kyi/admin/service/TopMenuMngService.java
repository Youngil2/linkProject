package com.practice.kyi.admin.service;

import java.util.List;
import java.util.Map;

import com.practice.kyi.admin.dao.vo.TopMenuVO;

public interface TopMenuMngService {
	
	 List<TopMenuVO> selectTopMenuList(TopMenuVO vo);
	
	 int registTopMenu(TopMenuVO vo);
	 public int selectTopMenu(String topMenu);
	 public int selectTopMenuNm(String topMenuNm);
	 
	 TopMenuVO selectTopMenuForm(String topMenuSeq);
	 int updateTopMenu(TopMenuVO vo);
	 
	 int activeTopMenu(TopMenuVO vo);
	 
	 int updateTopMenuOrder(List<Map<String, Object>> menuList);
}
