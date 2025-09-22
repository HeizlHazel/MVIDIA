package com.kh.mvidia.message.model.vo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MessageBox {
    private String msgId;
    private String senderNo;
    private String title;
    private String content;
    private String sendDate;
}
