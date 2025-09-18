package com.kh.mvidia.permission.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Permission {

    private String permCode;
    private String permName;
    private String isGranted;

    private String userId;

}
