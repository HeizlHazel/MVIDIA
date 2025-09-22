package com.kh.mvidia.message.model.service;

import java.util.List;
import java.util.Map;

public interface MessageService {

    /**
     * 수신 메시지 목록 조회
     */
    List<Map<String, Object>> getInboxMessages(String receiverNo, String filter, int offset, int pageSize);

    /**
     * 수신 메시지 개수 조회
     */
    int getInboxMessageCount(String receiverNo, String filter);

    /**
     * 읽음 상태 업데이트
     */
    boolean markAsRead(String msgId, String receiverNo);

    /**
     * 중요 표시 토글
     */
    Map<String, Object> toggleImportant(String msgId, String receiverNo);

    /**
     * 메시지 삭제
     */
    Map<String, Object> deleteMessage(String msgId, String receiverNo);

    /**
     * 메시지 발송
     */
    Map<String, Object> sendMessage(Map<String, Object> messageData);

    /**
     * 발신 메시지 목록 조회
     */
    List<Map<String, Object>> getOutboxMessages(String senderNo, int offset, int pageSize);

    /**
     * 발신 메시지 개수 조회
     */
    int getOutboxMessageCount(String senderNo);

    /**
     * 사원 검색
     */
    List<Map<String, Object>> searchEmployees(String keyword);

    /**
     * 발송 메시지 상세 조회 (발신함용)
     */
    Map<String, Object> getMessageDetail(String msgId, String receiverNo);

    Map<String, Object> getSentMessageDetail(String msgId);

    // 불필요한 2번째 파라미터 제거
    void markAsRead(String msgId);

}