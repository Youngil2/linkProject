package com.practice.kyi.board.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.practice.kyi.board.dao.vo.BoardVO;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository
public class BoardDAO extends EgovAbstractMapper{
	
	//서브메뉴명 조회
	public String subMenuNm(String subMenu) {
		return this.selectOne("BoardDAO.subMenuNm",subMenu);
	}
	//게시글 등록
	public int insertBoard(BoardVO vo) {
		return this.insert("BoardDAO.insertBoard",vo);
	}	
	//게시글 수정
	public int updateBoard(BoardVO vo) {
		return this.update("BoardDAO.updateBoard", vo);
	}
	//권한체크
	public BoardVO boardChk(String boardSeq) {
		return this.selectOne("BoardDAO.boardChk",boardSeq);
	}
	//게시판 리스트 호출
	public List<BoardVO> boardList(BoardVO vo){
		return this.selectList("BoardDAO.boardList", vo);
	}
	//게시판 폼 호출
	public BoardVO boardForm(String boardSeq) {
		return this.selectOne("BoardDAO.boardForm",boardSeq);
	}
	
}
