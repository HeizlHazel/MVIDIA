package com.kh.mvidia.calendar.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CalendarDto {
	private String summary;
	private String start;
	private String end;
}
