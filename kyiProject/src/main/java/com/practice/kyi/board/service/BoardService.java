package com.practice.kyi.board.service;

import java.util.List;

import com.practice.kyi.board.dao.vo.BoardVO;

public interface BoardService {
	
	
	String subMenuNm(String subMenu);
	
	int insertBoard(BoardVO vo);
	int updateBoard(BoardVO vo);
	BoardVO boardChk(String boardSeq);
	
	BoardVO boardForm(String boardSeq);

	List<BoardVO> selectBoardList(BoardVO vo);
}
