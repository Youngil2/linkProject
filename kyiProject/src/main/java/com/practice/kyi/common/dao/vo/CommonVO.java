package com.practice.kyi.common.dao.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommonVO implements Serializable{

	private static final long serialVersionUID = 1L;
	
    private int rn;
    
    private String searchType = "";

    /** 통합검색키워드 */
    private String totalSearchKeyword = "";

    /** 검색키워드 */
    private String searchKeyword = "";

    /** 검색사용여부 */
    private String searchUseYn = "";

    /** 현재페이지 */
    private int pageIndex = 1;

    /** 페이지갯수 */
    private int pageUnit = 10;

    /** 페이지사이즈 */
    private int pageSize = 10;

    /** 첫페이지 */
    private int firstIndex = 1;

    /** 마지막페이지 */
    private int lastIndex = 1;

    /** 표시할페이지 */
    private int recordCountPerPage = 10;
    
    /** 전체 카운트 */
    private int totalCount = 0;
    private Integer[] pageUnitList = {10, 20, 50, 100, 1000};
    
    /** 이전, 다음 버튼 */
	private boolean prev, next;
	
    public void setSerchInfo(CommonVO commonVO) {
        this.setPageIndex(commonVO.getPageIndex());
        this.setPageUnit(commonVO.getPageUnit());
        this.setSearchType(commonVO.getSearchType());
        this.setSearchKeyword(commonVO.getSearchKeyword());
    }

}
