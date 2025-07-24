package com.practice.kyi.admin_link.dao.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiConfigVO extends CommonVO {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String configId;
    private String apiName;
    private String baseUrl;
    private String serviceKey;
    private String responseFormat; // JSON, XML
    private String targetTable;
    private String useYn;
    private String registDate;
    private String upatedDate;
    
    @JsonProperty("mappingConfigJson")
    private String mappingConfig; // JSON 형태의 매핑 설정
    @JsonProperty("parameterConfigJson")
    private String parameterConfig;
    
    // 스케줄링 관련 필드 추가
    private String scheduleYn;          // 스케줄 사용 여부 (Y/N)
    private String scheduleType;        // 스케줄 타입 (CRON, INTERVAL)
    private String cronExpression;      // cron 표현식
    private Integer intervalMinutes;    // 간격(분)
    private String scheduleDesc;        // 스케줄 설명
    private String nextExecuteTime;     // 다음 실행 시간
    private String lastExecuteTime;     // 마지막 실행 시간
    private String lastExecuteStatus;   // 마지막 실행 상태 (SUCCESS, FAIL)
    private String lastExecuteMsg;      // 마지막 실행 메시지
    
    // 실행 상태 추가
    private String scheduleStatus;      // RUNNING, STOPPED
    private String isRunning;
    

}
