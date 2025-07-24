package com.practice.kyi.admin_link.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class ApiConfigDAO extends EgovAbstractMapper{

    private static final String NAMESPACE = "apiConfig.";
    
    /**
     * API 설정 목록 조회
     */
    public List<ApiConfigVO> selectApiConfigList(ApiConfigVO searchVO) throws Exception {
        return selectList(NAMESPACE + "selectApiConfigList", searchVO);
    }
    
    /**
     * 활성화된 API 설정 목록 조회
     */
    public List<ApiConfigVO> selectActiveApiConfigList() throws Exception {
        return selectList(NAMESPACE + "selectActiveApiConfigList");
    }
    
    /**
     * API 설정 상세 조회
     */
    public ApiConfigVO selectApiConfig(ApiConfigVO searchVO) throws Exception {
        return selectOne(NAMESPACE + "selectApiConfig", searchVO);
    }
    
    /**
     * API 설정 등록
     */
    public int insertApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return insert(NAMESPACE + "insertApiConfig", apiConfigVO);
    }
    
    /**
     * API 설정 수정
     */
    public int updateApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return update(NAMESPACE + "updateApiConfig", apiConfigVO);
    }
    
    /**
     * API 설정 삭제
     */
    public int deleteApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return delete(NAMESPACE + "deleteApiConfig", apiConfigVO);
    }
    
    /**
     * 스케줄 활성화된 API 설정 목록 조회
     */
    public List<ApiConfigVO> selectScheduleActiveApiConfigList() throws Exception {
        return selectList(NAMESPACE + "selectScheduleActiveApiConfigList");
    }
    
    /**
     * API 설정의 마지막 실행 정보 업데이트
     */
    public int updateLastExecuteInfo(ApiConfigVO apiConfigVO) throws Exception {
        return update(NAMESPACE + "updateLastExecuteInfo", apiConfigVO);
    }
    
    /**
     * 다음 실행 시간 업데이트
     */
    public int updateNextExecuteTime(ApiConfigVO apiConfigVO) throws Exception {
        return update(NAMESPACE + "updateNextExecuteTime", apiConfigVO);
    }

    /**
     * 스케줄 상태 업데이트
     */
    public int updateScheduleStatus(ApiConfigVO apiConfigVO) throws Exception {
        return update(NAMESPACE + "updateScheduleStatus", apiConfigVO);
    }
    
    /**
     * 모든 스케줄 상태 업데이트
     */
    public int updateAllScheduleStatus(String status) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("scheduleStatus", status);
        return update(NAMESPACE + "updateAllScheduleStatus", paramMap);
    }
    /**
     * 연계 데이터 폼
     */
    public ApiConfigVO selectApiForm(String configId){
    	return this.selectOne(NAMESPACE+"selectApiForm",configId);
    }
    public int apiConfigUpdate(ApiConfigVO vo) {
    	return this.update(NAMESPACE+"updateApiConfig",vo);
    }
}
