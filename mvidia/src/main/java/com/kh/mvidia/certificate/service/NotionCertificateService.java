package com.kh.mvidia.certificate.service;

import java.util.Map;

public interface NotionCertificateService {
	
	Map<String, Object> createCertificatePage(String databaseId, String empNo, String approverEmpNo);
	
}
