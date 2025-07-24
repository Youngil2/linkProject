package com.practice.kyi.board.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.practice.kyi.board.dao.vo.BoardFileVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class BoardFileDAO extends EgovAbstractMapper {

    /**
     * 파일 정보 저장
     */
    public void insertBoardFile(BoardFileVO boardFileVO) {
        this.insert("BoardFileDAO.insertBoardFile", boardFileVO);
    }

    /**
     * 파일 정보 조회
     */
    public BoardFileVO selectBoardFile(String fileSeq) {
        return this.selectOne("BoardFileDAO.selectBoardFile", fileSeq);
    }

    /**
     * 게시글의 파일 목록 조회
     */
    public List<BoardFileVO> selectBoardFileList(String boardSeq) {
        return this.selectList("BoardFileDAO.selectBoardFileList", boardSeq);
    }

    /**
     * 파일 정보 수정
     */
    public void updateBoardFile(BoardFileVO boardFileVO) {
        this.update("BoardFileDAO.updateBoardFile", boardFileVO);
    }

    /**
     * 파일 정보 삭제
     */
    public void deleteBoardFile(String fileSeq) {
        this.delete("BoardFileDAO.deleteBoardFile", fileSeq);
    }

    /**
     * 게시글의 모든 파일 삭제
     */
    public void deleteBoardFilesByBoardSeq(String boardSeq) {
        this.delete("BoardFileDAO.deleteBoardFilesByBoardSeq", boardSeq);
    }

    /**
     * 파일 시퀀스 생성을 위한 다음 번호 조회
     */
    public int selectNextFileSeq() {
        return this.selectOne("BoardFileDAO.selectNextFileSeq");
    }
}