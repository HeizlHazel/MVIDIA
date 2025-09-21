package com.kh.mvidia.finance.model.dao;

import com.kh.mvidia.finance.model.vo.Attendance;
import com.kh.mvidia.finance.model.vo.Comp;
import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceDao {


    private final SqlSession sqlSession;

    public FinanceDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<Salary> selectSalary(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("salaryMapper.selectSalary", param);
    }

    public List<Salary> selectAllSalary(SqlSession sqlSession, Map<String, Object> param) {
        return sqlSession.selectList("salaryMapper.selectAllSalary", param);
    }

    public Salary selectSalaryByEmpAndMonth(SqlSession sqlSession, Map<String, Object> param) {
        List<Salary> list = sqlSession.selectList("salaryMapper.selectSalary", param);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Attendance> selectAttendanceByEmpMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        return sqlSession.selectList("salaryMapper.selectAttendanceByMonth",
                Map.of("empNo", empNo, "yearMonth", yearMonth));
    }

    public void updateOvPrice(SqlSession sqlSession, String empNo, String yearMonth, int ovPrice) {
        sqlSession.update("salaryMapper.updateOvPrice",
                Map.of("empNo", empNo, "yearMonth", yearMonth, "ovPrice", ovPrice));
    }

    public void updateDeductAmt(SqlSession sqlSession, String empNo, String yearMonth, int deductAmt) {
        sqlSession.update("salaryMapper.updateDeductAmt",
                Map.of("empNo", empNo, "yearMonth", yearMonth, "deductAmt", deductAmt));
    }

    public void upsertSalaryOver(SqlSession sqlSession, String empNo, String yearMonth, String ovCode, int amount) {
         sqlSession.update("salaryMapper.upsertSalaryOver",
                 Map.of("empNo", empNo, "yearMonth", yearMonth,
                        "ovCode", ovCode, "amount", amount));
    }

    public void upsertSalaryTax(SqlSession sqlSession, String empNo, String yearMonth, String taxCode, int amount) {
        sqlSession.update("salaryMapper.upsertSalaryTax",
                Map.of("empNo", empNo, "yearMonth", yearMonth,
                        "taxCode", taxCode, "amount", amount));
    }

    public List<Tax> selectTaxesByEmpAndMonth(SqlSession sqlSession, String empNo, String yearMonth) {
        return sqlSession.selectList("salaryMapper.selectTaxesByEmpAndMonth",
                Map.of("empNo", empNo, "yearMonth", yearMonth));
    }

    public List<Comp> getAllComponents() {
        return sqlSession.selectList("componentMapper.getAllComponents");
    }

    public List<Comp> searchComponents(SqlSession sqlSession) {
        return sqlSession.selectList("componentMapper.searchComponents");
    }

}
