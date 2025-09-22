package com.kh.mvidia.integratedAtt.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Attendance {
	
	private String attNo;
	private String empNo;
	private String empName;
	private String hrmNo;
	private String hrmName;
	private String attDate;
	private String arrivingTime;
	private String leavingTime;
	private String attStatus;
	private String attStatusName;

}
