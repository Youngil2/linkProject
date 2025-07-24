package com.practice.kyi.admin_link.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.practice.kyi.admin_link.dao.vo.ScheduleHistoryVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class ScheduleHistoryDAO extends EgovAbstractMapper{
	
    private static final String NAMESPACE = "scheduleHistory.";
    /**
     * 실행 이력 등록
     */
    public int insertScheduleHistory(ScheduleHistoryVO historyVO) throws Exception {
        return insert(NAMESPACE + "insertScheduleHistory", historyVO);
    }
    
    /**
     * 실행 이력 목록 조회
     */
    public List<ScheduleHistoryVO> selectScheduleHistoryList(ScheduleHistoryVO searchVO) throws Exception {
        return selectList(NAMESPACE + "scheduleHistoryList", searchVO);
    }
    
    /**
     * 실행 이력 완료 업데이트
     */
    public int updateScheduleHistoryComplete(ScheduleHistoryVO historyVO) throws Exception {
        return update(NAMESPACE + "updateScheduleHistoryComplete", historyVO);
    }
    /**
     * 실행 이력 상세 조회
     */
    public ScheduleHistoryVO selectHistory(String historyId) {    	
    	return selectOne(NAMESPACE+"selectHistory" ,historyId);
    }
    
    /**
     * 실행 이력 통계
     */
    public ScheduleHistoryVO historyStatistics(String configId) {
    	return selectOne(NAMESPACE+"historyStatistics",configId);
    }
    /**
     * 실행 이력 수정
     */
    public int selectConfigId(String configId) {
    	return selectOne(NAMESPACE+"selectConfigId",configId);
    }
    public int updateApiName(String configId, String apiName) {
    	 Map<String, Object> params = new HashMap<>();
         params.put("configId", configId);
         params.put("apiName", apiName);
    	return update(NAMESPACE+"updateApiName",params);
    }
}
