package com.practice.kyi.admin_link.dao.vo;

import java.util.List;
import java.util.Map;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class PublicDataVO extends CommonVO {
	
	private static final long serialVersionUID = 1L;
	
    private String schemaName;          // 스키마명
    private String tableName;           // 테이블명
    private List<String> columnList;    // 컬럼명 리스트
    private List<Object> valueList;     // 값 리스트
    private Map<String, Object> dataMap; // 원본 데이터 맵
    
    // 기본 생성자
    public PublicDataVO() {}
    
    // 기존 생성자
    public PublicDataVO(String tableName, List<String> columnList, List<Object> valueList) {
        this.parseFullTableName(tableName);
        this.columnList = columnList;
        this.valueList = valueList;
    }
    
    /**
     * 스키마.테이블 형태의 문자열을 파싱하여 스키마명과 테이블명을 분리
     * @param fullTableName "스키마.테이블" 형태의 문자열 또는 "테이블"만
     */
    public void parseFullTableName(String fullTableName) {
        if (fullTableName == null || fullTableName.trim().isEmpty()) {
            throw new IllegalArgumentException("테이블명은 필수입니다.");
        }
        
        String[] parts = fullTableName.trim().split("\\.");
        
        if (parts.length == 1) {
            // 스키마가 없는 경우 - 기본 스키마 사용 또는 null로 설정
            this.schemaName = null; // 또는 "public" 등 기본값 설정
            this.tableName = parts[0];
        } else if (parts.length == 2) {
            // 스키마.테이블 형태
            this.schemaName = parts[0];
            this.tableName = parts[1];
        } else {
            throw new IllegalArgumentException("올바르지 않은 테이블명 형식입니다: " + fullTableName);
        }
    }
    
    /**
     * 전체 테이블명 반환 (스키마.테이블)
     */
    public String getFullTableName() {
        if (schemaName != null && !schemaName.trim().isEmpty()) {
            return schemaName + "." + tableName;
        }
        return tableName;
    }
    
    /**
     * 테이블명만 설정 (기존 방식 호환)
     */
    public void setTableName(String tableName) {
        if (tableName != null && tableName.contains(".")) {
            // "스키마.테이블" 형태면 파싱
            parseFullTableName(tableName);
        } else {
            // 테이블명만 있으면 그대로 설정
            this.tableName = tableName;
        }
    }
    
    
    @Override
    public String toString() {
        return "PublicDataVO{" +
                "schemaName='" + schemaName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", columnList=" + columnList +
                ", valueList=" + valueList +
                ", dataMap=" + dataMap +
                '}';
    }

}
