package com.practice.kyi.admin.dao.vo;

import com.practice.kyi.common.dao.vo.CommonVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubMenuVO extends CommonVO{

	private static final long serialVersionUID = 1L;
	
	private String subMenuSeq;
	private String topMenuNm;
	private String subMenu;
	private String subMenuNm;
	private int subMenuOrder;
	private String deleteYn;
}