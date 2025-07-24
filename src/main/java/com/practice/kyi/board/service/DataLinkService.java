package com.practice.kyi.board.service;

import java.util.List;
import java.util.Map;

import com.practice.kyi.admin_link.dao.vo.PublicDataVO;

public interface DataLinkService {
	
		List<Map<String, Object>> getDataListWithoutSyncDt(String schema, String tableName, PublicDataVO vo)  throws Exception;
		/**
		 * 테이블 컬럼명 조회
		 * @param schema 스키마명
		 * @param tableName 테이블명
		 * @return 컬럼명 리스트
		 * @throws Exception
		 */
		public List<Map<String, Object>> getColumnNames(String schema, String tableName) throws Exception;
		
		// 전체 데이터 조회용 (페이징 없음)
		public List<Map<String, Object>> getAllDataList(String schema, String tableName, PublicDataVO vo) throws Exception;
		
		public String selectApiNm(String targetTable);
}
