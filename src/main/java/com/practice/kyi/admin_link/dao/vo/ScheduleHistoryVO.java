package com.practice.kyi.admin_link.dao.vo;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleHistoryVO extends CommonVO{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long historyId;
    private String configId;
    private String apiName;
    private String executeTime;
    private String status;
    private String message;
    private Integer processedCount;
    private Integer errorCount;
    private String executeStartTime;
    private String executeEndTime;
    private Long executeDuration;

    private Double successRate; //성공률
    private Double executeDurationInSeconds; //실행 소요시간(초)
    private Double processingSpeed;  // 초당 처리 건수
}
