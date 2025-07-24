package com.practice.kyi.admin.dao.vo;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TopMenuVO extends CommonVO{
	
	private static final long serialVersionUID = 1L;
	
	private String topMenuSeq;
	private String topMenu;
	private String topMenuNm;
	private int topMenuOrder;
	private String deleteYn;
}