package com.practice.kyi.admin_link.dao;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.practice.kyi.admin_link.dao.vo.PublicDataVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class PublicDataDAO extends EgovAbstractMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(PublicDataDAO.class);

    /**
     * 테이블 존재 여부 확인
     */
    public int checkTableExists(PublicDataVO publicDataVO) throws Exception {
        String fullTableName = publicDataVO.getFullTableName();
        logger.info("table name : "+fullTableName);
        try {
            if (fullTableName == null || fullTableName.trim().isEmpty()) {
                throw new IllegalArgumentException("테이블명은 필수입니다.");
            }

            PublicDataVO vo = new PublicDataVO();
            vo.parseFullTableName(fullTableName.trim());

            return selectOne("publicData.checkTableExists", vo);

        } catch (Exception e) {
            throw new Exception("테이블 존재 여부 확인 중 오류 발생: " + e.getMessage(), e);
        }
    }
    /**
     * 테이블 컬럼 정보 조회
     */
    public List<Map<String, Object>> selectTableColumns(PublicDataVO publicDataVO) throws Exception {
        try {
            String fullTableName = publicDataVO.getFullTableName();
            if (fullTableName == null || fullTableName.trim().isEmpty()) {
                throw new IllegalArgumentException("테이블명은 필수입니다.");
            }

            // PublicDataVO 객체 자체를 전달 (schemaName, tableName이 이미 분리되어 있음)
            return selectList("publicData.selectTableColumns", publicDataVO);

        } catch (Exception e) {
            throw new Exception("테이블 컬럼 정보 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }
    

    /**
     * 테이블 데이터 건수 확인
     */
    public int checkTableDataCount(PublicDataVO publicDataVO) throws Exception {
        String fullTableName = publicDataVO.getFullTableName();
        try {
            if (fullTableName == null || fullTableName.trim().isEmpty()) {
                throw new IllegalArgumentException("테이블명은 필수입니다.");
            }

            PublicDataVO vo = new PublicDataVO();
            vo.parseFullTableName(fullTableName.trim());
            logger.info("테이블 데이터 수 = "+selectOne("publicData.checkTableDataCount", vo));
            return selectOne("publicData.checkTableDataCount", vo);

        } catch (Exception e) {
            throw new Exception("테이블 데이터 건수 확인 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * APPEND INSERT만 수행 (TRUNCATE 없이 데이터 추가만)
     */
    @Transactional
    public void appendInsertData(PublicDataVO tableInfo, List<PublicDataVO> dataList) throws Exception {
        try {
            logger.info("APPEND INSERT 시작 - 테이블: {}, 삽입 예정 건수: {}", 
                       tableInfo.getFullTableName(), dataList.size());
            
            // 데이터 INSERT (Map 방식 사용)
            int insertedCount = 0;
            for (PublicDataVO data : dataList) {
                if (data.getDataMap() != null && !data.getDataMap().isEmpty()) {
                    insert("publicData.insertDynamicDataMap", data);
                    insertedCount++;
                }
            }
            
            logger.info("APPEND INSERT 완료 - 테이블: {}, 삽입 건수: {}", 
                       tableInfo.getFullTableName(), insertedCount);

        } catch (Exception e) {
            logger.error("APPEND INSERT 처리 중 오류: {}", e.getMessage());
            throw new Exception("APPEND INSERT 처리 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 조건부 TRUNCATE + INSERT 실행 (트랜잭션 처리)
     * - 테이블에 데이터가 있으면 TRUNCATE 후 INSERT
     * - 테이블이 비어있으면 바로 INSERT
     */
    @Transactional
    public void conditionalTruncateAndInsertData(PublicDataVO tableInfo, List<PublicDataVO> dataList) throws Exception {
        try {
            // 1. 테이블 데이터 건수 확인
            int currentDataCount = checkTableDataCount(tableInfo);
            logger.info("테이블 {} 현재 데이터 건수: {}", tableInfo.getFullTableName(), currentDataCount);
            
            // 2. 데이터가 있으면 TRUNCATE 실행
            if (currentDataCount > 0) {
                logger.info("기존 데이터가 존재하여 TRUNCATE 실행: {}", tableInfo.getFullTableName());
                delete("publicData.truncateTable", tableInfo);
            } else {
                logger.info("기존 데이터가 없어 TRUNCATE 생략: {}", tableInfo.getFullTableName());
            }

            // 3. 데이터 INSERT (Map 방식 사용)
            int insertedCount = 0;
            for (PublicDataVO data : dataList) {
                if (data.getDataMap() != null && !data.getDataMap().isEmpty()) {
                    insert("publicData.insertDynamicDataMap", data);
                    insertedCount++;
                }
            }
            
            logger.info("데이터 INSERT 완료 - 테이블: {}, 삽입 건수: {}", 
                       tableInfo.getFullTableName(), insertedCount);

        } catch (Exception e) {
            logger.error("조건부 TRUNCATE + INSERT 처리 중 오류: {}", e.getMessage());
            throw new Exception("조건부 TRUNCATE + INSERT 처리 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 메서드명 유지 - 내부적으로 조건부 TRUNCATE 사용
     */
    @Transactional
    public void truncateAndInsertData(PublicDataVO tableInfo, List<PublicDataVO> dataList) throws Exception {
        // 조건부 TRUNCATE 메서드로 위임
        conditionalTruncateAndInsertData(tableInfo, dataList);
    }
}