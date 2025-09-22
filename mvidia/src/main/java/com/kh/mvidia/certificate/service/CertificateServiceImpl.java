package com.kh.mvidia.certificate.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateServiceImpl implements CertificateService{
	
	@Value("${notion.api.token}")
	private String notionApiToken;
	
	private final RestTemplate restTemplate = new RestTemplate();
	private final Gson gson = new Gson();
	
	@Override
	public void createCertificatePage(String databaseId, String title, String empNo) {
		String url = "https://api.notion.com/v1/pages";
		
		JsonObject payload = new JsonObject();
		JsonObject parent = new JsonObject();
		parent.addProperty("database_id", databaseId);
		payload.add("parent", parent);
		
		JsonObject properties = new JsonObject();
		properties.add("Type", createTitleProperty(title));
		properties.add("EmpNo", createRichTextProperty(empNo));
		properties.add("IssueDate", createDateProperty(LocalDate.now()));
		payload.add("properties", properties);
		
		HttpHeaders headers = createHeaders();
		HttpEntity<String> request = new HttpEntity<>(gson.toJson(payload), headers);
		
		restTemplate.exchange(url, HttpMethod.POST, request, String.class);
	}
	
	@Override
	public Map<String, Object> getPayslipData(String databaseId, String empNo) {
		String url = "https://api.notion.com/v1/databases/" + databaseId + "/query";
		
		JsonObject payload = new JsonObject();
		JsonObject filter = new JsonObject();
		JsonObject propertyFilter = new JsonObject();
		propertyFilter.addProperty("property", "사번"); // Ensure this matches the Notion DB field name
		JsonObject textFilter = new JsonObject();
		textFilter.addProperty("equals", empNo);
		propertyFilter.add("rich_text", textFilter);
		filter.add("filter", propertyFilter);
		payload.add("filter", filter);
		
		HttpHeaders headers = createHeaders();
		HttpEntity<String> request = new HttpEntity<>(gson.toJson(payload), headers);
		
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		
		JsonObject responseJson = gson.fromJson(response.getBody(), JsonObject.class);
		JsonArray results = responseJson.getAsJsonArray("results");
		
		if (results.size() > 0) {
			JsonObject page = results.get(0).getAsJsonObject();
			JsonObject props = page.getAsJsonObject("properties");
			
			Map<String, Object> payslipData = new HashMap<>();
			payslipData.put("name", getTextFromNotionProperty(props, "이름"));
			payslipData.put("empNo", getTextFromNotionProperty(props, "사번"));
			payslipData.put("payDate", getTextFromNotionProperty(props, "지급일"));
			payslipData.put("position", getTextFromNotionProperty(props, "직급"));
			payslipData.put("baseSalary", getNumberFromNotionProperty(props, "기본급"));
			payslipData.put("mealAllowance", getNumberFromNotionProperty(props, "식대"));
			payslipData.put("bonus", getNumberFromNotionProperty(props, "성과급"));
			payslipData.put("totalPay", getNumberFromNotionProperty(props, "총 지급액"));
			payslipData.put("nationalPension", getNumberFromNotionProperty(props, "국민연금"));
			payslipData.put("healthInsurance", getNumberFromNotionProperty(props, "건강보험"));
			payslipData.put("employmentInsurance", getNumberFromNotionProperty(props, "고용보험"));
			payslipData.put("incomeTax", getNumberFromNotionProperty(props, "소득세"));
			payslipData.put("totalDeduction", getNumberFromNotionProperty(props, "총 공제액"));
			payslipData.put("netPay", getNumberFromNotionProperty(props, "실수령액"));
			
			return payslipData;
		}
		
		return null;
	}
	
	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(notionApiToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Notion-Version", "2022-06-28");
		return headers;
	}
	
	private JsonObject createTitleProperty(String content) {
		JsonObject property = new JsonObject();
		JsonArray titleArray = new JsonArray();
		JsonObject textObject = new JsonObject();
		JsonObject textContent = new JsonObject();
		textContent.addProperty("content", content);
		textObject.add("text", textContent);
		titleArray.add(textObject);
		property.add("title", titleArray);
		return property;
	}
	
	private JsonObject createRichTextProperty(String content) {
		JsonObject property = new JsonObject();
		JsonArray richTextArray = new JsonArray();
		JsonObject textObject = new JsonObject();
		JsonObject textContent = new JsonObject();
		textContent.addProperty("content", content);
		textObject.add("text", textContent);
		richTextArray.add(textObject);
		property.add("rich_text", richTextArray);
		return property;
	}
	
	private JsonObject createDateProperty(LocalDate date) {
		JsonObject property = new JsonObject();
		JsonObject dateObject = new JsonObject();
		dateObject.addProperty("start", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		property.add("date", dateObject);
		return property;
	}
	
	private String getTextFromNotionProperty(JsonObject props, String propName) {
		if (props.has(propName) && props.get(propName).getAsJsonObject().has("rich_text")) {
			JsonArray richText = props.get(propName).getAsJsonObject().getAsJsonArray("rich_text");
			if (richText.size() > 0 && richText.get(0).getAsJsonObject().has("plain_text")) {
				return richText.get(0).getAsJsonObject().get("plain_text").getAsString();
			}
		}
		return null;
	}
	
	private double getNumberFromNotionProperty(JsonObject props, String propName) {
		if (props.has(propName) && props.get(propName).getAsJsonObject().has("number")) {
			return props.get(propName).getAsJsonObject().get("number").getAsDouble();
		}
		return 0.0;
	}
}
