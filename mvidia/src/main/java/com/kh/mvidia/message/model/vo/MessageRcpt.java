package com.kh.mvidia.message.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageRcpt {
    private String receiverNo;
    private String msgId;
    private String readYn;
    private String importYn;
}
