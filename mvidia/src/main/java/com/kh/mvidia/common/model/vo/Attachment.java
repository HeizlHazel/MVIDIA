package com.kh.mvidia.common.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Attachment {
	
	private String atchId;
	private String refType;
	private String originName;
	private String changeName;
	private String uploadDate;
	private String uploadEmpNo;
	private String fileStatus;

}
