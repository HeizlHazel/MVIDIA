package com.kh.mvidia.employee.controller;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


@Controller
public class EmployeeController {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
    @Autowired
    private EmployeeService empService;
	
	@Autowired
	private PasswordEncoder bcryptPasswordEncoder;

    @PostMapping("login.emp")
    public String loginEmp(Employee emp, HttpSession session, RedirectAttributes redirectAttributes){
		
		Employee loginEmp =  empService.loginEmp(emp);
		
		if(loginEmp != null && bcryptPasswordEncoder.matches(emp.getEmpPwd(), loginEmp.getEmpPwd())) {
			
			session.setAttribute("loginEmp", loginEmp);
			
			// 세션에 저장된 'redirectUrl'을 가져옴
			String redirectUrl = (String) session.getAttribute("redirectUrl");
			
			// 사용 후 세션에서 URL을 제거하여 재사용 방지
			session.removeAttribute("redirectUrl");
			
			// 리다이렉션 URL이 존재하면 해당 URL로 이동
			if(redirectUrl != null && !redirectUrl.isEmpty()) {
				return "redirect:" + redirectUrl;
			} else {
				// 없으면 기본 메인 페이지로 이동
				return "redirect:/mainPage";
			}
		}else{
			redirectAttributes.addFlashAttribute("alertMsg", "로그인에 실패하였습니다. 다시 로그인 시도해주세요.");
			return "redirect:/";
		}
    }
	
	@PostMapping("/logout.emp")
	public String logoutEmp(HttpSession session){
		session.invalidate();
		return "redirect:/";
	}
	
	@PostMapping("/insertInfo.emp")
	public String insertInfo(Employee emp, Attachment atch, MultipartFile atchFile, Model model, @RequestParam("jobNo") String jobNo) {
		
		// 비밀번호 자동 생성
		String password = null;
		try {
			// String 타입의 birthday를 Date 객체로 변환하여 비밀번호를 생성
			Date birthdayDate = new SimpleDateFormat("yyyy-MM-dd").parse(emp.getBirthday());
			password = emp.getEmpEngLName().toLowerCase() + new SimpleDateFormat("yyMMdd").format(birthdayDate);
		} catch (ParseException e) {
			// 날짜 변환 실패 시 예외 처리
			e.printStackTrace();
			model.addAttribute("errorMsg", "생년월일 형식 오류입니다.");
			return "common/errorPage";
		}
		emp.setEmpPwd(password);
		
		// 파일 정보 처리
		if (!atchFile.getOriginalFilename().equals("")) {
			String changeName = saveFile(atchFile);
			atch.setRefType("F");
			atch.setOriginName(atchFile.getOriginalFilename());
			atch.setChangeName(changeName);
		}
		
		// 최종 확인 페이지에 전달할 데이터들을 Model에 추가
		model.addAttribute("emp", emp);
		model.addAttribute("atch", atch);
		model.addAttribute("jobNo", jobNo);
		
		return "/hr/accountConfirmPage";
	}
	
	@PostMapping("/createEmp.emp")
	public String createEmp(Employee emp, Attachment atch, Model model, RedirectAttributes redirectAttributes){
		
		int result1 = 1;
		
		// 비밀번호 암호화 후 저장
		String encPwd = bcryptPasswordEncoder.encode(emp.getEmpPwd());
		emp.setEmpPwd(encPwd);
		
		int result2 = empService.insertEmpInfo(emp);
		
		if(atch.getOriginName() != null && !atch.getOriginName().equals("")){
			atch.setUploadEmpNo(emp.getEmpNo());
			result1 = empService.insertFile(atch);
		}
		
		if(result1 * result2 > 0) {
			redirectAttributes.addFlashAttribute("alertMsg", "사원 계정 생성에 성공하였습니다. 해당 사원의 사번은 " + emp.getEmpNo() + " 이고 해당 사원의 이메일은 " + emp.getEmpNo() + "@gmail.com 입니다.");
			return "redirect:/hr/empAccount.hr";
		} else{
			model.addAttribute("errorMsg", "사원 계정 생성에 실패했습니다.");
			return "common/errorPage";
		}
		
	}

	
	public String saveFile(MultipartFile atchFile){
	String originName =atchFile.getOriginalFilename();
	
	String currentTime =new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	int ranNum = (int)(Math.random() * 90000 + 10000);
	String ext =originName.substring(originName.lastIndexOf("."));
	
	String changeName = currentTime + ranNum +ext;
		
		File savePath =new File(uploadDir);
		if(!savePath.exists()){
			savePath.mkdirs();
		}
		
		try{
			atchFile.transferTo(new File(savePath, changeName));
		} catch (IllegalStateException | IOException e ){
			e.printStackTrace();
		}
		
		return changeName;
	}
	
	
}

