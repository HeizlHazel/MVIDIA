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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    @Value("${notion.api-token}")
    private String notionToken;

    @Value("${notion.database-id}")
    private String databaseId;

    private static final String NOTION_URL = "https://api.notion.com/v1/pages";
    private static final String FILE_UPLOAD_URL = "https://api.notion.com/v1/file_uploads";

    private final TemplateEngine templateEngine;

    public NotionServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
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
        builder.run();

        return outputStream.toByteArray();
    }

    @Override
    public void insertPayrollToNotion(Salary salary, List<Tax> taxList) {
        try {
            // 1. PDF 생성
            byte[] pdfBytes = generateSalaryPdf(salary, taxList);

            // 2. 단계별 파일 업로드 과정
            String pageId = createNotionPage(salary);           // 페이지 생성
            String fileUploadId = uploadFileToNotion(pdfBytes, salary); // 파일 업로드
            updatePageWithFile(pageId, fileUploadId, salary);    // 페이지에 파일 연결

            System.out.println("✅ Notion에 급여명세서 업로드 완료!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("노션 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // Step 1: 첨부파일 없이 Notion 페이지 생성
    private String createNotionPage(Salary salary) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject properties = new JSONObject();

            // 명세서 이름 (title)
            properties.put("명세서 이름", new JSONObject()
                    .put("title", new JSONArray().put(new JSONObject()
                            .put("text", new JSONObject().put("content",
                                    "급여명세-" + salary.getEmpName() + "-" + salary.getPayDate())))));

            // 사원명 (rich_text)
            properties.put("사원명", new JSONObject()
                    .put("rich_text", new JSONArray().put(new JSONObject()
                            .put("text", new JSONObject().put("content", salary.getEmpName())))));

            // 급여연월 (date)
            String payDate = salary.getPayDate();
            String isoDate = payDate + "-01";
            properties.put("급여년월", new JSONObject()
                    .put("date", new JSONObject().put("start", isoDate)));

            // 실지급액
            properties.put("실지급액", new JSONObject()
                    .put("number", Integer.parseInt(salary.getNetPay())));

            JSONObject parent = new JSONObject().put("database_id", databaseId);
            JSONObject body = new JSONObject()
                    .put("parent", parent)
                    .put("properties", properties);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(NOTION_URL, request, String.class);
            JSONObject jsonResponse = new JSONObject(response);

            String pageId = jsonResponse.getString("id");
            System.out.println("✅ Step 1: Notion 페이지 생성 완료 - ID: " + pageId);

            return pageId;

        } catch (Exception e) {
            throw new RuntimeException("페이지 생성 실패: " + e.getMessage(), e);
        }
    }

    // Step 2: Notion에 파일 업로드 (3단계 과정)
    private String uploadFileToNotion(byte[] pdfBytes, Salary salary) {
        try {
            String fileName = "급여명세_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";

            // Step 2-1: 파일 업로드 생성
            String fileUploadId = createFileUpload(fileName, pdfBytes.length);

            // Step 2-2: 파일 전송
            sendFileUpload(fileUploadId, fileName, pdfBytes);

            System.out.println("✅ Step 2: 파일 업로드 완료 - File Upload ID: " + fileUploadId);
            return fileUploadId;

        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    // Step 2-1: 파일 업로드 생성
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
            throw new RuntimeException("파일 업로드 생성 실패: " + e.getMessage(), e);
        }
    }

    // Step 2-2: 파일 전송
    private void sendFileUpload(String fileUploadId, String fileName, byte[] pdfBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // MultiValueMap을 사용해 multipart/form-data 준비
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // ByteArrayResource로 파일 데이터 래핑
            ByteArrayResource fileResource = new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            String sendUrl = FILE_UPLOAD_URL + "/" + fileUploadId + "/send";
            restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntity, String.class);

        } catch (Exception e) {
            throw new RuntimeException("파일 전송 실패: " + e.getMessage(), e);
        }
    }

    // Step 3: 페이지에 업로드된 파일 연결
    private void updatePageWithFile(String pageId, String fileUploadId, Salary salary) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + notionToken);
            headers.set("Notion-Version", "2022-06-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String fileName = "급여명세_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";

            JSONObject properties = new JSONObject();
            JSONArray files = new JSONArray();

            // file_upload 타입으로 파일 연결
            files.put(new JSONObject()
                    .put("type", "file_upload")
                    .put("name", fileName)
                    .put("file_upload", new JSONObject()
                            .put("id", fileUploadId)));

            properties.put("첨부파일", new JSONObject().put("files", files));

            JSONObject updateBody = new JSONObject().put("properties", properties);

            HttpEntity<String> request = new HttpEntity<>(updateBody.toString(), headers);

            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

            String updateUrl = "https://api.notion.com/v1/pages/" + pageId;
            restTemplate.exchange(updateUrl, HttpMethod.PATCH, request, String.class);

            System.out.println("✅ Step 3: 페이지에 파일 연결 완료");

        } catch (Exception e) {
            System.err.println("⚠️ 페이지 업데이트 실패: " + e.getMessage());
            // 실패해도 페이지와 파일은 생성되었으므로 에러를 던지지 않음
        }
    }

    // === Helper Methods ===
    private int getTaxAmount(List<Tax> taxList, String taxCode) {
        return taxList.stream()
                .filter(t -> taxCode.equals(t.getTaxCode()))
                .mapToInt(t -> parseInt(t.getAmount()))
                .sum();
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }
}