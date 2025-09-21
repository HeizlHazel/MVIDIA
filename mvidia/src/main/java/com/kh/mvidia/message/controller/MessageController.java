package com.kh.mvidia.message.controller;

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

    /**
     * 수신함 페이지
     */
    @GetMapping("/inbox")
    public String inboxPage(@RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "filter", defaultValue = "all") String filter,
                            HttpSession session, Model model) {

        // ★ 임시로 설정된 부분 - 실제로는 세션에서 가져와야 함
        String receiverNo = "22010001"; // (String) session.getAttribute("empNo");

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

        // ★ 임시로 설정된 부분 - 실제로는 세션에서 가져와야 함
        String senderNo = "22010001"; // (String) session.getAttribute("empNo");
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
     * 메시지 삭제
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteMessage(@RequestParam("msgId") String msgId,
                                             @RequestParam("empNo") String empNo) {

        log.info("메시지 삭제 요청 - msgId: {}, empNo: {}", msgId, empNo);
        return messageService.deleteMessage(msgId, empNo);
    }

    /**
     * 발신함 페이지
     */
    @GetMapping("/outbox")
    public String outboxPage(@RequestParam(value = "page", defaultValue = "1") int page,
                             HttpSession session, Model model) {

        // ★ 임시로 설정된 부분 - 실제로는 세션에서 가져와야 함
        String senderNo = "22010001"; // (String) session.getAttribute("empNo");

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

        // 파라미터 검증
        if (msgId == null || msgId.trim().isEmpty()) {
            log.warn("유효하지 않은 msgId: {}", msgId);
            return createErrorResponse("유효하지 않은 메시지 ID입니다.");
        }

        try {
            String empNo = (String) session.getAttribute("empNo");
            if (empNo == null) {
                empNo = "22010001"; // 임시 설정
                log.info("임시 empNo 사용: {}", empNo);
            }

            log.info("서비스 호출 전 - msgId: {}, empNo: {}", msgId, empNo);

            // 메시지 상세 조회 + 읽음 처리
            Map<String, Object> message = null;

            // 우선 messageService가 null인지 체크
            if (messageService == null) {
                log.error("MessageService가 null입니다!");
                return createErrorResponse("서비스 초기화 오류입니다.");
            }

            message = messageService.getMessageDetail(msgId, empNo);
            log.info("서비스 호출 후 결과: {}", message);

            if (message == null || message.isEmpty()) {
                log.warn("메시지를 찾을 수 없음 - msgId: {}, empNo: {}", msgId, empNo);

                // MyBatis 직접 호출 시도
                try {
                    log.info("MyBatis 직접 호출 시도...");
                    Map<String, Object> directResult = sqlSession.selectOne("messageMapper.selectMessageDetail", Map.of("msgId", msgId));
                    log.info("직접 호출 결과: {}", directResult);

                    if (directResult != null && !directResult.isEmpty()) {
                        message = sanitizeMessageData(directResult);
                        // 읽음 처리도 직접 수행
                        sqlSession.update("messageMapper.updateReadStatus", Map.of("msgId", msgId, "receiverNo", empNo));
                        sqlSession.commit();
                        return message;
                    }
                } catch (Exception dbEx) {
                    log.error("직접 DB 호출 실패", dbEx);
                }

                return createDefaultMessage(msgId);
            }

            // null 값들을 안전한 기본값으로 처리
            message = sanitizeMessageData(message);

            log.info("=== 쪽지 조회 성공 - msgId: {}, title: {} ===", msgId, message.get("title"));
            return message;

        } catch (Exception e) {
            log.error("=== 쪽지 상세보기 중 오류 발생 - msgId: {} ===", msgId, e);
            return createErrorResponse("쪽지를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
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

        String content = getStringValue(message, "content");
        if (content == null || content.trim().isEmpty()) {
            sanitized.put("content", "(내용이 없습니다)");
            log.debug("content를 기본값으로 설정");
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
}