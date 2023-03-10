package com.itwillbs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.gson.JsonObject;
import com.itwillbs.dao.AdminDAO;
import com.itwillbs.domain.PageDTO;
import com.itwillbs.domain.ProjectDTO;
import com.itwillbs.domain.UserDTO;
import com.itwillbs.domain.NoticeDTO;
import com.itwillbs.domain.PaymentDTO;
import com.itwillbs.service.AdminService;


@Controller
public class AdminController {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Inject
	private AdminService adminService;
	
//	관리자 메인 페이지
	@RequestMapping(value = "/admin/userMain", method = RequestMethod.GET)	
	public String userMain() {
		
		return "admin/userMain";
	}

//	관리자 회원관리페이지
	@RequestMapping(value = "/admin/userList", method = RequestMethod.GET)	
	public String userList(Model model, HttpServletRequest request) {
		//페이징처리
		int pageSize = 10;
		String pageNum = request.getParameter("pageNum");
		
		if(pageNum==null){
		 	pageNum = "1";
		}
		
		int currentPage = Integer.parseInt(pageNum);
		
		PageDTO dto = new PageDTO();
		
		dto.setPageSize(pageSize);
		dto.setPageNum(pageNum);
		dto.setCurrentPage(currentPage);
		
		List<UserDTO> userList = adminService.getUserList(dto);
		
		int count = adminService.getBoardCount();
		int pageBlock = 10; 
		int startPage = (currentPage-1)/pageBlock*pageBlock+1;
		int endPage = startPage+pageBlock-1;
		int pageCount = count/pageSize + ( count % pageSize == 0 ? 0 : 1);
		
		if(endPage > pageCount){
	 	   endPage = pageCount;
	    }
		
		dto.setCount(count);
		dto.setPageBlock(pageBlock);
		dto.setStartPage(startPage);
		dto.setEndPage(endPage);
		dto.setPageCount(pageCount);
		
		model.addAttribute("userList", userList);
		model.addAttribute("pageDto", dto);
		
		return "admin/userList";
	}
//	회원리스트에서의 검색가능
	@RequestMapping(value = "/admin/listPageSearch", method = RequestMethod.GET)	
	public String listPageSearch() {
		
		return "admin/listPageSearch";
	}
	
//	관리자 회원상세페이지
	@RequestMapping(value = "/admin/userView", method = RequestMethod.GET)
	public String userView(String userId, Model model) {
		// DB에서 userDTO 가져오기
		model.addAttribute("userDTO", adminService.userView(userId));  
		
		// DB에서 proDTO 가져오기
		List<ProjectDTO> proList = adminService.getProject(userId);
		if(proList != null) {
			model.addAttribute("proList", proList); //proDTO
		}
		
		// DB에서 payDTO 가져오기 
		List<PaymentDTO> payList = adminService.getPayment(userId);
		if(payList != null) {
			System.out.println(payList);
			model.addAttribute("payList", payList); //proDTO
		}
		return "admin/userView";
	}
	
	
//	관리자 회원상태제어(계정정지기능)
	@RequestMapping(value = "/admin/updatePro", method = RequestMethod.GET)	
	public String updatePro(HttpServletRequest request, String userId) {
		
		request.setAttribute("userId", userId);
		String result = adminService.goBlack(userId);
		
		// 활동계정이면(USR00) -> 정지계정(USR02)으로
		if(result.equals("USR00")) { 
			adminService.updateUserBlack(userId);
		} else if (result.equals("USR02")){ 
		// 정지계정이면
			adminService.updateUserNomal(userId);
		}
		
		return "redirect:/admin/userView?userId="+userId;
	}
	
//	프로젝트 심사페이지 목록 불러오기 (심사중 / 심사완료)
	@RequestMapping(value = "/admin/projectJudge", method = RequestMethod.GET)
	public String projectJudge(Model model, HttpServletRequest request) {
		//페이징처리 심사중인 프로젝트 
		int pageSize = 10;
		String pageNum = request.getParameter("pageNum");
		
		if(pageNum==null){
		 	pageNum = "1";
		}
		
		int currentPage = Integer.parseInt(pageNum);
		
		PageDTO dto = new PageDTO();
		
		dto.setPageSize(pageSize);
		dto.setPageNum(pageNum);
		dto.setCurrentPage(currentPage);
		
		List<ProjectDTO> judgeList = adminService.projectJudge(dto);
		
		int count = adminService.getJudgeCount();
		int pageBlock = 10; 
		int startPage = (currentPage-1)/pageBlock*pageBlock+1;
		int endPage = startPage+pageBlock-1;
		int pageCount = count/pageSize + ( count % pageSize == 0 ? 0 : 1);
		
		
		if(endPage > pageCount){
	 	   endPage = pageCount;
	    }
		
		dto.setCount(count);
		dto.setPageBlock(pageBlock);
		dto.setStartPage(startPage);
		dto.setEndPage(endPage);
		dto.setPageCount(pageCount);
		
		model.addAttribute("judgeList", judgeList);
		model.addAttribute("pageDto", dto);
		
		//심사완료된 프로젝트
		int pageSize2 = 10;
		String pageNum2 = request.getParameter("pageNum2");
		
		if(pageNum2==null){
		 	pageNum2 = "1";
		}
		
		int currentPage2 = Integer.parseInt(pageNum2);
		
		PageDTO dto2 = new PageDTO();
		
		dto2.setPageSize(pageSize2);
		dto2.setPageNum(pageNum2);
		dto2.setCurrentPage(currentPage2);
		
		List<ProjectDTO> judgeFinish = adminService.judgeFinish(dto2);
		
		int count2 = adminService.getFinishCount();
		int pageBlock2 = 10; 
		int startPage2 = (currentPage2-1)/pageBlock2*pageBlock2+1;
		int endPage2 = startPage2+pageBlock2-1;
		int pageCount2 = count2/pageSize2 + ( count2 % pageSize2 == 0 ? 0 : 1);
		
		if(endPage2 > pageCount2){
	 	   endPage2 = pageCount2;
	    }
		
		dto2.setCount(count2);
		dto2.setPageBlock(pageBlock2);
		dto2.setStartPage(startPage2);
		dto2.setEndPage(endPage2);
		dto2.setPageCount(pageCount2);
		
		
		model.addAttribute("judgeFinish", judgeFinish);
		model.addAttribute("pageDto2", dto2);
		
		return "admin/projectJudge";
	}
	

