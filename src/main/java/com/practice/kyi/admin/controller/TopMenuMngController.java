package com.practice.kyi.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.admin.service.TopMenuMngService;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

@Controller
@RequestMapping("/admin")
public class TopMenuMngController {
	
	private static final Logger logger = LoggerFactory.getLogger(TopMenuMngController.class);
	
	@Autowired
	TopMenuMngService topMenuMngService;

	//상위메뉴 생성 폼
	@GetMapping("menu_mng/top_menu_mng")
	public String topMenuMng() {
		return "mng/topMenuMngList";
	}
	
	@PostMapping("menu_mng/top_menu_mng_list")
	@ResponseBody
	public Map<String, Object> topMenuList(@RequestBody Map<String, Object> request) {
	    Map<String, Object> data = new HashMap<>();
	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String deleteYn = request.get("deleteYn") != null ? request.get("deleteYn").toString() : "";
	
	    // VO에 설정
	    TopMenuVO topMenuVO = new TopMenuVO();
	    topMenuVO.setPageUnit(pageUnit);
	    topMenuVO.setPageIndex(pageIndex);
	    topMenuVO.setSearchKeyword(searchKeyword);
	    topMenuVO.setSearchType(searchType);
	    topMenuVO.setDeleteYn(deleteYn);
	
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(topMenuVO);
	
	    // 리스트 조회
	    List<TopMenuVO> topMenuList = topMenuMngService.selectTopMenuList(topMenuVO);
	
	    int totalCount = 0;
	    if (!topMenuList.isEmpty()) {
	        totalCount = topMenuList.get(0).getTotalCount(); // VO에 totalCount 들어오는 경우
	    }
	
	    pagination.setTotalRecordCount(totalCount);
	
	    // 응답 구성
	    data.put("topMenuList", topMenuList);
	    data.put("totalCount", totalCount);
	    return data;
	}
	
	@GetMapping("top_menu_create")
	public String topMenuCreate() {
		return "mng/topMenuMngForm";
	}
	
	//상위메뉴 생성
	@PostMapping("/menuCreate")
	@ResponseBody 
	public Map<String, Object> menuCreate(TopMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			topMenuMngService.registTopMenu(vo);
			logger.info(vo.getTopMenuNm()+" 생성");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
	//상위메뉴 영문명 중복체크
	@PostMapping("topMenuChk")
	@ResponseBody
	public int topMenuChk(@RequestBody Map<String, String> request) {
		String topMemu = request.get("topMenu");
		int topMenuChk = topMenuMngService.selectTopMenu(topMemu);
		return topMenuChk;
	}
	//상위메뉴 한글명 중복체크
	@PostMapping("/topMenuNmChk")
	@ResponseBody
	public int topMenuNmChk(@RequestBody Map<String, String> request) {
		String topMemuNm = request.get("topMenuNm");
		int topMenuNmChk = topMenuMngService.selectTopMenuNm(topMemuNm);
		return topMenuNmChk;
	}
	//상위매뉴 수정 폼
	@GetMapping("top_menu_update")
	public String topMenuUpdate(@RequestParam("top_menu_seq") String topMenuSeq, Model model) {
		 TopMenuVO topMenu = topMenuMngService.selectTopMenuForm(topMenuSeq.toUpperCase());
		 model.addAttribute("topMenu",topMenu);
		return "mng/topMenuMngForm";
	}
	
	//상위메뉴 수정
	@PostMapping("/topMenuUpdate")
	@ResponseBody 
	public Map<String, Object> topMenuUpdate(TopMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			topMenuMngService.updateTopMenu(vo);
			logger.info(vo.getTopMenuNm()+" 수정");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
	//상위메뉴 활성화 및 비활성화
	@PostMapping("top_menu_active")
	@ResponseBody 
	public Map<String, Object> topMenuActive(@RequestBody TopMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			topMenuMngService.activeTopMenu(vo);
			logger.info(vo.getTopMenuNm()+" 수정");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
	//상위메뉴 순서 변경
	@PostMapping("/updateTopMenuOrder")
	@ResponseBody
	public Map<String, Object> updateTopMenuOrder(@RequestBody Map<String, Object> requestData) {
		Map<String, Object> params = new HashMap<>();
		
	    try {
	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> menuList = (List<Map<String, Object>>) requestData.get("menuList");
	        
	        topMenuMngService.updateTopMenuOrder(menuList);
	        params.put("success", true);
	        params.put("result", "success");
	    } catch (Exception e) {
	        params.put("success", false);
	        params.put("result", "error");
	        params.put("message", e.getMessage());
	    }	
		return params;
	}
}
