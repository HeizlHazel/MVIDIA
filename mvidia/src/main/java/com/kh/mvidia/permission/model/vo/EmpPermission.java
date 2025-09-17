package com.kh.mvidia.permission.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EmpPermission {

    private String userId;
    private String permCode;
    private String isGranted;

}