	//	프로젝트 상황페이지 목록 불러오기 -> 프로젝트 심사페이지로 넘어감
	@RequestMapping(value = "/admin/projectStatus", method = RequestMethod.GET)
	public String projectStatus(HttpServletRequest request, Model model) {
		//프로젝트 진행중(승인) 게시판 페이징
		int pageSize = 10;
		String pageNum = request.getParameter("pageNum");
		
		if(pageNum==null){
		 	pageNum = "1";
		}
		
		int currentPage = Integer.parseInt(pageNum);
		
		PageDTO dto = new PageDTO();
		
		dto.setPageSize(pageSize);
		dto.setPageNum(pageNum);
		dto.setCurrentPage(currentPage);
		
		//프로젝트 진행중(승인)인 목록 불러오기
		List<ProjectDTO> projectING = adminService.projectING(dto);
		
		//진행중인 펀딩의 개수를 불러오기
		int count = adminService.getProIngCount();
		int pageBlock = 10; 
		int startPage = (currentPage-1)/pageBlock*pageBlock+1;
		int endPage = startPage+pageBlock-1;
		int pageCount = count/pageSize + ( count % pageSize == 0 ? 0 : 1);
		
		
		if(endPage > pageCount){
	 	   endPage = pageCount;
	    }
		
		dto.setCount(count);
		dto.setPageBlock(pageBlock);
		dto.setStartPage(startPage);
		dto.setEndPage(endPage);
		dto.setPageCount(pageCount);
		
		System.out.println("startPage : " + startPage);
		System.out.println("endPage : " + endPage);
		
		
		model.addAttribute("projectING", projectING);
		model.addAttribute("pageDto", dto);

		//펀딩완료(펀딩실패, 펀딩성공) 게시판 페이징
		int pageSize2 = 10;
		String pageNum2 = request.getParameter("pageNum2");
		
		if(pageNum2==null){
		 	pageNum2 = "1";
		}
		
		int currentPage2 = Integer.parseInt(pageNum);
		
		PageDTO dto2 = new PageDTO();
		
		dto2.setPageSize(pageSize2);
		dto2.setPageNum(pageNum2);
		dto2.setCurrentPage(currentPage2);
		//펀딩완료(펀딩실패, 펀딩성공) 목록 불러오기
		List<ProjectDTO> projectFinish = adminService.projectFinish(dto2);
		
		
		int count2 = adminService.getProFinishCount();
		int pageBlock2 = 10; 
		int startPage2 = (currentPage2-1)/pageBlock2*pageBlock2+1;
		int endPage2 = startPage2+pageBlock2-1;
		int pageCount2 = count2/pageSize2 + ( count2 % pageSize2 == 0 ? 0 : 1);
		
		
		if(endPage2 > pageCount2){
	 	   endPage2 = pageCount2;
	    }
		
		dto2.setCount(count2);
		dto2.setPageBlock(pageBlock2);
		dto2.setStartPage(startPage2);
		dto2.setEndPage(endPage2);
		dto2.setPageCount(pageCount2);
		
		System.out.println("startPage2 : " + startPage2);
		System.out.println("endPage2 : " + endPage2);
		
		
		model.addAttribute("projectFinish", projectFinish);
		model.addAttribute("pageDto2", dto2);
		return "admin/projectStatus";
	}
	
	
	@RequestMapping(value = "/admin/judgeDetail", method = RequestMethod.GET)
	public String judgeDetail(HttpServletRequest request, Model model) {
		String proIDX = request.getParameter("proIDX");
		model.addAttribute("proIDX", proIDX);
		return "admin/judgeDetail";
	}

// 프로젝트 승인/반려하기 (update 구문 기능) 
	@RequestMapping(value = "/admin/projectUpdate", method = RequestMethod.POST)
	public String projectUpdate(HttpServletRequest request) {
		// 이전페이지에서 proIDX값 받아오기 
		String proIDX = request.getParameter("proIDX");
		
		// judgeDetail 페이지에서 버튼값 받아오기 
		String value = request.getParameter("judge");
		
		// 해당 프로젝트의 현재 프로젝트 상태값가져오기
		String nowProStatus = adminService.getProStatus(proIDX);
		// 조건에 따라 상태값이 다르게 변경됨
		if(nowProStatus.equals("PJT02") && value.equals("승인")) {
			//플젝현황이 심사중(0)이면서 '승인'버튼을 눌렀으면 
			adminService.approvalPro(proIDX);
			
		} else if(nowProStatus.equals("PJT02") && value.equals("반려")) {
			//플젝현황이 심사중(0)이면서 '반려'버튼을 눌렀으면
			adminService.refusePro(proIDX);
			
		} else {
			return "redirect:/admin/projectJudge";
		}
		
		return "redirect:/admin/projectJudge";
	}
//	카테고리 등록 페이지 
	@RequestMapping(value = "/admin/category", method = RequestMethod.GET)	
	public String categoryInsert(HttpServletRequest request) {
		return "admin/category";
	}
	
