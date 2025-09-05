package com.kh.mvidia.employee.passwordEncryptor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncryptor {
		public static void main(String[] args) {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String rawPassword = "choi880918"; // 예: "1234"
			String encryptedPassword = passwordEncoder.encode(rawPassword);
			System.out.println("암호화된 비밀번호: " + encryptedPassword);
		}
}
