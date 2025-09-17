package com.kh.mvidia.integratedAtt.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Vacation {
	
	private String vaId;
	private String empNo;
	private String empName;
	private String authNo;
	private String authName;
	private String vaCategory;
	private String vaName;
	private String vaStart;
	private String vaEnd;
	private String vaDays;
	private String vaReason;
	private String alterEmp;
	private String alterEmpName;
	private String vaStatus;
	private String vaStatusName;

}
