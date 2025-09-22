package com.kh.mvidia.calendar.service;

import com.kh.mvidia.calendar.dto.CalendarDto;

import java.util.ArrayList;
import java.util.List;

public interface CalendarService {
	ArrayList<CalendarDto> getKoreanHolidays(int year, int month);
}
