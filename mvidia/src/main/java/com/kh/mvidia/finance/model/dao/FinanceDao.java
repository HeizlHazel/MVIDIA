package com.kh.mvidia.finance.model.dao;

import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceDao {


    public List<Salary> selectSalary(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("salaryMapper.selectSalary", param);
    }

    public Salary selectSalaryByEmpAndMonth(SqlSession sqlSession, Map<String, Object> param) {
        List<Salary> list = sqlSession.selectList("salaryMapper.selectSalary", param);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Attendance> selectAttendanceByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        return sqlSession.selectList("salaryMapper.selectAttendanceByMonth",
                Map.of("empNo", empNo, "yearMonth", yearMonth));
    }

    public int updateOvPrice(SqlSession sqlSession, String empNo, String yearMonth, int ovPrice) {
        return sqlSession.update("salaryMapper.updateOvPrice",
                Map.of("empNo", empNo, "yearMonth", yearMonth, "ovPrice", ovPrice));
    }

    public int upsertSalaryOver(SqlSession sqlSession, String empNo, String yearMonth, String ovCode, int amount) {
        return sqlSession.update("salaryMapper.upsertSalaryOver",
                Map.of("empNo", empNo, "yearMonth", yearMonth,
                        "ovCode", ovCode, "amount", amount));
    }

    public int selectDeductByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        return sqlSession.selectOne("salaryMapper.selectDeductByEmpMonth",
                Map.of("empNo", empNo, "yearMonth", yearMonth));
    }

    public int upsertSalaryTax(SqlSession sqlSession, String empNo, String yearMonth, String taxCode, int amount) {
        return sqlSession.update("salaryMapper.upsertSalaryTax",
                Map.of("empNo", empNo, "yearMonth", yearMonth,
                        "taxCode", taxCode, "amount", amount));
    }


    public List<Tax> selectTaxesByEmpAndMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        return sqlSession.selectList("salaryMapper.selectTaxesByEmpAndMonth",
                Map.of("empNo", empNo, "yearMonth", yearMonth));
    }

}
