package com.practice.kyi.common.dao.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FaqVO extends CommonVO{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String faqSeq;
    private String question;
    private String answer;
    private String keywords;
    private String category;
    private int viewCount;
    private String useYn;
    private String registDate;
    private String registUser;
    private String updateDate;
    private String updateUser;
}