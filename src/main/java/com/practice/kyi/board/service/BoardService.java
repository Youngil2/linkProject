package com.practice.kyi.board.service;

import java.util.List;

import com.practice.kyi.board.dao.vo.BoardVO;
import com.practice.kyi.common.dao.vo.FaqVO;

public interface BoardService {
	
	
	String subMenuNm(String subMenu);
	
	int insertBoard(BoardVO vo);
	int updateBoard(BoardVO vo);
	BoardVO boardChk(String boardSeq);
	
	BoardVO boardForm(String boardSeq);

	List<BoardVO> selectBoardList(BoardVO vo);
	List<FaqVO> selectfaqList(FaqVO vo);
	
	List<FaqVO> findFaq(FaqVO vo);
}
