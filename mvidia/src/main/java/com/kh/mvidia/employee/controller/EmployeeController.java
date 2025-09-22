package com.kh.mvidia.employee.controller;

import com.kh.mvidia.common.model.vo.Attachment;
import com.kh.mvidia.common.model.vo.EmpModifyReq;
import com.kh.mvidia.employee.model.service.EmployeeService;
import com.kh.mvidia.employee.model.vo.Employee;
import com.kh.mvidia.permission.model.service.PermissionService;
import com.kh.mvidia.permission.model.service.PermissionServiceImpl;
import com.kh.mvidia.permission.model.vo.Permission;
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
import java.util.*;


@Controller
public class EmployeeController {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
    @Autowired
    private EmployeeService empService;

    @Autowired
    private PermissionService pService;
	
	@Autowired
	private PasswordEncoder bcryptPasswordEncoder;

    @PostMapping("login.emp")
    public String loginEmp(Employee emp, HttpSession session, RedirectAttributes redirectAttributes){
		
		Employee loginEmp =  empService.loginEmp(emp);
		
		if(loginEmp != null && bcryptPasswordEncoder.matches(emp.getEmpPwd(), loginEmp.getEmpPwd())) {
			
			session.setAttribute("loginEmp", loginEmp);

            // 권한 정보 조회 및 세션 저장
            List<Permission> permissions = pService.selectPermissionList(loginEmp.getEmpNo());
            Set<String> grantedPerms = new HashSet<>();

            for(Permission perm : permissions) {
                if("Y".equals(perm.getIsGranted())) {
                    grantedPerms.add(perm.getPermCode());
                }
            }
            session.setAttribute("grantedPerms", grantedPerms);

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
		}else if(loginEmp != null && !bcryptPasswordEncoder.matches(emp.getEmpPwd(), loginEmp.getEmpPwd())){
			redirectAttributes.addFlashAttribute("alertMsg", "비밀번호를 잘못입력하셨습니다.");
			return "redirect:/";
		} else{
			redirectAttributes.addFlashAttribute("alertMsg", "존재하지 않는 사번입니다.");
			return "redirect:/";
		}
    }
	
	@PostMapping("/logout.emp")
	public String logoutEmp(HttpSession session){
        session.removeAttribute("loginEmp");
        session.removeAttribute("grantedPerms");
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
			atch.setFileStatus("U");
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
	
	@PostMapping("/updateInfo.emp")
	public String updateEmpInfo( @RequestParam Map<String, String> params,
								 @RequestParam(value = "atchFile", required = false) MultipartFile atchFile,
								 Employee emp,
								 String originAtchNo,
								 String oldValue,
								 RedirectAttributes redirectAttributes,
								 HttpSession session){
		
			int result = 0;
			int atchResult = 1;

			Employee originEmp = empService.selectEmpNo(emp.getEmpNo());

			if(originEmp == null){
				redirectAttributes.addFlashAttribute("alertMsg", "존재하지 않는 사원입니다.");
				return "redirect:/mainPage";
			}

			Attachment newAtch = null;
			if (atchFile != null && !atchFile.isEmpty()) {
				// 새로운 파일이 업로드된 경우
				String changeName = saveFile(atchFile);
				newAtch = new Attachment();
				newAtch.setRefType("F");
				newAtch.setOriginName(atchFile.getOriginalFilename());
				newAtch.setChangeName(changeName);
				newAtch.setUploadEmpNo(emp.getEmpNo());
				newAtch.setFileStatus("L");
			}

			List<EmpModifyReq> reqList = new ArrayList<>();

			if (newAtch != null) {
				atchResult = empService.insertFile( newAtch);
				if (atchResult > 0) {
					EmpModifyReq atchReq = new EmpModifyReq();
					atchReq.setOldAtchId(originAtchNo);
					atchReq.setNewAtchId(newAtch.getAtchId());
					atchReq.setEmpNo(emp.getEmpNo());
					atchReq.setFieldName("profilePic");
					String oldAttachmentValue = (originAtchNo != null && !originAtchNo.isEmpty())
							? oldValue
							: "default_profile.png";
					
					atchReq.setOldValue(oldAttachmentValue);
					atchReq.setNewValue(newAtch.getChangeName());
					atchReq.setNewAtchId(newAtch.getAtchId());
					reqList.add(atchReq);
				}
			}

			if (!emp.getEmpLName().equals(originEmp.getEmpLName())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("empLName");
				req.setOldValue(originEmp.getEmpLName());
				req.setNewValue(emp.getEmpLName());
				reqList.add(req);
			}
			if (!emp.getEmpName().equals(originEmp.getEmpName())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("empName");
				req.setOldValue(originEmp.getEmpName());
				req.setNewValue(emp.getEmpName());
				reqList.add(req);
			}
			if (!emp.getEmpEngLName().equals(originEmp.getEmpEngLName())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("empEngLName");
				req.setOldValue(originEmp.getEmpEngLName());
				req.setNewValue(emp.getEmpEngLName());
				reqList.add(req);
			}
			if (!emp.getEmpEngName().equals(originEmp.getEmpEngName())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("empEngName");
				req.setOldValue(originEmp.getEmpEngName());
				req.setNewValue(emp.getEmpEngName());
				reqList.add(req);
			}
			if (!emp.getBirthday().equals(originEmp.getBirthday())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("birthday");
				req.setOldValue(originEmp.getBirthday());
				req.setNewValue(emp.getBirthday());
				reqList.add(req);
			}
			if (!emp.getAddress().equals(originEmp.getAddress())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("address");
				req.setOldValue(originEmp.getAddress());
				req.setNewValue(emp.getAddress());
				reqList.add(req);
			}
			if (!emp.getPhone().equals(originEmp.getPhone())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("phone");
				req.setOldValue(originEmp.getPhone());
				req.setNewValue(emp.getPhone());
				reqList.add(req);
			}
			if (!emp.getExtNo().equals(originEmp.getExtNo())) {
				EmpModifyReq req = new EmpModifyReq();
				req.setEmpNo(emp.getEmpNo());
				req.setFieldName("extNo");
				req.setOldValue(originEmp.getExtNo());
				req.setNewValue(emp.getExtNo());
				reqList.add(req);
			}

			// 4. 변경 요청이 한 건이라도 있으면 DB에 삽입
			if (!reqList.isEmpty()) {
				result = empService.insertEmpModifyRequests(reqList);
			}


		if (result * atchResult > 0) {
			redirectAttributes.addFlashAttribute("alertMsg", "개인정보 수정 요청이 완료되었습니다.");
		} else {
			redirectAttributes.addFlashAttribute("alertMsg", "개인정보 수정 요청에 실패했습니다.");
		}

		return "redirect:/mainPage";
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

