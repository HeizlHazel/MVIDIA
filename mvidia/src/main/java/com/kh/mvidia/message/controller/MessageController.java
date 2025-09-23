package com.kh.mvidia.message.controller;

import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.message.model.dao.MessageDao;
import com.kh.mvidia.message.model.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private SqlSession sqlSession;

    // 수신함 페이지
    @GetMapping("/inbox")
    public String inboxPage(@RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "filter", defaultValue = "all") String filter,
                            HttpSession session, Model model) {

        // 로그인한 직원 사번
        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        if (loginEmp == null) {
            log.warn("로그인 정보가 세션에 없습니다. 수신함 접근 불가");
            return "redirect:/"; // 로그인 페이지로 리다이렉트
        }

        String receiverNo = loginEmp.getEmpNo();
        log.info("세션에서 가져온 empNo = {}", receiverNo);
        log.info("receiverNo 원본='{}', 길이={}", receiverNo, receiverNo.length());
        log.info("receiverNo='{}' (length={})", receiverNo, receiverNo.length());

        // 페이징 설정
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        // 수신 메시지 목록 조회
        List<Map<String, Object>> messages = messageService.getInboxMessages(receiverNo, filter, offset, pageSize);

        // 전체 개수 조회
        int totalCount = messageService.getInboxMessageCount(receiverNo, filter);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("filter", filter);

        return "message/inbox";
    }

    /**
     * 메시지 읽기
     */
    @GetMapping("/read")
    public String readMessage(@RequestParam("msgId") String msgId,
                              HttpSession session,
                              Model model) {

        // 임시 로그인 처리
        String empNo = (String) session.getAttribute("empNo");
        if (empNo == null) empNo = "22010001";

        // DB 조회
        Map<String, Object> message = messageService.getMessageDetail(msgId, empNo);

        // 테스트용: DB에서 가져온 데이터 없으면 임시 Map
        if (message == null) {
            message = new HashMap<>();
            message.put("msgId", "MSG0011");
            message.put("title", "스브");
            message.put("senderName", "하늘");
            message.put("sendDate", "2025-09-21");
            message.put("content", "테스트 메시지 내용");
        }

        model.addAttribute("message", message);
        return "read-ajax";
    }

    /**
     * 쪽지 작성 페이지
     */
    @GetMapping("/compose")
    public String composePage() {
        return "message/compose";
    }

    /**
     * 쪽지 발송
     */
    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> messageData,
                                           HttpSession session) {

        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        if (loginEmp == null) {
            log.warn("로그인 정보가 없습니다. 발송 불가");
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "로그인이 필요합니다.");
            return error;
        }

        String senderNo = loginEmp.getEmpNo();
        messageData.put("senderNo", senderNo);

        log.info("쪽지 발송 요청 - 발신자: {}, 제목: {}", senderNo, messageData.get("title"));

        Map<String, Object> result = messageService.sendMessage(messageData);
        log.info("쪽지 발송 결과: {}", result);

        return result;
    }

    /**
     * 중요 표시 토글
     */
    @PostMapping("/toggle-important")
    @ResponseBody
    public Map<String, Object> toggleImportant(@RequestParam("msgId") String msgId,
                                               @RequestParam("empNo") String empNo) {

        log.info("중요 표시 토글 요청 - msgId: {}, empNo: {}", msgId, empNo);
        return messageService.toggleImportant(msgId, empNo);
    }

    /**
     * 발신함 페이지
     */
    @GetMapping("/outbox")
    public String outboxPage(@RequestParam(value = "page", defaultValue = "1") int page,
                             HttpSession session, Model model) {

        Employee loginEmp = (Employee) session.getAttribute("loginEmp");

        String senderNo = loginEmp.getEmpNo();

        // 페이징 설정
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        // 발신 메시지 목록 조회
        List<Map<String, Object>> messages = messageService.getOutboxMessages(senderNo, offset, pageSize);

        // 전체 개수 조회
        int totalCount = messageService.getOutboxMessageCount(senderNo);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "message/outbox";
    }

    /**
     * 테스트용 - 전체 사원 조회
     */
    @GetMapping("/test-all-employees")
    @ResponseBody
    public List<Map<String, Object>> getAllEmployees() {
        log.info("전체 사원 조회 테스트");
        return messageService.searchEmployees(""); // 빈 키워드로 전체 조회
    }

    @GetMapping("/search-employees")
    @ResponseBody
    public List<Map<String, Object>> searchEmployees(@RequestParam("keyword") String keyword) {
        log.info("사원 검색 요청: {}", keyword);
        try {
            List<Map<String, Object>> result = messageService.searchEmployees(keyword);
            log.info("검색 결과 개수: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("사원 검색 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    /**
     * 발신 메시지 상세 조회
     */
    @GetMapping("/sent-detail")
    @ResponseBody
    public Map<String, Object> getSentMessageDetail(@RequestParam("msgId") String msgId) {
        log.info("발신 메시지 상세 조회 요청 - msgId: {}", msgId);

        Map<String, Object> result = new HashMap<>();
        try {
            result = messageService.getSentMessageDetail(msgId);
            if (result == null || result.isEmpty()) {
                result.put("success", false);
                result.put("message", "메시지를 찾을 수 없습니다.");
            } else {
                result.put("success", true);
            }
        } catch (Exception e) {
            log.error("발신 메시지 상세 조회 오류", e);
            result.put("success", false);
            result.put("message", "메시지를 불러오는 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * AJAX 쪽지 상세보기 - 안전한 null 처리
     */
    @GetMapping("/read-ajax")
    @ResponseBody
    public Map<String, Object> readMessageAjax(@RequestParam("msgId") String msgId,
                                               HttpSession session) {

        log.info("=== 쪽지 상세보기 AJAX 요청 시작 - msgId: {} ===", msgId);

        if (msgId == null || msgId.trim().isEmpty()) {
            return createErrorResponse("유효하지 않은 메시지 ID입니다.");
        }

        try {
            // 세션에서 loginEmp 가져오기
            Employee loginEmp = (Employee) session.getAttribute("loginEmp");
            if (loginEmp == null) {
                log.warn("세션에 로그인 정보 없음!");
                return createErrorResponse("로그인이 필요합니다.");
            }

            String empNo = loginEmp.getEmpNo();
            log.info("세션에서 가져온 empNo = {}", empNo);

            Map<String, Object> message = messageService.getMessageDetail(msgId, empNo);

            if (message == null || message.isEmpty()) {
                log.warn("메시지를 찾을 수 없음 - msgId: {}, empNo: {}", msgId, empNo);
                return createDefaultMessage(msgId);
            }

            return sanitizeMessageData(message);

        } catch (Exception e) {
            log.error("쪽지 상세보기 중 오류 발생", e);
            return createErrorResponse("쪽지를 불러오는 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/update-importance")
    @ResponseBody
    public Map<String, Object> updateMessageImportance(
            @RequestParam String msgId,
            @RequestParam String receiverNo,
            @RequestParam String importYn,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 실제 사용자 확인 (보안)
            Employee loginEmp = (Employee) session.getAttribute("loginEmp");
            if (loginEmp == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            String actualReceiverNo = loginEmp.getEmpNo();

            // 기존 서비스 메소드 활용하거나 새로 만들기
            Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", actualReceiverNo, "importYn", importYn);
            int result = messageDao.updateImportantStatus(sqlSession, param);

            if (result > 0) {
                response.put("success", true);
                response.put("message", "중요도가 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "중요도 변경에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("중요도 업데이트 오류", e);
            response.put("success", false);
            response.put("message", "시스템 오류가 발생했습니다.");
        }

        return response;
    }


    /**
     * 오류 응답 생성
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", errorMessage);
        errorResponse.put("title", "오류");
        errorResponse.put("senderName", "시스템");
        errorResponse.put("sendDate", new Date());
        errorResponse.put("content", errorMessage);
        return errorResponse;
    }

    /**
     * 기본 메시지 생성 (DB에서 못찾을 경우)
     */
    private Map<String, Object> createDefaultMessage(String msgId) {
        Map<String, Object> defaultMessage = new HashMap<>();
        defaultMessage.put("msgId", msgId);
        defaultMessage.put("title", "메시지를 찾을 수 없습니다");
        defaultMessage.put("senderName", "알 수 없음");
        defaultMessage.put("sendDate", new Date());
        defaultMessage.put("content", "요청하신 메시지를 데이터베이스에서 찾을 수 없습니다.");
        defaultMessage.put("readYn", "G");
        return defaultMessage;
    }

    /**
     * 메시지 데이터의 null 값들을 안전한 기본값으로 처리
     */
    private Map<String, Object> sanitizeMessageData(Map<String, Object> message) {
        if (message == null) {
            log.warn("sanitizeMessageData: 입력 메시지가 null입니다.");
            return createDefaultMessage("UNKNOWN");
        }

        log.info("sanitizeMessageData 시작 - 원본 데이터: {}", message);

        Map<String, Object> sanitized = new HashMap<>(message);

        // 각 필드별로 null 체크 및 기본값 설정
        String title = getStringValue(message, "title");
        if (title == null || title.trim().isEmpty()) {
            sanitized.put("title", "(제목 없음)");
            log.debug("title을 기본값으로 설정");
        }

        String senderName = getStringValue(message, "senderName");
        if (senderName == null || senderName.trim().isEmpty()) {
            // senderNo라도 있으면 사용, 없으면 "알 수 없음"
            String senderNo = getStringValue(message, "senderNo");
            String finalSenderName = (senderNo != null && !senderNo.trim().isEmpty()) ?
                    "사원번호: " + senderNo : "알 수 없는 발신자";
            sanitized.put("senderName", finalSenderName);
            log.debug("senderName을 '{}'으로 설정", finalSenderName);
        }

        Object sendDate = message.get("sendDate");
        if (sendDate == null) {
            sanitized.put("sendDate", new Date());
            log.debug("sendDate를 현재 시간으로 설정");
        }

        String readYn = getStringValue(message, "readYn");
        if (readYn == null) {
            sanitized.put("readYn", "G");
            log.debug("readYn을 'G'로 설정");
        }

        log.info("sanitizeMessageData 완료 - 정리된 데이터: {}", sanitized);
        return sanitized;
    }

    /**
     * Map에서 String 값을 안전하게 가져오기
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        return value.toString();
    }

    @PostMapping("/delete/inbox")
    @ResponseBody
    public Map<String, Object> deleteInbox(@RequestBody Map<String, Object> payload, HttpSession session) {
        String msgId = (String) payload.get("msgId");

        Employee loginEmp = (Employee) session.getAttribute("loginEmp");
        String receiverNo = (loginEmp != null) ? loginEmp.getEmpNo() : null;

        int rows = (receiverNo != null) ? messageService.deleteInboxMessage(msgId, receiverNo) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "삭제 성공" : "삭제 실패");
        return result;
    }

    @PostMapping("/delete/outbox")
    @ResponseBody
    public Map<String, Object> deleteOutbox(@RequestBody Map<String, Object> payload, HttpSession session) {
        String msgId = (String) payload.get("msgId");

        Employee loginEmp = (Employee) session.getAttribute("loginEmp");
        String senderNo = (loginEmp != null) ? loginEmp.getEmpNo() : null;

        System.out.println(">>> delete 요청 msgId=[" + msgId + "], senderNo=[" + senderNo + "]");

        int rows = (senderNo != null) ? messageService.deleteOutboxMessage(msgId, senderNo) : 0;

        log.info(">>> delete 결과 rows={}", rows);

        Map<String, Object> result = new HashMap<>();
        result.put("success", rows > 0);
        result.put("message", rows > 0 ? "발신 쪽지가 삭제되었습니다." : "삭제 실패 (조건 불일치)");
        return result;
    }

}