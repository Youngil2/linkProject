package com.practice.kyi.config;

import com.practice.kyi.common.dao.vo.CommonVO;

public class PageUtil {
    public static Pagination setPagination(CommonVO commonVO) {
        Pagination pagination = new Pagination();
        
        // 현재 페이지 및 레코드 수 설정
        pagination.setCurrentPageNo(commonVO.getPageIndex());  // 현재 페이지 설정
        pagination.setRecordCountPerPage(commonVO.getPageUnit());
        pagination.setPageSize(commonVO.getPageSize());
        
        // 첫번째/마지막 인덱스 계산
        int firstIndex = (pagination.getCurrentPageNo() - 1) * pagination.getRecordCountPerPage()+1;
        commonVO.setFirstIndex(firstIndex);
        commonVO.setLastIndex(firstIndex + pagination.getRecordCountPerPage() - 1);
        
        return pagination;
    }
}
