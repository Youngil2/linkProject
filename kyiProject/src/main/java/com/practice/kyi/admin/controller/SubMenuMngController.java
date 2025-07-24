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

import com.practice.kyi.admin.dao.vo.SubMenuVO;
import com.practice.kyi.admin.dao.vo.TopMenuVO;
import com.practice.kyi.admin.service.SubMenuMngService;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

@Controller
@RequestMapping("/admin")
public class SubMenuMngController {
	
	private static final Logger logger = LoggerFactory.getLogger(SubMenuMngController.class);
	
	@Autowired
	SubMenuMngService subMenuMngService;

	//상위메뉴 생성 폼
	@GetMapping("menu_mng/sub_menu_mng")
	public String subMenuMng() {
		return "mng/subMenuMngList";
	}
	@PostMapping("menu_mng/sub_menu_mng_list")
	@ResponseBody
	public Map<String, Object> subMenuList(@RequestBody Map<String, Object> request) {
	    Map<String, Object> data = new HashMap<>();
	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String deleteYn = request.get("deleteYn") != null ? request.get("deleteYn").toString() : "";
	
	    // VO에 설정
	    SubMenuVO subMenuVO = new SubMenuVO();
	    subMenuVO.setPageUnit(pageUnit);
	    subMenuVO.setPageIndex(pageIndex);
	    subMenuVO.setSearchKeyword(searchKeyword);
	    subMenuVO.setSearchType(searchType);
	    subMenuVO.setDeleteYn(deleteYn);
	
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(subMenuVO);
	
	    // 리스트 조회
	    List<SubMenuVO> subMenuList = subMenuMngService.selectSubMenuList(subMenuVO);
	
	    int totalCount = 0;
	    if (!subMenuList.isEmpty()) {
	        totalCount = subMenuList.get(0).getTotalCount(); // VO에 totalCount 들어오는 경우
	    }
	
	    pagination.setTotalRecordCount(totalCount);
	
	    // 응답 구성
	    data.put("subMenuList", subMenuList);
	    data.put("totalCount", totalCount);
	    return data;
	}
	
	@GetMapping("sub_menu_create")
	public String subMenuCreate() {
		return "mng/subMenuMngForm";
	}
	
	//하위메뉴 생성 폼 셀렉트 박스
	@PostMapping("/selectTopList")
	@ResponseBody
	public Map<String, Object> selectTopMenuList(TopMenuVO vo){
		Map<String, Object> res = new HashMap<>();
	    List<TopMenuVO> topMenuList = subMenuMngService.topMenuNm(vo);
	    res.put("topMenuList", topMenuList);
		return res;
	}
	//상위메뉴 생성
	@PostMapping("/subMenuCreate")
	@ResponseBody 
	public Map<String, Object> subMenuCreate(SubMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			subMenuMngService.registSubMenu(vo);
			logger.info(vo.getSubMenuNm()+" 생성");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
	//상위메뉴 영문명 중복체크
	@PostMapping("subMenuChk")
	@ResponseBody
	public int subMenuChk(@RequestBody Map<String, String> request) {
		String subMemu = request.get("subMenu");
		int topMenuChk = subMenuMngService.selectSubMenu(subMemu);
		return topMenuChk;
	}
	//상위메뉴 한글명 중복체크
	@PostMapping("/subMenuNmChk")
	@ResponseBody
	public int subMenuNmChk(@RequestBody Map<String, String> request) {
		String subMemuNm = request.get("subMenuNm");
		int subMenuNmChk = subMenuMngService.selectSubMenuNm(subMemuNm);
		return subMenuNmChk;
	}
	//상위매뉴 수정 폼
	@GetMapping("sub_menu_update")
	public String subMenuUpdate(@RequestParam("sub_menu_seq") String subMenuSeq, Model model) {
		 SubMenuVO subMenu = subMenuMngService.selectSubMenuForm(subMenuSeq.toUpperCase());
		 model.addAttribute("subMenu",subMenu);
		return "mng/subMenuMngForm";
	}
	
	//상위메뉴 수정
	@PostMapping("/subMenuUpdate")
	@ResponseBody 
	public Map<String, Object> subMenuUpdate(SubMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			System.out.println("vo.getSubMenuSeq() : "+ vo.getSubMenuSeq());
			subMenuMngService.updateSubMenu(vo);
			logger.info(vo.getSubMenuNm()+" 수정");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		System.out.println("params : "+ params);
		
		return params;
	}
	//상위메뉴 활성화 및 비활성화
	@PostMapping("sub_menu_active")
	@ResponseBody 
	public Map<String, Object> subMenuActive(@RequestBody SubMenuVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			subMenuMngService.activeSubMenu(vo);
			logger.info(vo.getTopMenuNm()+" 수정");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
	//상위메뉴 순서 변경
	@PostMapping("/updateSubMenuOrder")
	@ResponseBody
	public Map<String, Object> updateTopMenuOrder(@RequestBody Map<String, Object> requestData) {
		Map<String, Object> params = new HashMap<>();
		
	    try {
	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> menuList = (List<Map<String, Object>>) requestData.get("menuList");
	        
	        subMenuMngService.updateSubMenuOrder(menuList);
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
