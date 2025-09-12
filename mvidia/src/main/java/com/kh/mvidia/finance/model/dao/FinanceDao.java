package com.kh.mvidia.finance.model.dao;

import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Sales;
import com.kh.mvidia.finance.model.vo.Tax;
import jakarta.websocket.Session;
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

    public int selectDeductByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        return sqlSession.selectOne("salaryMapper.selectDeductByEmpMonth", param);
    }

    public int upsertSalaryTax(SqlSession sqlSession, String empNo, String yearMonth, String taxCode, int amount) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", yearMonth);
        param.put("taxCode", taxCode);
        param.put("amount", amount);
        return sqlSession.update("salaryMapper.upsertSalaryTax", param);
    }

    public Salary selectSalaryByEmpAndMonth(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectOne("salaryMapper.selectSalaryByEmpAndMonth", param);
    }

    public List<Tax> selectTaxesByEmpAndMonth(SqlSession sqlSession, String empNo, String payDate) {
        Map<String, Object> param = new HashMap<>();
        param.put("empNo", empNo);
        param.put("yearMonth", payDate.replace("-", "")); // yyyy-MM → yyyyMM 변환
        return sqlSession.selectList("salaryMapper.selectTaxesByEmpAndMonth", param);
    }

    public List<Sales> selectQuarterlySales(SqlSession sqlSession, String year) {
        return sqlSession.selectList("salaryMapper.selectQuarterlySales", year);
    }

    public int mergeQuarterlySales(SqlSession sqlSession, String year) {
        return sqlSession.insert("salaryMapper.mergeQuarterlySales", year);
    }

    public long selectYearlySales(SqlSession sqlSession ,String year) {
        Long result = sqlSession.selectOne("salaryMapper.selectYearlySales", year);
        return (result != null) ? result : 0;
    }
}
