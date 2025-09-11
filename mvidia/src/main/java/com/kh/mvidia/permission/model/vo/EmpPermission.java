package com.kh.mvidia.permission.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmpPermission {

    private String userId;
    private String perm_code;
    private String isGranted;

}
