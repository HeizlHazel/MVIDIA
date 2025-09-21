package com.kh.mvidia.permission.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SystemLog {

    private String logId;
    private String logType;
    private String targetId;
    private String targetName;
    private String actorId;
    private String action;
    private String createdAt;
    private String reason;
    private String deptName;
    private String notioniDocId;

}
