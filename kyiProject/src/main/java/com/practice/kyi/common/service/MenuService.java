package com.practice.kyi.common.service;

import java.util.List;
import java.util.Map;

import com.practice.kyi.admin.dao.vo.TopMenuVO;

public interface MenuService {
	List<Map<String, Object>> selectTopMenuList(TopMenuVO vo);
    List<Map<String, Object>> selectSubMenuList(String topMenuNm);
}