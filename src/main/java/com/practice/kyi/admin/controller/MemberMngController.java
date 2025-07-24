package com.practice.kyi.admin.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.admin.service.MemberMngService;
import com.practice.kyi.common.dao.vo.LoginVO;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

@Controller
@RequestMapping("/admin")
public class MemberMngController {
	
	private static final Logger logger = LoggerFactory.getLogger(MemberMngController.class);
	
	@Autowired
	MemberMngService memberMngService;
	
	@GetMapping("client_mng/member_mng")
	public String memberMngList() {
		return "mng/memberList";
	}
	
	@PostMapping("client_mng/member_mng_list")
	@ResponseBody
	public Map<String, Object> memberMngList(HttpServletRequest req, @RequestBody Map<String, Object> request) {
	    Map<String, Object> data = new HashMap<>();
	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String approveYn = request.get("approveYn") != null ? request.get("approveYn").toString() : "";
	    String certifyYn = request.get("certifyYn") != null ? request.get("certifyYn").toString() : "";
	    
	    // VO에 설정
	    LoginVO loginVO = new LoginVO();
	    loginVO.setPageUnit(pageUnit);
	    loginVO.setPageIndex(pageIndex);
	    loginVO.setSearchKeyword(searchKeyword);
	    loginVO.setSearchType(searchType);
	    loginVO.setApproveYn(approveYn);
	    loginVO.setCertifyYn(certifyYn);
	
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(loginVO);
	
	    // 리스트 조회
	    List<LoginVO> memberMngList = memberMngService.selectMemberList(loginVO);
	
	    int totalCount = 0;
	    if (!memberMngList.isEmpty()) {
	        totalCount = memberMngList.get(0).getTotalCount(); // VO에 totalCount 들어오는 경우
	    }
	
	    pagination.setTotalRecordCount(totalCount);
	    
	    // 현재 로그인한 사용자 ID 추가
	    HttpSession session = req.getSession();
	    String currentUserId = (String) session.getAttribute("memberId");
	
	    // 응답 구성
	    data.put("currentUserId", currentUserId);
	    data.put("topMenuList", memberMngList);
	    data.put("totalCount", totalCount);
	    return data;
	}
	@PostMapping("approve_member")
	@ResponseBody
	public Map<String, Object> approveMember(@RequestBody LoginVO vo) {
		
		Map<String, Object> params = new HashMap<>();
		try {
			memberMngService.approveMember(vo);
			if ("Y".equals(vo.getApproveYn())) {
				logger.info(vo.getMemberId() + " → 승인 처리됨");
			} else if ("N".equals(vo.getApproveYn())) {
				logger.info(vo.getMemberId() + " → 불승인 처리됨");
			}
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}		
		return params;		
	}
	
	@PostMapping("block_member")
	@ResponseBody
	public Map<String, Object> blockMemberUpdate(@RequestBody LoginVO vo) {
		Map<String, Object> params = new HashMap<>();
		try {
			memberMngService.blockMember(vo);
			if ("Y".equals(vo.getBlockYn())) {
				logger.info(vo.getMemberId() + " → 차단 처리됨");
			} else if ("N".equals(vo.getBlockYn())) {
				logger.info(vo.getMemberId() + " → 차단 해제됨");
			}
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}	
		
		return params;
	}
	@PostMapping("changeRole")
	@ResponseBody
	public Map<String, Object> changeRole(@RequestBody LoginVO vo) {
		Map<String, Object> params = new HashMap<>();
		try {
			memberMngService.changeRole(vo);
			logger.info(vo.getMemberId() +"의 권한이 "+ vo.getMemberRole() +" 변경됨");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}	
		
		return params;
	}
	
}
