package com.practice.kyi.admin_link.service;

import java.util.List;

import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.dao.vo.ScheduleHistoryVO;

public interface PublicDataScheduleService {
	
    void startAllSchedules() throws Exception;
    void stopAllSchedules() throws Exception;
    void startSchedule(String configId) throws Exception;
    void stopSchedule(String configId) throws Exception;
    void executeScheduledApi(ApiConfigVO configVO) throws Exception;
    List<ScheduleHistoryVO> getScheduleHistoryList(ScheduleHistoryVO searchVO) throws Exception;
    
    public List<ApiConfigVO> getScheduleListWithStatus(ApiConfigVO searchVO)  throws Exception;
    public boolean isScheduleRunning(String configId);
    
    ScheduleHistoryVO selectHistory(String historyId);
    ScheduleHistoryVO historyStatistics (String configId);
    
    int selectConfigId(String configId);
    int updateApiName(String configId, String apiName);

}
