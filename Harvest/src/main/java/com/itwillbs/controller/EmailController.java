package com.itwillbs.controller;


import java.time.LocalDate;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.itwillbs.domain.ProjectDTO;


@Controller
public class EmailController {
	
	@Autowired
	private JavaMailSender mailSender;
	
	
	@RequestMapping(value = "/email", method = RequestMethod.GET)
    public String sendMail(ProjectDTO projectDTO, HttpSession session, HttpServletRequest request) throws Exception{
        
		LocalDate todaysDate = LocalDate.now();
		
		String subject = request.getParameter("title");
		String content = "프로젝트펀딩이 시작되었습니다. https://tumblbug.com/";
        String from = "ki6532@naver.com";
        String to = (String)session.getAttribute("id");
        
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper mailHelper = new MimeMessageHelper(mail,"UTF-8");
            
            mailHelper.setFrom(from);
            mailHelper.setTo(to);
            mailHelper.setSubject("["+subject+"] 프로젝트 펀딩이 시작되었습니다.");
            mailHelper.setText(content);
            
            mailSender.send(mail);
        	
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return "redirect:/projectList/expect";
    }



}
