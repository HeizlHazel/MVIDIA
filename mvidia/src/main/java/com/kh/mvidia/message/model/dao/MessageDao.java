package com.kh.mvidia.message.model.dao;

import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.message.model.vo.MessageBox;
import com.kh.mvidia.message.model.vo.MessageRcpt;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MessageDao {

    private final SqlSession sqlSession;

    public MessageDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    /** 수신 메시지 목록 조회 */
    public List<Map<String, Object>> selectInboxMessages(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("messageMapper.selectInboxMessages", param);
    }

    /** 수신 메시지 개수 조회 */
    public int selectInboxMessageCount(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectOne("messageMapper.selectInboxMessageCount", param);
    }

    public Map<String, Object> selectMessageDetail(SqlSession sqlSession, String msgId, String receiverNo) {
        Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
        return sqlSession.selectOne("messageMapper.selectMessageDetail", param);
    }


    /** 읽음 상태 업데이트 */
    public int updateReadStatus(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.update("messageMapper.updateReadStatus", param);
    }


    /** 중요 상태 조회/업데이트 */
    public String selectImportantStatus(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectOne("messageMapper.selectImportantStatus", param);
    }

    public int updateImportantStatus(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.update("messageMapper.updateImportantStatus", param);
    }

    /** 메시지 발송 */
    public int insertMessageBox(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.insert("messageMapper.insertMessageBox", param);
    }

    public int insertMessageRcpt(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.insert("messageMapper.insertMessageRcpt", param);
    }

    /** 발신함 관련 */
    public List<Map<String, Object>> selectMessageReceivers(SqlSession sqlSession, String msgId) {
        return sqlSession.selectList("messageMapper.selectMessageReceivers", msgId);
    }

    public int selectOutboxMessageCount(SqlSession sqlSession, String senderNo) {
        return sqlSession.selectOne("messageMapper.selectOutboxMessageCount", senderNo);
    }

    /** 사원 검색 */
    public List<Map<String, Object>> searchEmployees(SqlSession sqlSession, String keyword) {
        return sqlSession.selectList("messageMapper.searchEmployees", keyword);
    }

    /** 메시지 ID 생성 */
    public String selectNextMessageId(SqlSession sqlSession) {
        return sqlSession.selectOne("messageMapper.selectNextMessageId");
    }

    public Map<String, Object> selectMessageDetail(String msgId, String receiverNo) {
        Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
        return sqlSession.selectOne("messageMapper.selectMessageDetail", param);
    }

    public List<Employee> findAllEmployees(SqlSession session) {
        return sqlSession.selectList("messageMapper.findAllEmployees");
    }

    public String selectCurrMessageId(SqlSession sqlSession) {
        return sqlSession.selectOne("messageMapper.selectCurrMessageId");
    }

    public int updateReadStatus(String msgId) {
        Map<String, Object> param = Map.of("msgId", msgId);
        return sqlSession.update("messageMapper.updateReadStatus", param);
    }


    public Map<String, Object> selectMessageRDetail(SqlSession sqlSession, String msgId, String receiverNo) {
        Map<String, Object> param = Map.of("msgId", msgId, "receiverNo", receiverNo);
        return sqlSession.selectOne("messageMapper.selectMessageRDetail", param);
    }

    /** 발송 메시지 상세 조회 */
    public Map<String, Object> selectSentMessageDetail(SqlSession sqlSession, String msgId) {
        return sqlSession.selectOne("messageMapper.selectSentMessageDetail", msgId);
    }

    /** 발신함 메시지 목록 조회 */
    public List<Map<String, Object>> selectOutboxMessages(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("messageMapper.selectOutboxMessages", param);
    }

    public int deleteInboxMessage(SqlSessionTemplate sqlSession, String msgId, String receiverNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("msgId", msgId);
        params.put("receiverNo", receiverNo);
        return sqlSession.update("messageMapper.deleteInboxMessage", params);
    }

    public int deleteOutboxMessage(SqlSessionTemplate sqlSession, String msgId, String senderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("msgId", msgId);
        params.put("senderNo", senderNo);
        return sqlSession.update("messageMapper.deleteOutboxMessage", params);
    }


}
