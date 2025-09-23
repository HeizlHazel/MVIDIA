package com.kh.mvidia.notion.service;

import com.kh.mvidia.finance.model.vo.Salary;
import com.kh.mvidia.finance.model.vo.Tax;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class NotionServiceImpl implements NotionService {

    @Value("${notion.api.token}")
    private String notionToken;

    @Value("${notion.finance.database.id}")
    private String databaseId;

    private static final String NOTION_URL = "https://api.notion.com/v1/pages";
    private static final String FILE_UPLOAD_URL = "https://api.notion.com/v1/file_uploads";

    private final TemplateEngine templateEngine;

    public NotionServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void insertPayrollToNotion(Salary salary, List<Tax> taxList) {
        System.out.println("insertPayrollToNotion 시작");

        try {
            if (notionToken == null || notionToken.trim().isEmpty()) {
                throw new RuntimeException("Notion API 토큰이 설정되지 않았습니다.");
            }
            if (databaseId == null || databaseId.trim().isEmpty()) {
                throw new RuntimeException("Notion Database ID가 설정되지 않았습니다.");
            }

            // 1. PDF 생성
            byte[] pdfBytes = generateSalaryPdf(salary, taxList);

            // 2. 파일 업로드
            String fileUploadId = uploadFileToNotion(pdfBytes, salary);

            // 3. 페이지 생성
            String pageId = createNotionPageWithFile(salary, fileUploadId);
            System.out.println("Notion 페이지 생성 완료 - Page ID: " + pageId);

        } catch (Exception e) {
            throw new RuntimeException("노션 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private byte[] generateSalaryPdf(Salary salary, List<Tax> taxList) throws Exception {

        Context context = new Context();
        context.setVariable("salary", salary);
        context.setVariable("taxList", taxList);

        String html = templateEngine.process("finance/salary-pdf", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);

        // 폰트 설정
        builder.useFont( () -> getClass().getResourceAsStream("/fonts/malgun.ttf"),
                "Malgun Gothic");
        // 급여명세서 크기 설정
        builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
        builder.run();

        return outputStream.toByteArray();
    }

    // 노션에 파일 업로드
    private String uploadFileToNotion(byte[] pdfBytes, Salary salary) {
        try {
            String fileName = "급여명세서_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";
            String fileUploadId = createFileUpload(fileName, pdfBytes.length);
            sendFileUpload(fileUploadId, fileName, pdfBytes);
            return fileUploadId;

        } catch (Exception e) {
            System.err.println("파일 업로드 실패:");
            e.printStackTrace();
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    private String createFileUpload(String fileName, int fileSize) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject requestBody = new JSONObject()
                    .put("filename", fileName)
                    .put("content_type", "application/pdf")
                    .put("size", fileSize);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(FILE_UPLOAD_URL, request, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("id");
        } catch (Exception e) {
            System.err.println("파일 업로드 세션 생성 실패:");
            e.printStackTrace();
            throw new RuntimeException("파일 업로드 생성 실패: " + e.getMessage(), e);
        }
    }

    private void sendFileUpload(String fileUploadId, String fileName, byte[] pdfBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            body.add("file", fileResource);

            String sendUrl = FILE_UPLOAD_URL + "/" + fileUploadId + "/send";
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            System.err.println("파일 전송 실패:");
            e.printStackTrace();
            throw new RuntimeException("파일 전송 실패: " + e.getMessage(), e);
        }
    }

    private String createNotionPageWithFile(Salary salary, String fileUploadId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject properties = new JSONObject();

            // === [디버깅 로그] ERP 데이터 출력 ===
            System.out.println("[DEBUG] Salary 객체 내용:");
            System.out.println("empNo=" + salary.getEmpNo());
            System.out.println("empName=" + salary.getEmpName());
            System.out.println("payDate=" + salary.getPayDate());
            System.out.println("netPay=" + salary.getNetPay());

            // 명세서 이름 (title)
            properties.put("명세서 구분", new JSONObject()
                    .put("title", new JSONArray().put(new JSONObject()
                            .put("text", new JSONObject().put("content",
                                    salary.getPayDate() + "-급여명세서-" + salary.getEmpName() )))));

            // 사원명 (rich_text)
            properties.put("사원명", new JSONObject()
                    .put("rich_text", new JSONArray().put(new JSONObject()
                            .put("text", new JSONObject().put("content", salary.getEmpName())))));


            String workedMonth = salary.getPayDate();
            java.time.YearMonth ym = java.time.YearMonth.parse(workedMonth);
            java.time.LocalDate payDateForNotion = ym.plusMonths(1).atDay(9);

            System.out.println("[DEBUG] 변환된 지급일(LocalDate): " + payDateForNotion);

            properties.put("지급일", new JSONObject()
                    .put("date", new JSONObject().put("start", payDateForNotion.toString())));

            properties.put("실지급액", new JSONObject()
                    .put("rich_text", new JSONArray().put(new JSONObject()
                            .put("text", new JSONObject().put("content", salary.getNetPay() + "원")))));

            // 첨부파일 추가
            String fileName = "급여명세서_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";
            JSONArray files = new JSONArray();
            files.put(new JSONObject()
                    .put("type", "file_upload")
                    .put("name", fileName)
                    .put("file_upload", new JSONObject()
                            .put("id", fileUploadId)));

            properties.put("첨부파일", new JSONObject().put("files", files));

            JSONObject parent = new JSONObject().put("database_id", databaseId);
            JSONObject body = new JSONObject()
                    .put("parent", parent)
                    .put("properties", properties);

            // === [디버깅 로그] Notion 요청 Body 출력 ===
            System.out.println("[DEBUG] Notion API 요청 Body:");
            System.out.println(body.toString(2)); // pretty print

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(NOTION_URL, request, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("id");

        } catch (Exception e) {
            System.err.println("페이지 생성 실패:");
            e.printStackTrace();
            throw new RuntimeException("페이지 생성 실패: " + e.getMessage(), e);
        }
    }
}