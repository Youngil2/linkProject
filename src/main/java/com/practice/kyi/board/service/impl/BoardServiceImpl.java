package com.practice.kyi.board.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.board.dao.BoardDAO;
import com.practice.kyi.board.dao.vo.BoardVO;
import com.practice.kyi.board.service.BoardService;
import com.practice.kyi.common.dao.ChatDAO;
import com.practice.kyi.common.dao.vo.FaqVO;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("BoardService")
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService{
	
	@Autowired
	BoardDAO boardDAO;
	
	@Autowired
	ChatDAO chatDAO;


	@Override
	public String subMenuNm(String subMenu) {
		return boardDAO.subMenuNm(subMenu);
	}


	@Override
	public int insertBoard(BoardVO vo) {
		return boardDAO.insertBoard(vo);
	}


	@Override
	public int updateBoard(BoardVO vo) {
		return boardDAO.updateBoard(vo);
	}


	@Override
	public BoardVO boardChk(String boardSeq) {
		return boardDAO.boardChk(boardSeq);
	}


	@Override
	public List<BoardVO> selectBoardList(BoardVO vo) {
		List<BoardVO> list = boardDAO.boardList(vo);
		return list;
	}


	@Override
	public BoardVO boardForm(String boardSeq) {
		return boardDAO.boardForm(boardSeq);
	}


	@Override
	public List<FaqVO> selectfaqList(FaqVO vo) {
		List<FaqVO> list = boardDAO.faqList(vo);
		return list;
	}


	@Override
	public List<FaqVO> findFaq(FaqVO vo) {
		List<FaqVO> list = chatDAO.findFaq(vo);
		return list;
	}
	
	

}
