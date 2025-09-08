package com.kh.mvidia.finance.model.dao;

import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceDao {

    public List<Salary> selectSalaryByMonth(SqlSession sqlSession, String yearMonth) {
        return sqlSession.selectList("salaryMapper.selectSalaryByMonth", yearMonth);
    }

    public List<Salary> selectSalaryByCondition(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("salaryMapper.selectSalaryByCondition", param);
    }

    public List<Attendance> selectAttendanceByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        return sqlSession.selectList("salaryMapper.selectAttendanceByMonth", param);
    }

    // TB_SALARY.OV_PRICE 합계 업데이트
    public int updateOvPrice(SqlSession sqlSession, String empNo, String yearMonth, int ovPrice) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        param.put("ovPrice", ovPrice);
        return sqlSession.update("salaryMapper.updateOvPrice", param);
    }

    // TB_SALARY_OVER 수당 항목별 금액 upsert
    public int upsertSalaryOver(SqlSession sqlSession, String empNo, String yearMonth, String ovCode, int amount) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        param.put("ovCode", ovCode);
        param.put("amount", amount);
        return sqlSession.update("salaryMapper.upsertSalaryOver", param);
    }

    public int upsertSalaryTax(SqlSession sqlSession, String empNo , String yearMonth, String taxCode, int amount) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        param.put("taxCode", taxCode);
        param.put("amount", amount);
        return sqlSession.update("salaryMapper.upsertSalaryTax", param);
    }

    public int selectDeductByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        return sqlSession.selectOne("salaryMapper.selectDeductByEmpMonth", param);
    }

    public int upsertSalaryTax(SqlSession sqlSession, String empNo, String yearMonth, String taxCode, String amount) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        param.put("taxCode", taxCode);
        param.put("amount", amount);
        return sqlSession.update("salaryMapper.upsertSalaryTax", param);
    }
}
