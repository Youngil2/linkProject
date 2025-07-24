package com.practice.kyi.admin_link.controller;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.dao.vo.ScheduleHistoryVO;
import com.practice.kyi.admin_link.service.ApiConfigService;
import com.practice.kyi.admin_link.service.PublicDataScheduleService;
import com.practice.kyi.admin_link.service.PublicDataService;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class PublicDataController {
	
    @Autowired
    private PublicDataService publicDataService;
    
    @Autowired
    private ApiConfigService apiConfigService;
    
    @Autowired
    private PublicDataScheduleService scheduleService;
    
    private static final Logger logger = LoggerFactory.getLogger(PublicDataController.class);
    
    
    /**
     * API 설정 목록 화면
     */
    @GetMapping("/datalink_mng/link_mng")
    public String apiConfigListl() {

        return "/dataLink/apiConfigList";
    }
    @PostMapping("linkList")
    @ResponseBody
    public Map<String, Object> linkList(@RequestBody Map<String, Object> request) throws Exception{
    	Map<String, Object> data = new HashMap<>();
    	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String useYn = request.get("useYn") != null ? request.get("useYn").toString() : "";
    	
	    ApiConfigVO apiConfigVO = new ApiConfigVO();
	    apiConfigVO.setPageUnit(pageUnit);
	    apiConfigVO.setPageIndex(pageIndex);
	    apiConfigVO.setSearchKeyword(searchKeyword);
	    apiConfigVO.setSearchType(searchType);
	    apiConfigVO.setUseYn(useYn);
	    
	    Pagination pagination = PageUtil.setPagination(apiConfigVO);
	    
	    List<ApiConfigVO> apiConfigList = apiConfigService.selectApiConfigList(apiConfigVO);
	    
	    int totalCount = 0;
	    if(!apiConfigList.isEmpty()) {
	    	totalCount = apiConfigList.get(0).getTotalCount();
	    }
	    
	    pagination.setTotalRecordCount(totalCount);
	    
	    data.put("totalCount", totalCount);
	    data.put("apiConfigList", apiConfigList);
	    
    	return data;
    }
    
    /**
     * API 연계 등록
     */
    @GetMapping("link_create")
    public String apiLinkCreate() {
    	return "/dataLink/apiConfigForm";
    }
    @PostMapping("/apiConfigCreate")
    @ResponseBody
    public Map<String, Object> menuCreate(@RequestBody ApiConfigVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			apiConfigService.insertApiConfig(vo);
			logger.info(vo.getConfigId()+" 생성");
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
    /**
     * API 연계 수정 
     * @param configId
     * @param model
     * @return
     */
	@GetMapping("datalink_update")
	public String ApiFormUpdate(@RequestParam("config_id") String configId, Model model) {
		 	ApiConfigVO apiConfig = publicDataService.selectApiForm(configId.toUpperCase());	 
		 
		    // JSON 데이터 안전한 처리
		    String mappingConfig = processJsonData(apiConfig.getMappingConfig());
		    String parameterConfig = processJsonData(apiConfig.getParameterConfig());

		    model.addAttribute("apiConfig", apiConfig);
		    model.addAttribute("mappingConfigJson", mappingConfig);
		    model.addAttribute("parameterConfigJson", parameterConfig);
		    
		    return "/dataLink/apiConfigForm";
		}

		private String processJsonData(String jsonData) {
		    if (jsonData == null || jsonData.trim().isEmpty() || jsonData.equals("[")) {
		        return "[]";
		    }
		    
		    try {
		        ObjectMapper mapper = new ObjectMapper();
		        // JSON 유효성 검사
		        JsonNode jsonNode = mapper.readTree(jsonData);
		        
		        // 빈 배열이거나 null인 경우 처리
		        if (jsonNode.isArray() && jsonNode.size() == 0) {
		            return "[]";
		        }
		        
		        // 유효한 JSON이면 그대로 반환
		        return mapper.writeValueAsString(jsonNode);
		    } catch (Exception e) {
		        System.out.println("JSON 파싱 실패, 기본값 반환: " + e.getMessage());
		        System.out.println("원본 데이터: " + jsonData);
		        return "[]";
		    }
		}

	@PostMapping("apiConfigUpdate")
	@ResponseBody 
	public Map<String, Object> apiConfigUpdate(@RequestBody ApiConfigVO vo) {
		Map<String, Object> params = new HashMap<>();		
		try {
			publicDataService.apiConfigUpdate(vo);
			logger.info(vo.getConfigId()+" 수정");
			
			String configId = vo.getConfigId();
			int historyChk = scheduleService.selectConfigId(configId);
			
			if(historyChk>0) {
				scheduleService.updateApiName(configId, vo.getApiName());
			}
			
			params.put("state", true);
		} catch (Exception e) {
			params.put("state", false);
		}
		return params;
	}
    /**
     * 전체 API 연계 실행
     */
    @PostMapping("/executeAllSync")
    @ResponseBody
    public Map<String, Object> executeAllSync() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            publicDataService.executeAllApiConnections();
            result.put("success", true);
            result.put("message", "모든 API 연계가 완료되었습니다.");
            
        } catch (Exception e) {
            logger.error("전체 API 연계 실패", e);
            result.put("success", false);
            result.put("message", "API 연계 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 특정 API 연계 실행
     */
    @PostMapping("/executeSync")
    @ResponseBody
    public Map<String, Object> executeSync(@RequestParam("configId") String configId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ApiConfigVO searchVO = new ApiConfigVO();
            searchVO.setConfigId(configId);
            ApiConfigVO configVO = apiConfigService.selectApiConfig(searchVO);          
            if (configVO != null) {
                publicDataService.executeApiConnection(configVO);
                result.put("success", true);
                result.put("message", configVO.getApiName() + " 연계가 완료되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "설정 정보를 찾을 수 없습니다.");
            }
            
        } catch (Exception e) {
            logger.error("API 연계 실패", e);
            result.put("success", false);
            result.put("message", "API 연계 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 스케줄 상태 실시간 조회 (AJAX)
     */
    @PostMapping("/getScheduleStatus")
    @ResponseBody
    public Map<String, Object> getScheduleStatus(@RequestParam("configId") String configId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean isRunning = scheduleService.isScheduleRunning(configId);
            result.put("success", true);
            result.put("isRunning", isRunning);
            result.put("status", isRunning ? "RUNNING" : "STOPPED");
            
        } catch (Exception e) {
            logger.error("스케줄 상태 조회 실패", e);
            result.put("success", false);
            result.put("message", "상태 조회 중 오류가 발생했습니다.");
        }
        
        return result;
    }
    
    /**
     * 스케줄 시작 - 디버깅 강화 버전
     */
    @PostMapping("/startSchedule")
    @ResponseBody
    public Map<String, Object> startSchedule(@RequestParam("configId") String configId, 
                                            HttpServletRequest request, 
                                            HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 요청 정보 로깅
            logger.info("=== 스케줄 시작 요청 ===");
            logger.info("ConfigId: {}", configId);
            logger.info("Request URI: {}", request.getRequestURI());
            
            // Content-Type 설정 (JSON 응답 보장)
            response.setContentType("application/json;charset=UTF-8");
            
            // 스케줄 서비스 호출
            scheduleService.startSchedule(configId);
            
            result.put("success", true);
            result.put("message", "스케줄이 시작되었습니다.");
            result.put("configId", configId);
            result.put("timestamp", System.currentTimeMillis());
            
            logger.info("스케줄 시작 성공: configId={}", configId);
            
        } catch (Exception e) {
            logger.error("스케줄 시작 실패: configId={}", configId, e);
            
            result.put("success", false);
            result.put("message", "스케줄 시작 중 오류가 발생했습니다: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            result.put("configId", configId);
            result.put("timestamp", System.currentTimeMillis());
            
            // HTTP 상태 코드 설정
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        logger.info("응답 데이터: {}", result);
        return result;
    }
    /**
     * 스케줄 중지
     */
    @PostMapping("/stopSchedule")
    @ResponseBody
    public Map<String, Object> stopSchedule(@RequestParam("configId") String configId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            scheduleService.stopSchedule(configId);
            result.put("success", true);
            result.put("message", "스케줄이 중지되었습니다.");
            
        } catch (Exception e) {
            logger.error("스케줄 중지 실패", e);
            result.put("success", false);
            result.put("message", "스케줄 중지 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 전체 스케줄 시작
     */
    @PostMapping("/startAllSchedules")
    @ResponseBody
    public Map<String, Object> startAllSchedules() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            scheduleService.startAllSchedules();
            result.put("success", true);
            result.put("message", "모든 스케줄이 시작되었습니다.");
            
        } catch (Exception e) {
            logger.error("전체 스케줄 시작 실패", e);
            result.put("success", false);
            result.put("message", "스케줄 시작 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 전체 스케줄 중지
     */
    @PostMapping("/stopAllSchedules")
    @ResponseBody
    public Map<String, Object> stopAllSchedules() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            scheduleService.stopAllSchedules();
            result.put("success", true);
            result.put("message", "모든 스케줄이 중지되었습니다.");
            
        } catch (Exception e) {
            logger.error("전체 스케줄 중지 실패", e);
            result.put("success", false);
            result.put("message", "스케줄 중지 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 스케줄 실행 이력 조회
     */
    @GetMapping("/datalink_mng/link_histroy_mng")
    public String scheduleHistory() {        
        return "/dataLink/apiLinkHistory";
    }
    @PostMapping("linkHistory")
    @ResponseBody
    public Map<String, Object> linkHistory(@RequestBody Map<String, Object> request) throws Exception{
    	Map<String, Object> data = new HashMap<>();
    	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String status = request.get("status") != null ? request.get("status").toString() : "";
    	
	    ScheduleHistoryVO vo = new ScheduleHistoryVO();
	    vo.setPageUnit(pageUnit);
	    vo.setPageIndex(pageIndex);
	    vo.setSearchKeyword(searchKeyword);
	    vo.setSearchType(searchType);
	    vo.setStatus(status);
	    
	    Pagination pagination = PageUtil.setPagination(vo);
	    
	    List<ScheduleHistoryVO> historyList = scheduleService.getScheduleHistoryList(vo);
	    
	    int totalCount = 0;
	    if(!historyList.isEmpty()) {
	        System.out.println("first item totalCount: " + historyList.get(0).getTotalCount());
	    	totalCount = historyList.get(0).getTotalCount();
	    }
	    
	    pagination.setTotalRecordCount(totalCount);
	    
	    data.put("totalCount", totalCount);
	    data.put("historyList", historyList);
	    
    	return data;
    }
    /**
     * 연계 상세보기 - 상단 통계 
     */
    @GetMapping("/selectHistory")
    public String selectHistory() {    	
    	return "dataLink/selectHistory";
    }
    @PostMapping("/historyStatistics")
    @ResponseBody
    public Map<String, Object> historyStatistics(@RequestParam("configId") String configId){
    	Map<String, Object> data =new HashMap<>();
    	try {
    		ScheduleHistoryVO result = scheduleService.historyStatistics(configId);
    		data.put("successRate", result.getSuccessRate());
            data.put("processedCount", result.getProcessedCount());
            data.put("executeDurationInSeconds", result.getExecuteDurationInSeconds());
            data.put("errorCount", result.getErrorCount());
    		data.put("success", true);    		
    	}catch (Exception e) {
            logger.error("이력 통계 호출 실패", e);
            data.put("success", false);
            data.put("message", "이력 통계 호출 중 오류가 발생했습니다: " + e.getMessage());
		}    	
    	return data;
    }
    @PostMapping("/selectHistory")
    @ResponseBody
    public Map<String, Object> selectHistory(@RequestParam("historyId") String historyId){
    	Map<String, Object> data =new HashMap<>();
    	try {    		
    		ScheduleHistoryVO result = scheduleService.selectHistory(historyId);
    		//정보테이블 영역
    		//기본 정보
    		data.put("configId", result.getConfigId());
    		data.put("apiName", result.getApiName());
    		data.put("executeTime", result.getExecuteTime());
    		data.put("status", result.getStatus());
    		data.put("message", result.getMessage());
    		
    		//실행 시간 정보
    		data.put("executeStartTime", result.getExecuteStartTime());
    		data.put("executeEndTime", result.getExecuteEndTime());
    		data.put("executeDuration", result.getExecuteDuration());
    		data.put("processedCount", result.getProcessedCount());
    		data.put("errorCount", result.getErrorCount());
    		//처리속도
    		data.put("processingSpeed", result.getProcessingSpeed());    		
    		data.put("success", true);
    	}catch (Exception e) {    		
    		logger.error("연계 이력 상세 정보 호출 오류 ",e);
    		data.put("success", false);    		
		}
    	return data;
    }
    
	@GetMapping("api_pop_up")
	public String ApiLinkPopUp(@RequestParam("config_id") String configId, Model model) {
		 	ApiConfigVO apiConfig = publicDataService.selectApiForm(configId.toUpperCase());	 
		 
		    // JSON 데이터 안전한 처리
		    String mappingConfig = processJsonData(apiConfig.getMappingConfig());
		    String parameterConfig = processJsonData(apiConfig.getParameterConfig());

		    model.addAttribute("apiConfig", apiConfig);
		    model.addAttribute("mappingConfigJson", mappingConfig);
		    model.addAttribute("parameterConfigJson", parameterConfig);
		    
		    return "/dataLink/apiLinkPopUp";
		}
}
