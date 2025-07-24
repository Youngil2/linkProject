package com.practice.kyi.admin_link.service.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.util.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin_link.dao.ApiConfigDAO;
import com.practice.kyi.admin_link.dao.ScheduleHistoryDAO;
import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.dao.vo.ScheduleHistoryVO;
import com.practice.kyi.admin_link.service.PublicDataScheduleService;
import com.practice.kyi.admin_link.service.PublicDataService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("PublicDataScheduleService")
public class PublicDataScheduleServiceImpl extends EgovAbstractServiceImpl implements PublicDataScheduleService{
	
    @Autowired
    private ApiConfigDAO apiConfigDAO;
    
    @Autowired
    private ScheduleHistoryDAO scheduleHistoryDAO;
    
    @Autowired
    private PublicDataService publicDataService;
    
    private static final Logger logger = LoggerFactory.getLogger(PublicDataScheduleServiceImpl.class);
    
    // 실행 중인 스케줄 상태를 메모리에서 관리
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> runningSchedules = Collections.synchronizedSet(new HashSet<>());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    /**
     * 모든 스케줄 시작
     */
    @Override
    public void startAllSchedules() throws Exception {
        List<ApiConfigVO> scheduleConfigs = apiConfigDAO.selectScheduleActiveApiConfigList();
        
        for (ApiConfigVO config : scheduleConfigs) {
            startSchedule(config.getConfigId());
        }
        
        logger.info("전체 스케줄 시작 완료: {} 개", scheduleConfigs.size());
    }
    
    
    /**
     * 모든 스케줄 중지
     */
    @Override
    public void stopAllSchedules() throws Exception {
        for (Map.Entry<String, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            entry.getValue().cancel(false);
        }
        scheduledTasks.clear();
        
        // DB에 모든 스케줄 상태를 STOPPED로 업데이트
        apiConfigDAO.updateAllScheduleStatus("STOPPED");
        runningSchedules.clear();
        
        logger.info("전체 스케줄 중지 완료");
    }
    
    
    /**
     * 스케줄 목록 조회 시 실행 상태 포함
     */
    public List<ApiConfigVO> getScheduleListWithStatus(ApiConfigVO searchVO) throws Exception {
        List<ApiConfigVO> scheduleList = apiConfigDAO.selectApiConfigList(searchVO);
        
        // 각 스케줄의 실행 상태 설정
        for (ApiConfigVO schedule : scheduleList) {
            boolean isRunning = runningSchedules.contains(schedule.getConfigId());
            schedule.setScheduleStatus(isRunning ? "RUNNING" : "STOPPED");
            schedule.setIsRunning(isRunning ? "Y" : "N");
        }
        
        return scheduleList;
    }
    /**
     * 특정 스케줄 시작
     */
    @Override
    public void startSchedule(String configId) throws Exception {
    	 // 기존 스케줄이 있으면 중지
        stopSchedule(configId);
        
        ApiConfigVO vo = new ApiConfigVO();
        vo.setConfigId(configId);
        
        ApiConfigVO configVO = apiConfigDAO.selectApiConfig(vo);
        logger.info(vo.getConfigId()+"의 스케줄 실행 여부 : "+configVO.getScheduleYn());
        
        if (configVO == null || !"Y".equals(configVO.getScheduleYn())) {
            return;
        }
        
        ScheduledFuture<?> scheduledTask = null;
        logger.info(configVO.getApiName()+" 연계 스케줄 시작");
        logger.info(configVO.getApiName()+" 연계 스케줄 타입 " + configVO.getScheduleType());
        
        if ("CRON".equals(configVO.getScheduleType())) {
            scheduledTask = scheduleCronTask(configVO);
        } else if ("INTERVAL".equals(configVO.getScheduleType())) {
            scheduledTask = scheduleIntervalTask(configVO);
        }
        if (scheduledTask != null) {
            scheduledTasks.put(configId, scheduledTask);
            runningSchedules.add(configId); // 실행 상태에 추가
            
            // DB에 상태 업데이트
            updateScheduleStatus(configId, "RUNNING");
            
            // 다음 실행시간 초기 설정
            String nextExecuteTime = calculateNextExecuteTime(configVO);
            if (nextExecuteTime != null) {
                ApiConfigVO nextTimeVO = new ApiConfigVO();
                nextTimeVO.setConfigId(configId);
                nextTimeVO.setNextExecuteTime(nextExecuteTime);
                apiConfigDAO.updateNextExecuteTime(nextTimeVO);
            }
            
            logger.info("스케줄 시작: {} - {}, 다음 실행시간: {}", 
                       configVO.getApiName(), 
                       "CRON".equals(configVO.getScheduleType()) ? configVO.getCronExpression() : 
                       configVO.getIntervalMinutes() + "분 간격",
                       nextExecuteTime);
        }
    }
    /**
     * 특정 스케줄 중지
     */
    @Override
    public void stopSchedule(String configId) throws Exception {
        ScheduledFuture<?> task = scheduledTasks.remove(configId);
        if (task != null) {
            task.cancel(false);
        }
        ApiConfigVO nextTimeVO = new ApiConfigVO();
        nextTimeVO.setConfigId(configId);
        nextTimeVO.setNextExecuteTime(null);
        apiConfigDAO.updateNextExecuteTime(nextTimeVO);
        
        runningSchedules.remove(configId); // 실행 상태에서 제거
        
        // DB에 상태 업데이트
        updateScheduleStatus(configId, "STOPPED");
        
        logger.info("스케줄 중지: configId={}", configId);
    }
    
