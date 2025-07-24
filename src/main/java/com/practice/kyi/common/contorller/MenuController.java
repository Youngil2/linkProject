package com.practice.kyi.common.contorller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.common.service.MenuService;

@Controller
@RequestMapping("/members")
public class MenuController {

	@Autowired
	private MenuService menuService;
	
	@GetMapping("/menu")
	@ResponseBody
	public Map<String, Object> getTopMenu(TopMenuVO vo){
		 Map<String, Object> res = new HashMap<>();
		 List<Map<String, Object>> topMenuList = menuService.selectTopMenuList(vo);
		 
		 for (Map<String, Object> topMenu : topMenuList) {
		        String topMenuNm = (String) topMenu.get("top_menu_nm");
		        List<Map<String, Object>> subMenuList = menuService.selectSubMenuList(topMenuNm);
		        topMenu.put("subMenus", subMenuList != null ? subMenuList : new ArrayList<>());
		    }
		 
		 res.put("topMenuList", topMenuList);
		 return res;
    }
}