package com.practice.kyi.board.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin_link.dao.vo.PublicDataVO;
import com.practice.kyi.board.dao.DataLinkDAO;
import com.practice.kyi.board.service.DataLinkService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("DataLinkService")
public class DataLinkServiceImpl extends EgovAbstractServiceImpl implements DataLinkService{
	
	@Autowired
	DataLinkDAO dataLinkDAO;
	
	@Override
	public List<Map<String, Object>> getColumnNames(String schema, String tableName) throws Exception {
	    // 파라미터 검증
	    if (schema == null || schema.trim().isEmpty()) {
	        throw new IllegalArgumentException("스키마명은 필수입니다.");
	    }
	    if (tableName == null || tableName.trim().isEmpty()) {
	        throw new IllegalArgumentException("테이블명은 필수입니다.");
	    }
	    
	    Map<String, String> params = new HashMap<>();
	    params.put("schema", schema);
	    params.put("tableName", tableName);
	    
	    List<Map<String, Object>> columnNames = dataLinkDAO.getColumnNames(params);
	    
	    if (columnNames == null || columnNames.isEmpty()) {
	        throw new Exception("해당 테이블의 컬럼 정보를 찾을 수 없습니다: " + schema + "." + tableName);
	    }
	    
	    return columnNames;
	}
	
    /**
     * sync_dt를 제외한 데이터 조회
     * @param schema 스키마명
     * @param tableName 테이블명
     * @return 조회된 데이터 리스트
     */
    public  List<Map<String, Object>> getDataListWithoutSyncDt(String schema, String tableName, PublicDataVO vo) throws Exception {
        // 1. 컬럼 리스트 조회
        Map<String, Object> params = new HashMap<>();
        params.put("schema", schema);
        params.put("tableName", tableName);

        String columnList = dataLinkDAO.getColumnList(params);

        if (columnList == null || columnList.trim().isEmpty()) {
            throw new Exception("해당 테이블의 컬럼 정보를 찾을 수 없습니다.");
        }

        // 2. 모든 필요한 값들을 params에 직접 추가
        params.put("columnList", columnList);
        params.put("target_table", schema + "." + tableName);
        
        // 페이징 및 검색 값들을 직접 추가
        params.put("firstIndex", vo.getFirstIndex());
        params.put("lastIndex", vo.getLastIndex());
        params.put("searchKeyword", vo.getSearchKeyword());
        params.put("searchType", vo.getSearchType());

        return dataLinkDAO.selectDataList(params);
    }

	@Override
	public List<Map<String, Object>> getAllDataList(String schema, String tableName, PublicDataVO vo) throws Exception {
	    // 1. 컬럼 리스트 조회
	    Map<String, Object> params = new HashMap<>();
	    params.put("schema", schema);
	    params.put("tableName", tableName);
	    
	    String columnList = dataLinkDAO.getColumnList(params);
	    if (columnList == null || columnList.trim().isEmpty()) {
	        throw new Exception("해당 테이블의 컬럼 정보를 찾을 수 없습니다.");
	    }
	    
	    // 2. 모든 필요한 값들을 params에 직접 추가
	    params.put("columnList", columnList);
	    params.put("target_table", schema + "." + tableName);
	    
	    // 검색 조건만 추가 (페이징 정보는 제외)
	    params.put("searchKeyword", vo.getSearchKeyword());
	    params.put("searchType", vo.getSearchType());
	    
	    // firstIndex와 lastIndex를 설정하지 않거나 null로 설정하여 전체 데이터 조회
	    // 또는 DAO에서 다른 메서드를 호출할 수도 있습니다.
	    
	    return dataLinkDAO.selectAllDataList(params); // 전체 데이터 조회용 DAO 메서드
	}

	@Override
	public String selectApiNm(String targetTable) {
		return dataLinkDAO.selectApiNm(targetTable);
	}
}
