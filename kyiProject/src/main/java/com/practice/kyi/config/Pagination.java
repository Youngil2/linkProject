package com.practice.kyi.config;

import com.practice.kyi.common.dao.vo.CommonVO;

public class Pagination {
    private int currentPageNo;          // 현재 페이지 번호
    private int recordCountPerPage;     // 한 페이지당 게시되는 게시물 수
    private int pageSize;               // 페이지 리스트에 게시되는 페이지 수
    private int totalRecordCount;       // 전체 게시물 수

    private int firstPageNoOnPageList;  // 페이지 리스트의 첫 페이지 번호
    private int lastPageNoOnPageList;   // 페이지 리스트의 마지막 페이지 번호
    private int firstRecordIndex;       // SQL 조건절에 사용되는 시작 rownum
    private int lastRecordIndex;        // SQL 조건절에 사용되는 끝 rownum

    private boolean xprev;              // 이전 버튼
    private boolean xnext;              // 다음 버튼

    // Getters and setters
    public int getCurrentPageNo() {
        return currentPageNo;
    }
    public void setCurrentPageNo(int currentPageNo) {
        this.currentPageNo = currentPageNo;
    }
    public int getRecordCountPerPage() {
        return recordCountPerPage;
    }
    public void setRecordCountPerPage(int recordCountPerPage) {
        this.recordCountPerPage = recordCountPerPage;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public int getTotalRecordCount() {
        return totalRecordCount;
    }
    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    // 페이지 리스트의 첫 페이지 번호 계산
    public int getFirstPageNoOnPageList() {
        firstPageNoOnPageList = ((currentPageNo - 1) / pageSize) * pageSize + 1;
        return firstPageNoOnPageList;
    }

    // 페이지 리스트의 마지막 페이지 번호 계산
    public int getLastPageNoOnPageList() {
        lastPageNoOnPageList = Math.min(getFirstPageNoOnPageList() + pageSize - 1, getRealEnd());
        return lastPageNoOnPageList;
    }

    // 페이징 SQL 시작 rownum
    public int getFirstRecordIndex() {
        firstRecordIndex = (currentPageNo - 1) * recordCountPerPage;
        return firstRecordIndex;
    }

    // 페이징 SQL 끝 rownum
    public int getLastRecordIndex() {
        lastRecordIndex = Math.min(getFirstRecordIndex() + recordCountPerPage - 1, totalRecordCount - 1);
        return lastRecordIndex;
    }

    // 이전 버튼 여부
    public boolean getXprev() {
        xprev = getFirstPageNoOnPageList() > 1;
        return xprev;
    }

    // 다음 버튼 여부
    public boolean getXnext() {
        xnext = getLastPageNoOnPageList() < getRealEnd();
        return xnext;
    }

    // 전체 페이지 수 계산
    public int getRealEnd() {
        return (int) Math.ceil((double) totalRecordCount / recordCountPerPage);
    }
    
    // CommonVO의 pageIndex와 pageUnit을 바탕으로 Pagination 설정
    public void setPagination(CommonVO commonVO) {
        this.currentPageNo = commonVO.getPageIndex();  // 현재 페이지 번호
        this.recordCountPerPage = commonVO.getPageUnit();  // 한 페이지당 게시되는 게시물 수
        this.pageSize = 10;  // 페이지 리스트에 보여줄 페이지 수 (필요시 수정)
        this.totalRecordCount = commonVO.getTotalCount();  // 전체 레코드 수

        // 계산된 값으로 Pagination 값 설정
        commonVO.setFirstIndex(this.getFirstRecordIndex());  // 첫 번째 레코드 인덱스 설정
        commonVO.setLastIndex(this.getLastRecordIndex());    // 마지막 레코드 인덱스 설정
    }

}
