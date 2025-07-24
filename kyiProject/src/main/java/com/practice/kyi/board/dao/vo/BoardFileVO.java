package com.practice.kyi.board.dao.vo;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoardFileVO extends CommonVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String fileSeq;
	private String boardSeq;
	private String filePath;
	private String fileNm;
	private long fileSize;
	private String fileExt; //파일 확장자
	private String originalFileNm; //원본파일명
	
	private String registUser;
    private String registDate;
    private String updateUser;
    private String updateDate;
    
}
