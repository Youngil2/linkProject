package com.practice.kyi.board.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.practice.kyi.admin_link.dao.vo.PublicDataVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class DataLinkDAO extends EgovAbstractMapper{
	
	/**
	 * 테이블 컬럼명 조회
	 * @param params schema, tableName
	 * @return 컬럼명 리스트
	 */
	public List<Map<String, Object>> getColumnNames(Map<String, String> params) {
	    return selectList("DataLink.getColumnNames", params);
	}

	
    /**
     * sync_dt를 제외한 컬럼 리스트 조회
     * @param params schema, tableName
     * @return 컬럼 리스트 (콤마로 구분된 문자열)
     */
    public String getColumnList(Map<String, Object> params) {
        return selectOne("DataLink.getColumnList", params);
    }
    
    /**
     * 동적 컬럼으로 데이터 조회
     * @param params columnList, target_table
     * @return 조회된 데이터 리스트
     */
    public List<Map<String, Object>> selectDataList(Map<String, Object> params) {
        return selectList("DataLink.selectDataList", params); // XML은 그대로 사용
    }
    
    public List<Map<String, Object>> selectAllDataList(Map<String, Object> params){
    	return selectList("DataLink.selectAllDataList", params);
    }
    
    public String selectApiNm(String targetTable) {
    	return selectOne("DataLink.selectApiNm",targetTable);
    }
}
