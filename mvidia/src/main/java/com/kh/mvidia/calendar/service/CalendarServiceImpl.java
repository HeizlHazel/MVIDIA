package com.kh.mvidia.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.kh.mvidia.calendar.dto.CalendarDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService{
	
	@Value("${gCalendar.api-key}")
	private String apiKey;
	
	private static final String KOREA_HOLIDAY_CAL_ID = "ko.south_korea#holiday@group.v.calendar.google.com";
	private static final String APPLICATION_NAME = "Calendar Viewer";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	
	@Override
	public ArrayList<CalendarDto> getKoreanHolidays(int year, int month) {
		try {
			Calendar service = buildService();
			
			// [중요] timeMax는 "배타적"이므로 다음 달 1일 00:00:00 으로 설정
			LocalDate firstDay = LocalDate.of(year, month, 1);
			LocalDate firstDayNextMonth = firstDay.plusMonths(1);
			
			ZoneId KST = ZoneId.of("Asia/Seoul");
			DateTime timeMin = new DateTime(firstDay.atStartOfDay(KST).toInstant().toEpochMilli());
			DateTime timeMax = new DateTime(firstDayNextMonth.atStartOfDay(KST).toInstant().toEpochMilli());// exclusive
			
			Events events = service.events()
					.list(KOREA_HOLIDAY_CAL_ID)
					.setKey(apiKey)
					.setTimeMin(timeMin)
					.setTimeMax(timeMax)
					.setSingleEvents(true)
					.setOrderBy("startTime")
					.execute();
			
			ArrayList<CalendarDto> list = new ArrayList<>();
			if (events.getItems() != null) {
				for (Event ev : events.getItems()) {
					String start = ev.getStart().getDateTime() != null
							? ev.getStart().getDateTime().toStringRfc3339()
							: ev.getStart().getDate().toStringRfc3339(); // 종일 이벤트
					
					String end = ev.getEnd().getDateTime() != null
							? ev.getEnd().getDateTime().toStringRfc3339()
							: ev.getEnd().getDate().toStringRfc3339();
					
					list.add(new CalendarDto(ev.getSummary(), start, end));
				}
			}
			return list;
			
		} catch (Exception e) { // IOException | GeneralSecurityException 포함
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	private Calendar buildService() throws GeneralSecurityException, java.io.IOException {
		return new Calendar.Builder(
				GoogleNetHttpTransport.newTrustedTransport(),
				JSON_FACTORY,
				null // 공개 캘린더 + API Key만 사용할 때는 null로 OK
		).setApplicationName(APPLICATION_NAME).build();
	}
}