    /**
     * Cron 방식 스케줄링
     */
    private ScheduledFuture<?> scheduleCronTask(ApiConfigVO configVO) {
        try {
            CronExpression cronExpression = new CronExpression(configVO.getCronExpression());
            
            return scheduler.scheduleAtFixedRate(() -> {
                try {
                    Date now = new Date();
                    Date nextExecution = cronExpression.getNextValidTimeAfter(now);
                    
                    if (nextExecution != null) {
                        long delay = nextExecution.getTime() - now.getTime();
                        if (delay <= 60000) { // 1분 이내면 실행
                            executeScheduledApi(configVO);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Cron 스케줄 실행 오류: {}", configVO.getApiName(), e);
                }
            }, 0, 1, TimeUnit.MINUTES);
            
        } catch (Exception e) {
            logger.error("Cron 표현식 오류: {}", configVO.getCronExpression(), e);
            return null;
        }
    }
    
    /**
     * 간격 방식 스케줄링
     */
    private ScheduledFuture<?> scheduleIntervalTask(ApiConfigVO configVO) {
        return scheduler.scheduleAtFixedRate(() -> {
            try {
                executeScheduledApi(configVO);
            } catch (Exception e) {
                logger.error("간격 스케줄 실행 오류: {}", configVO.getApiName(), e);
            }
        }, 0, configVO.getIntervalMinutes(), TimeUnit.MINUTES);
    }
    
    /**
     * 스케줄된 API 실행
     */
    @Override
    public void executeScheduledApi(ApiConfigVO configVO) throws Exception {
	        Date startDate = new Date();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        
	        ScheduleHistoryVO historyVO = new ScheduleHistoryVO();
	        historyVO.setConfigId(configVO.getConfigId());
	        historyVO.setApiName(configVO.getApiName());
	        historyVO.setExecuteStartTime(sdf.format(startDate));  // String 형태로 변환해서 설정
	        
	        long startTime = System.currentTimeMillis();
	        int processedCount = 0; 
	        
	        try {
	            logger.info("스케줄 API 연계 시작: {}", configVO.getApiName());
	            
	            // API 연계 실행
	            processedCount = publicDataService.executeApiConnection(configVO);
	            Date endDate = new Date();
	            long endTime = System.currentTimeMillis();
	            
	            // 성공 정보 업데이트
	            historyVO.setStatus("SUCCESS");
	            historyVO.setMessage("정상 처리 완료");
	            historyVO.setExecuteEndTime(sdf.format(endDate));  // String 형태로 변환해서 설정
	            historyVO.setExecuteDuration(endTime - startTime);
	            historyVO.setProcessedCount(processedCount);  // 처리 건수 (필요시 실제 값으로 설정)
	            historyVO.setErrorCount(0);     // 오류 건수
	            
	            // 다음 실행시간 계산
	            String nextExecuteTime = calculateNextExecuteTime(configVO);
	            
	            // API 설정의 마지막 실행 정보 업데이트
	            ApiConfigVO updateVO = new ApiConfigVO();
	            updateVO.setConfigId(configVO.getConfigId());
	            updateVO.setLastExecuteTime(sdf.format(endDate));
	            updateVO.setLastExecuteStatus("SUCCESS");
	            updateVO.setLastExecuteMsg("정상 처리 완료");
	            updateVO.setNextExecuteTime(nextExecuteTime);
	            apiConfigDAO.updateLastExecuteInfo(updateVO);
	            
	            logger.info("스케줄 API 연계 완료: {} ({}ms), 다음 실행시간: {}", 
	                       configVO.getApiName(), (endTime - startTime), processedCount, nextExecuteTime);
	            
	        } catch (Exception e) {
	            Date endDate = new Date();
	            long endTime = System.currentTimeMillis();
	            
	            // 실패 시에도 다음 실행시간 계산
	            String nextExecuteTime = calculateNextExecuteTime(configVO);
	            
	            // 실패 정보 업데이트
	            historyVO.setStatus("FAIL");
	            historyVO.setMessage("처리 실패: " + e.getMessage());
	            historyVO.setExecuteEndTime(sdf.format(endDate));  // String 형태로 변환해서 설정
	            historyVO.setExecuteDuration(endTime - startTime);
	            historyVO.setProcessedCount(processedCount);  // 처리 건수
	            historyVO.setErrorCount(1);     // 오류 건수
	            
	            // API 설정의 마지막 실행 정보 업데이트
	            ApiConfigVO updateVO = new ApiConfigVO();
	            updateVO.setConfigId(configVO.getConfigId());
	            updateVO.setLastExecuteTime(sdf.format(endDate));
	            updateVO.setLastExecuteStatus("FAIL");
	            updateVO.setLastExecuteMsg("처리 실패: " + e.getMessage());
	            updateVO.setNextExecuteTime(nextExecuteTime);
	            apiConfigDAO.updateLastExecuteInfo(updateVO);
	            
	            logger.error("스케줄 API 연계 실패: {}, 다음 실행시간: {}", 
	                        configVO.getApiName(), nextExecuteTime, e);
	            throw e;
	        } finally {
	            // 실행 이력 저장
	            scheduleHistoryDAO.insertScheduleHistory(historyVO);
	        }
    	}
    
    /**
     * 다음 실행시간 계산
     */
    private String calculateNextExecuteTime(ApiConfigVO configVO) {
    	try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            Date nextTime = null;
            
            if ("CRON".equals(configVO.getScheduleType())) {
                // CRON 표현식을 사용한 다음 실행시간 계산
                CronExpression cronExpression = new CronExpression(configVO.getCronExpression());
                nextTime = cronExpression.getNextValidTimeAfter(now);
                
            } else if ("INTERVAL".equals(configVO.getScheduleType())) {
                // 간격 기반 다음 실행시간 계산
                long intervalMillis = configVO.getIntervalMinutes() * 60 * 1000;
                nextTime = new Date(now.getTime() + intervalMillis);
            }
            
            if (nextTime != null) {
                return sdf.format(nextTime);
            }
            
        } catch (Exception e) {
            logger.error("다음 실행시간 계산 오류: configId={}, scheduleType={}", 
                        configVO.getConfigId(), configVO.getScheduleType(), e);
        }
        
        return null;
    }

    
    /**
     * 스케줄 실행 이력 조회
     */
    @Override
    public List<ScheduleHistoryVO> getScheduleHistoryList(ScheduleHistoryVO searchVO) throws Exception {
        return scheduleHistoryDAO.selectScheduleHistoryList(searchVO);
    }

    /**
     * 스케줄 상태 업데이트
     */
    private void updateScheduleStatus(String configId, String status) throws Exception {
        ApiConfigVO updateVO = new ApiConfigVO();
        updateVO.setConfigId(configId);
        updateVO.setScheduleStatus(status);
        apiConfigDAO.updateScheduleStatus(updateVO);
    }
    
    /**
     * 스케줄 실행 상태 확인
     */
    public boolean isScheduleRunning(String configId) {
        return runningSchedules.contains(configId);
    }


	@Override
	public ScheduleHistoryVO selectHistory(String historyId) {
		return scheduleHistoryDAO.selectHistory(historyId);
	}


	@Override
	public ScheduleHistoryVO historyStatistics(String configId) {
		return scheduleHistoryDAO.historyStatistics(configId);
	}


	@Override
	public int selectConfigId(String configId) {
		return scheduleHistoryDAO.selectConfigId(configId);
	}


	@Override
	public int updateApiName(String configId, String apiName) {
		return scheduleHistoryDAO.updateApiName(configId, apiName);
	}
    
}
