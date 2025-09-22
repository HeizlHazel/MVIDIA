package com.kh.mvidia.certificate.service;

import java.util.Map;

public interface CertificateService {
	
	void createCertificatePage(String databaseId, String title, String empNo);
	
	
	Map<String, Object> getPayslipData(String databaseId, String empNo);
	
}
