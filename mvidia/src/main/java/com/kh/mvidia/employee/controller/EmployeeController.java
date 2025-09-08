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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;


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
	public String insertInfo(Employee emp, Attachment atch, MultipartFile atchFile, Model model, RedirectAttributes redirectAttributes){
		
		int result1 = 1;
		
		if(!atchFile.getOriginalFilename().equals("")){
			String changeName =saveFile(atchFile);
			
			atch.setRefType("F");
			atch.setOriginName(atchFile.getOriginalFilename());
			atch.setChangeName(changeName);
			
			int result1 = empService.insertFile(atch);
		}
		
		int result2 = empService.insertEmpInfo(emp);
		
		if(result1 * result2 > 0) {
			return "/hr/accountConfirmPage";
		} else{
			model.addAttribute("errorMsg", "사원 등록 실패");
			return "/common.errorPage";
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

