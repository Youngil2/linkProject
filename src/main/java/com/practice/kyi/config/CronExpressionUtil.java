package com.practice.kyi.config;

import java.util.Date;

import org.apache.logging.log4j.core.util.CronExpression;


public class CronExpressionUtil {

    /**
     * Cron 표현식 검증
     */
    public static boolean isValidCronExpression(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Cron 표현식을 한글로 변환
     */
    public static String cronToKorean(String cronExpression) {
        try {
            // 기본적인 패턴 매칭으로 한글 설명 생성
            if ("0 0 9 * * *".equals(cronExpression)) {
                return "매일 오전 9시";
            } else if ("0 0 */6 * * *".equals(cronExpression)) {
                return "6시간마다";
            } else if ("0 0 0 * * MON".equals(cronExpression)) {
                return "매주 월요일 자정";
            }
            return cronExpression;
        } catch (Exception e) {
            return cronExpression;
        }
    }
    
    /**
     * 다음 실행 시간 계산
     */
    public static Date getNextExecutionTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date());
        } catch (Exception e) {
            return null;
        }
    }

}
