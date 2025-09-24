package com.kh.mvidia.certificate.service;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class CertificateServiceImpl implements NotionCertificateService {
	
	@Value("${notion.api.token}")
	private String notionToken;
	
	private static final SecureRandom RND = new SecureRandom();
	
	// --- 공통 POST 요청 헬퍼 (헤더 자동 세팅) ---
	private HttpRequestWithBody notionPost(String url) {
		return Unirest.post(url)
				.header("Authorization", "Bearer " + notionToken)
				.header("Notion-Version", "2022-06-28")
				.header("Content-Type", "application/json");
	}
	
	private static String normalizeDbId(String raw){
		String s = raw.replace("-", "");
		if (s.length() == 32){
			return String.format("%s-%s-%s-%s-%s",
					s.substring(0,8), s.substring(8,12),
					s.substring(12,16), s.substring(16,20), s.substring(20));
		}
		return raw;
	}
	
	private static String rnd4(){ return String.format("%04d", RND.nextInt(10_000)); }
	private static String today(){ return LocalDate.now().toString(); } // yyyy-MM-dd
	
	@Override
	public Map<String, Object> createCertificatePage(String databaseId, String empNo, String approverEmpNo) {
		String dbId = normalizeDbId(databaseId);
		
		String issueNo = "ISS-" + today().replace("-", "") + "-" + empNo + "-" + rnd4();
		String certNo  = "CRT-" + today().replace("-", "") + "-" + rnd4();
		
		// ===== Notion properties =====
		JSONObject props = new JSONObject();
		
		// 증명서 발급 번호 (Title)
		JSONArray titleArr = new JSONArray()
				.put(new JSONObject().put("text", new JSONObject().put("content", issueNo)));
		props.put("증명서 발급 번호", new JSONObject().put("title", titleArr));
		
		// 신청자 사번 (Rich text)
		JSONArray rtEmp = new JSONArray()
				.put(new JSONObject().put("text", new JSONObject().put("content", empNo)));
		props.put("신청자 사번", new JSONObject().put("rich_text", rtEmp));
		
		// 결재자 사번 (Rich text)
		String approver = approverEmpNo == null ? "" : approverEmpNo;
		JSONArray rtApp = new JSONArray()
				.put(new JSONObject().put("text", new JSONObject().put("content", approver)));
		props.put("결재자 사번", new JSONObject().put("rich_text", rtApp));
		
		// 증명서 번호 (Rich text)
		JSONArray rtCert = new JSONArray()
				.put(new JSONObject().put("text", new JSONObject().put("content", certNo)));
		props.put("증명서 번호", new JSONObject().put("rich_text", rtCert));
		
		// 발급 날짜 (Date)
		props.put("발급 날짜", new JSONObject().put("date", new JSONObject().put("start", today())));
		
		// 상태 (Select) – 노션 DB에 "발급" 옵션이 있어야 함
		props.put("상태", new JSONObject().put("select", new JSONObject().put("name", "발급")));
		
		JSONObject body = new JSONObject()
				.put("parent", new JSONObject().put("database_id", dbId))
				.put("properties", props);
		
		HttpResponse<JsonNode> res = notionPost("https://api.notion.com/v1/pages")
				.body(body)
				.asJson();
		
		if (res.getStatus() / 100 != 2){
			String err = res.getBody() != null ? res.getBody().toString() : ("HTTP " + res.getStatus());
			throw new RuntimeException("Notion create page failed: " + err);
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("pageId", res.getBody().getObject().optString("id"));
		result.put("issueNo", issueNo);
		result.put("certNo",  certNo);
		return result;
	}
	
	
	// ===== Notion 값 파서 =====
	private static String getRichOrTitle(JSONObject props, String key){
		if(!props.has(key)) return null;
		JSONObject p = props.getJSONObject(key);
		if(p.has("title")){
			JSONArray arr = p.getJSONArray("title");
			if(arr.length() > 0) return arr.getJSONObject(0).optString("plain_text", null);
		}
		if(p.has("rich_text")){
			JSONArray arr = p.getJSONArray("rich_text");
			if(arr.length() > 0) return arr.getJSONObject(0).optString("plain_text", null);
		}
		if(p.has("select")){
			return p.getJSONObject("select").optString("name", null);
		}
		if(p.has("date")){
			return p.getJSONObject("date").optString("start", null);
		}
		return null;
	}
	
	private static double getNumber(JSONObject props, String key){
		if(!props.has(key)) return 0.0;
		JSONObject p = props.getJSONObject(key);
		return p.has("number") ? p.optDouble("number", 0.0) : 0.0;
	}
}
