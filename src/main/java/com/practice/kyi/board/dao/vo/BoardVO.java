package com.practice.kyi.board.dao.vo;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardVO extends CommonVO{
	
	private static final long serialVersionUID = 1L;
	
	private String boardSeq;
	
	private String subMenu;
	
	private String subMenuNm;
	
	private String registDate;
	
	private String registUser;
	
	private String updateUser;
	
	private String updateDate;
	
	private String deleteYn;
	
	private String boardTitle;
	
	private String contents;
	
	private int openCount;
	
	private String secretYn;
}
