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

    @Override
    public void insertPayrollToNotion(Salary salary, List<Tax> taxList) {
        System.out.println("🚀 insertPayrollToNotion 시작");

        try {
            // 설정 값 검증
            if (notionToken == null || notionToken.trim().isEmpty()) {
                throw new RuntimeException("Notion API 토큰이 설정되지 않았습니다.");
            }
            if (databaseId == null || databaseId.trim().isEmpty()) {
                throw new RuntimeException("Notion Database ID가 설정되지 않았습니다.");
            }

            System.out.println("🔧 설정 값 확인 완료");
            System.out.println("📝 Token: " + (notionToken.length() > 10 ? notionToken.substring(0, 10) + "..." : "설정됨"));
            System.out.println("🗄️ Database ID: " + (databaseId.length() > 10 ? databaseId.substring(0, 10) + "..." : "설정됨"));

            // 1. PDF 생성
            System.out.println("📄 PDF 생성 시작...");
            byte[] pdfBytes = generateSalaryPdf(salary, taxList);
            System.out.println("✅ PDF 생성 완료 - 크기: " + pdfBytes.length + " bytes");

            // 2. 파일 업로드
            System.out.println("📤 파일 업로드 시작...");
            String fileUploadId = uploadFileToNotion(pdfBytes, salary);
            System.out.println("✅ 파일 업로드 완료 - Upload ID: " + fileUploadId);

            // 3. 페이지 생성
            System.out.println("📋 Notion 페이지 생성 시작...");
            String pageId = createNotionPageWithFile(salary, fileUploadId);
            System.out.println("✅ Notion 페이지 생성 완료 - Page ID: " + pageId);

            System.out.println("🎉 모든 작업 완료!");

        } catch (Exception e) {
            System.err.println("❌ insertPayrollToNotion 오류:");
            System.err.println("오류 타입: " + e.getClass().getSimpleName());
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("노션 전송 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private byte[] generateSalaryPdf(Salary salary, List<Tax> taxList) throws Exception {
        System.out.println("📋 PDF 템플릿 처리 시작...");

        Context context = new Context();
        context.setVariable("salary", salary);
        context.setVariable("taxList", taxList);

        String html = templateEngine.process("finance/salary-pdf", context);
        System.out.println("📝 HTML 템플릿 처리 완료 - 길이: " + html.length());

        System.out.println("🖨️ PDF 변환 시작...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(outputStream);

        // 폰트 설정 확인
        try {
            builder.useFont(
                    () -> getClass().getResourceAsStream("/fonts/malgun.ttf"),
                    "Malgun Gothic"
            );
            System.out.println("✅ 폰트 설정 완료");
        } catch (Exception e) {
            System.out.println("⚠️ 폰트 설정 실패, 기본 폰트 사용: " + e.getMessage());
        }

        builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
        builder.run();

        System.out.println("✅ PDF 변환 완료");
        return outputStream.toByteArray();
    }

    private String uploadFileToNotion(byte[] pdfBytes, Salary salary) {
        System.out.println("📤 Notion 파일 업로드 프로세스 시작...");

        try {
            String fileName = "급여명세_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";
            System.out.println("📁 파일명: " + fileName);

            // Step 1: 파일 업로드 생성
            System.out.println("1️⃣ 파일 업로드 세션 생성...");
            String fileUploadId = createFileUpload(fileName, pdfBytes.length);
            System.out.println("✅ 파일 업로드 ID 생성: " + fileUploadId);

            // Step 2: 파일 전송
            System.out.println("2️⃣ 파일 데이터 전송...");
            sendFileUpload(fileUploadId, fileName, pdfBytes);
            System.out.println("✅ 파일 전송 완료");

            return fileUploadId;

        } catch (Exception e) {
            System.err.println("❌ 파일 업로드 실패:");
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

            System.out.println("📋 파일 업로드 요청 준비 완료");
            System.out.println("URL: " + FILE_UPLOAD_URL);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(FILE_UPLOAD_URL, request, String.class);
            System.out.println("📨 Notion API 응답: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("id");

        } catch (Exception e) {
            System.err.println("❌ 파일 업로드 세션 생성 실패:");
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
            System.out.println("📤 파일 전송 URL: " + sendUrl);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.exchange(sendUrl, HttpMethod.POST, requestEntity, String.class);
            System.out.println("✅ 파일 전송 성공");

        } catch (Exception e) {
            System.err.println("❌ 파일 전송 실패:");
            e.printStackTrace();
            throw new RuntimeException("파일 전송 실패: " + e.getMessage(), e);
        }
    }

    private String createNotionPageWithFile(Salary salary, String fileUploadId) {
        try {
            System.out.println("📋 Notion 페이지 생성 시작...");

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

            // 첨부파일 추가
            String fileName = "급여명세_" + salary.getEmpName() + "_" + salary.getPayDate() + ".pdf";
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

            System.out.println("📋 페이지 생성 요청 데이터 준비 완료");
            System.out.println("URL: " + NOTION_URL);

            HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(NOTION_URL, request, String.class);
            System.out.println("📨 페이지 생성 응답: " + response);

            JSONObject jsonResponse = new JSONObject(response);
            String pageId = jsonResponse.getString("id");

            System.out.println("✅ Notion 페이지 생성 완료 - ID: " + pageId);
            return pageId;

        } catch (Exception e) {
            System.err.println("❌ 페이지 생성 실패:");
            e.printStackTrace();
            throw new RuntimeException("페이지 생성 실패: " + e.getMessage(), e);
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