	@RequestMapping(value = "/admin/categoryUpdate", method = RequestMethod.GET)	
	public String categoryUpdate(HttpServletRequest request) {
		String cate_register = request.getParameter("cate_register");
		
		if(cate_register.equals("수정")) {
			System.out.println("수정하자");
		} else if(cate_register.equals("삭제")) {
			System.out.println("삭제하자");
		} else { //등록
			System.out.println("등록하자");
		}
		return "redirect:/admin/category";
	}
	
//	결제현황
	@RequestMapping(value = "/admin/payStatus", method = RequestMethod.GET)	
	public String payStatus(Model model) {
		List<PaymentDTO> payStandby = adminService.payStandby();
		model.addAttribute("payStandby", payStandby);
		
		List<PaymentDTO> paySuccess = adminService.paySuccess();
		model.addAttribute("paySuccess", paySuccess);
		
		List<PaymentDTO> payFinish = adminService.payFinish();
		model.addAttribute("payFinish", payFinish);
		
		return "admin/payStatus";
	}
	
//	공지사항 
	@RequestMapping(value = "/admin/notice", method = RequestMethod.GET)	
	public String notice(Model model, HttpServletRequest request) {
		//페이징처리
		int pageSize = 10;
		String pageNum = request.getParameter("pageNum");
		
		if(pageNum==null){
		 	pageNum = "1";
		}
		
		int currentPage = Integer.parseInt(pageNum);
		
		PageDTO dto = new PageDTO();
		
		dto.setPageSize(pageSize);
		dto.setPageNum(pageNum);
		dto.setCurrentPage(currentPage);
		
		List<NoticeDTO> noticeList = adminService.getNoticeList(dto);
		
		int count = adminService.getNoticeCount();
		int pageBlock = 10; 
		int startPage = (currentPage-1)/pageBlock*pageBlock+1;
		int endPage = startPage+pageBlock-1;
		int pageCount = count/pageSize + ( count % pageSize == 0 ? 0 : 1);
		
		if(endPage > pageCount){
	 	   endPage = pageCount;
	    }
		
		dto.setCount(count);
		dto.setPageBlock(pageBlock);
		dto.setStartPage(startPage);
		dto.setEndPage(endPage);
		dto.setPageCount(pageCount);
		
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("pageDto", dto);
		
		return "admin/notice";
	}
	//글쓰기 페이지로
	@RequestMapping(value = "/admin/write", method = RequestMethod.GET)	
	public String write(HttpServletRequest request) {
		return "admin/write";
	}
	//공지사항 글 insert해주는 Pro
	@RequestMapping(value = "/admin/writePro", method = RequestMethod.POST)	
	public String writePro(NoticeDTO noticeDTO) {
		
		System.out.println(noticeDTO.getIDX());
		System.out.println(noticeDTO.getTITLE());
		System.out.println(noticeDTO.getCONTENT());
		
		// maxIDX에서 +1을 해준 상태 = newIDX
		int IDX = adminService.maxIDX();
		
		//form에서 전달받은 값을 저장해줌
		noticeDTO.setIDX(IDX);
		System.out.println(IDX + "컨트롤러");
		//새로 저장된 데이터가 담긴 DTO를 insert해줌
		adminService.insertBoard(noticeDTO);
		
		return "redirect:/admin/notice";
	}

