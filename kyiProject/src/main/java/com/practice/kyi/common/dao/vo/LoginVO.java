package com.practice.kyi.common.dao.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginVO extends CommonVO{

	private static final long serialVersionUID = 1L;
	
	private String memberId;
	private String memberPw;
	private String email;
	private String memberRole;
	private String memberName;
	private String memberCompany;
	private String certifyYn;
	private String approveYn;
	private String blockYn;
	private String registDate;
	private String approveDate;

}
