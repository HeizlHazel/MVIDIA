package com.kh.mvidia.message.model.service;

import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.message.model.dao.MessageDao;
import com.kh.mvidia.message.model.vo.MessageBox;
import com.kh.mvidia.message.model.vo.MessageRcpt;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private MessageDao messageDao;

    @Override
    public List<Map<String, Object>> getInboxMessages(String receiverNo, String filter, int offset, int pageSize) {
        Map<String, Object> param = Map.of(
                "receiverNo", receiverNo,
                "filter", filter,
                "offset", offset,
                "pageSize", pageSize
        );
        return messageDao.selectInboxMessages(sqlSession, param);
    }

    @Override
    public int getInboxMessageCount(String receiverNo, String filter) {
        Map<String, Object> param = Map.of("receiverNo", receiverNo, "filter", filter);
        return messageDao.selectInboxMessageCount(sqlSession, param);
    }

    @Transactional
    @Override
    public Map<String, Object> getMessageDetail(String msgId, String receiverNo) {
        // 1. 메시지 상세 조회
        Map<String, Object> message = messageDao.selectMessageDetail(sqlSession, msgId, receiverNo);

        // 2. 읽음 처리 실행
        Map<String, Object> param = new HashMap<>();
        param.put("msgId", msgId);
        param.put("receiverNo", receiverNo);

        int updated = messageDao.updateReadStatus(sqlSession, param);

        System.out.println("읽음 처리 실행 - msgId=" + msgId + ", receiverNo=" + receiverNo + ", updated=" + updated);

        return message;
    }

    @Override
    public boolean markAsRead(String msgId, String receiverNo) {
        Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
        return messageDao.updateReadStatus(sqlSession, param) > 0;
    }

    @Override
    public Map<String, Object> toggleImportant(String msgId, String receiverNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
            String currentStatus = messageDao.selectImportantStatus(sqlSession, param);

            String newStatus = "Z".equals(currentStatus) ? "N" : "Z";
            param = Map.of("msgId", msgId, "receiverNo", receiverNo, "importYn", newStatus);

            int result = messageDao.updateImportantStatus(sqlSession, param);

            if (result > 0) {
                response.put("success", true);
                response.put("important", "Z".equals(newStatus));
                response.put("message", "Z".equals(newStatus) ? "중요 표시되었습니다." : "중요 표시가 해제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "중요 표시 변경에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("중요 표시 토글 중 오류", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }

        return response;
    }

    @Override
    public Map<String, Object> deleteMessage(String msgId, String receiverNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
            int result = messageDao.deleteMessage(sqlSession, param);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "메시지가 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "메시지 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("메시지 삭제 중 오류", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }

        return response;
    }

    @Transactional
    public Map<String, Object> sendMessage(Map<String, Object> messageData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. BOX insert
            messageDao.insertMessageBox(sqlSession, messageData);

            // 2. 방금 생성된 MSG_ID 가져오기
            String msgId = messageDao.selectCurrMessageId(sqlSession);
            // or selectNextMessageId / SEQ_MSG.CURRVAL
            messageData.put("msgId", msgId);

            // 3. RCPT insert
            @SuppressWarnings("unchecked")
            List<String> receivers = (List<String>) messageData.get("receivers");

            for (String receiverNo : receivers) {
                Map<String, Object> rcptData = new HashMap<>();
                rcptData.put("msgId", msgId);
                rcptData.put("receiverNo", receiverNo);
                messageDao.insertMessageRcpt(sqlSession, rcptData);

            }

            response.put("success", true);
            response.put("message", "메시지가 발송되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "발송 실패: " + e.getMessage());
        }

        return response;
    }


    @Override
    public List<Map<String, Object>> getOutboxMessages(String senderNo, int offset, int pageSize) {
        Map<String, Object> param = Map.of("senderNo", senderNo, "offset", offset, "pageSize", pageSize);
        return messageDao.selectOutboxMessages(sqlSession, param);
    }

    @Override
    public int getOutboxMessageCount(String senderNo) {
        return messageDao.selectOutboxMessageCount(sqlSession, senderNo);
    }

    @Override
    public Map<String, Object> getSentMessageDetail(String msgId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 발송 메시지 기본 정보 조회
            Map<String, Object> message = messageDao.selectSentMessageDetail(sqlSession, msgId);
            if (message != null) {
                result.putAll(message);

                // 2. 메시지 수신자 조회
                List<Map<String, Object>> receivers = messageDao.selectMessageReceivers(sqlSession, msgId);
                result.put("receivers", receivers);
            }
        } catch (Exception e) {
            log.error("발송 메시지 상세 조회 오류", e);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> searchEmployees(String keyword) {
        List<Employee> allEmployees = messageDao.findAllEmployees(sqlSession);

        return allEmployees.stream()
                .filter(emp -> keyword == null || keyword.isEmpty()
                        || emp.getEmpNo().contains(keyword)
                        || (emp.getEmpLName() != null && emp.getEmpLName().contains(keyword))
                        || (emp.getEmpName() != null && emp.getEmpName().contains(keyword))
                        || ((emp.getEmpLName() != null ? emp.getEmpLName() : "")
                        + (emp.getEmpName() != null ? emp.getEmpName() : ""))
                        .contains(keyword)
                )
                .map(emp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("empNo", emp.getEmpNo());
                    map.put("empName", (emp.getEmpLName() != null ? emp.getEmpLName() : "")
                            + (emp.getEmpName() != null ? emp.getEmpName() : ""));
                    map.put("deptName", emp.getDeptName());
                    map.put("jobName", emp.getJobName());
                    return map;
                })
                .toList();
    }

    @Override
    public void markAsRead(String msgId) {
        Map<String, Object> param = Map.of("msgId", msgId);
        messageDao.updateReadStatus(sqlSession, param); // 기존 DAO 메서드 사용
    }

}