	//공지사항 상세보기
	@RequestMapping(value = "/admin/boardDetail", method = RequestMethod.GET)	
	public String boardDetail(@RequestParam (value="IDX") int IDX, Model model) {
		//db에서 IDX를 기준으로 공지글 정보 가져오기 
		NoticeDTO noticeDTO = adminService.getBoard(IDX);
		//View로 DTO보내기
		model.addAttribute("noticeDTO", noticeDTO);
		
		return "admin/boardDetail";
	}
	
//	관리자 메일 보내기 페이지
	@RequestMapping(value = "/admin/email", method = RequestMethod.GET)	
	public String email() {
		
		return "admin/mailForm";
	}

//	관리자 메일 보내기 페이지
	@RequestMapping(value = "/admin/emailPro", method = RequestMethod.POST)	
	public String emailPro(UserDTO userDto, HttpServletRequest request) throws Exception {
		
		List<UserDTO> eventUserList = adminService.getEventUserList(userDto);
		
		for(UserDTO dto : eventUserList) {
					
					String subject = request.getParameter("subject");
					String content = request.getParameter("content");
			        String from = "omama69@gmail.com";
			        String to = dto.getId();
//			        System.out.println(subject +","+content+","+from+","+to);
			        
			        try {
			            MimeMessage mail = mailSender.createMimeMessage();
			            MimeMessageHelper mailHelper = new MimeMessageHelper(mail,"UTF-8");
			            
			            mailHelper.setFrom(from);
			            mailHelper.setTo(to);
			            mailHelper.setSubject(subject);
			            mailHelper.setText(content);
			            
			            mailSender.send(mail);
			            System.out.println("success");
			            
			        } catch(Exception e) {
			            e.printStackTrace();
			            System.out.println("fail");
			            break;
			        }
				}
		
		return "admin/mailForm";
	}
	
	
}
