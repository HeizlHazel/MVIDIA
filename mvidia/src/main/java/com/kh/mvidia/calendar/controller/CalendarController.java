package com.kh.mvidia.calendar.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.kh.mvidia.calendar.dto.CalendarDto;
import com.kh.mvidia.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
	
	@Autowired
	private CalendarService calService;
	
	
	@GetMapping("/events")
	public ArrayList<CalendarDto> getCalendarEvents(@RequestParam int year, @RequestParam int month) {
		return calService.getKoreanHolidays(year, month);
	}
}
