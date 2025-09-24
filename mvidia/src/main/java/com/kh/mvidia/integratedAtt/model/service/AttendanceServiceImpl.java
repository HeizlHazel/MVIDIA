package com.kh.mvidia.integratedAtt.model.service;

import com.kh.mvidia.common.model.vo.PageInfo;
import com.kh.mvidia.integratedAtt.model.dao.AttendanceDao;
import com.kh.mvidia.integratedAtt.model.vo.Attendance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService{
	
	@Autowired
	private AttendanceDao attDao;
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	private static final int START_MIN = 9*60;   // 09:00
	private static final int END_MIN   = 18*60;  // 18:00
	private static final int GRACE_MIN = 10;
	
	@Override
	public ArrayList<Attendance> selectRecentAttendances() {
		return attDao.selectRecentAttendances(sqlSession);
	}
	
	@Override
	public int selectAttListCount(HashMap<String, String> searchMap) {
		return attDao.selectAttListCount(sqlSession,searchMap);
	}
	
	@Override
	public ArrayList<Attendance> selectAttendanceList(PageInfo pi, HashMap<String, String> searchMap) {
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi", pi);
		paramMap.put("searchMap", searchMap);
		return attDao.selectAttendanceList(sqlSession,paramMap);
	}
	
	@Override
	public int updateAttendance(Attendance att) {
		return attDao.updateAttendance(sqlSession,att);
	}
	
	@Override
	public ArrayList<Attendance> selectEmpAttendanceList(PageInfo pi, HashMap<String, String> searchMap){
		HashMap<String, Object> paramMap = new HashMap<>();
		paramMap.put("pi",pi);
		paramMap.put("searchMap", searchMap);
		return attDao.selectEmpAttendanceList(sqlSession,paramMap);
	}

    @Override
	@Transactional
    public Attendance selectToday(String empNo) {
        return attDao.selectToday(sqlSession, empNo);
    }

    @Override
    public HashMap<String,Object> checkInUpsert(String empNo) {
		
		boolean isLateNow = new Date().after(
				buildDeadline(trunc(new Date()), START_MIN + GRACE_MIN)
		);
		String statusForInsert = isLateNow ? "T" : "N";
		
		HashMap<String,Object> param = new HashMap<>();
		param.put("empNo", empNo);
		param.put("status", statusForInsert);
		
		int affected = attDao.checkInUpsert(sqlSession, param); // 시그니처 변경
		boolean duplicated = (affected == 0);
		
		Attendance today = attDao.selectToday(sqlSession, empNo);
		
		HashMap<String,Object> res = new HashMap<>();
		Date arriving = parseHMOn(today.getAttDate(), today.getArrivingTime());
		Date leaving  = parseHMOn(today.getAttDate(), today.getLeavingTime());
		res.put("arriving", toHM(arriving));
		res.put("leaving",  toHM(leaving));
		res.put("status",   today != null ? today.getAttStatus() : null);
		res.put("duplicated", duplicated);
		res.put("message", duplicated ? "이미 출근 처리된 상태입니다." : "출근 처리 완료");
		return res;
    }

    @Override
    public HashMap<String, Object> checkOut(String empNo) {
		
		Attendance t = attDao.selectToday(sqlSession, empNo);
		HashMap<String,Object> res = new HashMap<>();
		if (t == null) {
			res.put("message", "출근 기록이 없습니다.");
			return res;
		}
		
		// 1) 먼저 퇴근 시각 기록(한 번만)
		attDao.checkOut(sqlSession, empNo);
		
		// 2) 최신 값 다시 조회
		t = attDao.selectToday(sqlSession, empNo);
		
		Date arriving = parseHMOn(t.getAttDate(), t.getArrivingTime());
		Date leaving  = parseHMOn(t.getAttDate(), t.getLeavingTime());
		
		// 근무시간(분)
		long workedMin = (arriving != null && leaving != null)
				? (leaving.getTime() - arriving.getTime()) / 60000L
				: 0L;
		
		// 정시 이전(17:50 이전) 퇴근인지
		boolean isBeforeScheduledEnd = leaving != null &&
				leaving.before(buildDeadline(trunc(t.getAttDate()), END_MIN - GRACE_MIN));
		
		// 지각/조퇴 판정
		boolean isLate  = arriving != null &&
				arriving.after(buildDeadline(trunc(t.getAttDate()), START_MIN + GRACE_MIN));
		
		boolean lessThan8h = workedMin < 480;                 // ✅ 8시간 미만 규칙 추가
		boolean isEarly    = isBeforeScheduledEnd || lessThan8h;
		
		String status = isLate ? "T" : (isEarly ? "E" : "N");
		
		// 3) 오늘 상태 업데이트
		HashMap<String,Object> param = new HashMap<>();
		param.put("empNo", empNo);
		param.put("status", status);
		attDao.updateStatusToday(sqlSession, param);
		
		// 4) 응답
		res.put("arriving", toHM(arriving));
		res.put("leaving",  toHM(leaving));
		res.put("status",   status);
		res.put("workedMin", workedMin); // (프론트에서 쓸 일 있으면)
		res.put("message",  (isEarly && !isLate) ? "조퇴 처리" : "퇴근 처리 완료");
		return res;
    }

    @Override
    public Attendance selectTodayTimes(String empNo) {
        return attDao.selectTodayTimes(sqlSession,empNo);
    }
	
	@Override
	public int selectEmpAttListCount(HashMap<String, String> searchMap) {
		return attDao.selectEmpAttListCount(sqlSession,searchMap);
	}
	
	
	/* ========= Helpers ========= */
	
	private Date trunc(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	/** ✅ String(YYYY-MM-DD…)을 00:00의 Date로 변환하는 오버로드 */
	private Date trunc(String ymd) {
		if (ymd == null || ymd.isEmpty()) return trunc(new Date());
		String s = ymd.length() >= 10 ? ymd.substring(0, 10) : ymd;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setLenient(false);
			return trunc(sdf.parse(s));
		} catch (ParseException e) {
			return trunc(new Date());
		}
	}
	
	private Date buildDeadline(Date base00, int minutes) {
		Calendar c = Calendar.getInstance();
		c.setTime(base00);
		c.add(Calendar.MINUTE, minutes);
		return c.getTime();
	}
	
	private String toHM(Date d) {
		if (d == null) return null;
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}
	
	/** 기존 시그니처(Date, String)는 그대로 둬도 되고, 아래 (String, String) 오버로드를 추가 */
	private Date parseHMOn(Date baseDate, String hm) {
		if (hm == null) return null;
		try {
			String[] sp = hm.split(":");
			int hh = Integer.parseInt(sp[0]);
			int mm = Integer.parseInt(sp[1]);
			Calendar c = Calendar.getInstance();
			c.setTime(trunc(baseDate != null ? baseDate : new Date()));
			c.set(Calendar.HOUR_OF_DAY, hh);
			c.set(Calendar.MINUTE, mm);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		} catch (Exception e) {
			return null;
		}
	}
	
	/** ✅ (String, String) 오버로드: baseDate가 "YYYY-MM-DD" 문자열이어도 호출 가능 */
	private Date parseHMOn(String baseYmd, String hm) {
		if (hm == null) return null;
		Date base = trunc(baseYmd);
		try {
			String[] sp = hm.split(":");
			int hh = Integer.parseInt(sp[0]);
			int mm = Integer.parseInt(sp[1]);
			Calendar c = Calendar.getInstance();
			c.setTime(base);
			c.set(Calendar.HOUR_OF_DAY, hh);
			c.set(Calendar.MINUTE, mm);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		} catch (Exception e) {
			return null;
		}
	}
}
