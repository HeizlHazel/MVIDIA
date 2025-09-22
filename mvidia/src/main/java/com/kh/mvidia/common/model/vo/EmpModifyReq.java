package com.kh.mvidia.common.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmpModifyReq {
	
	private String reqId;
	private String empNo;
	private String fieldName;
	private String oldValue;
	private String newValue;
	private String oldAtchId;
	private String newAtchId;
	private String reqDate;
	private String reqStatus;
	private String managerId;
	private String approveDate;
	private String rejectReason;
}
