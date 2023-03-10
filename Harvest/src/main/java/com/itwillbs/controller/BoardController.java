package com.itwillbs.controller;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.itwillbs.domain.BoardDTO;
import com.itwillbs.domain.PageDTO;
import com.itwillbs.service.BoardService;

@Controller
public class BoardController {
	
	@Inject
	private BoardService boardService;
	
	@RequestMapping(value = "/board/info", method = RequestMethod.GET)
	public String info() {
		return "notice/info";
	} 
	
	
	@RequestMapping(value = "/board/write", method = RequestMethod.GET)
	public String write() {
		return "notice/noticeWrite";
	} 
	
	@RequestMapping(value = "/board/writePro", method = RequestMethod.POST)
	public String writePro(BoardDTO boardDTO, HttpSession session) {
		boardService.insertBoard(boardDTO);
		return "redirect:/board/list";
	} // insertPro 메서드
	
	@RequestMapping(value = "/board/list", method = RequestMethod.GET)
	public String list(Model model, HttpServletRequest request) {
		int pageSize=5;
		String pageNum=request.getParameter("pageNum");
		if(pageNum==null) {
			pageNum="1";
		}
		int currentPage=Integer.parseInt(pageNum);
		
		PageDTO pageDTO=new PageDTO();
		// set메서드 호출
		pageDTO.setPageSize(pageSize);
		pageDTO.setPageNum(pageNum);
		pageDTO.setCurrentPage(currentPage);
		
		List<BoardDTO> boardList = boardService.getBoardList(pageDTO);
		
		int count=boardService.getBoardCount();
		int pageBlock=5;
		int startPage=(currentPage-1)/pageBlock*pageBlock+1;
		int endPage=startPage+pageBlock-1;
		int pageCount=count/pageSize+(count%pageSize== 0 ? 0 : 1);
		if(endPage>pageCount) {
			endPage=pageCount;
		}
		pageDTO.setCount(pageCount);
		pageDTO.setPageBlock(pageBlock);
		pageDTO.setStartPage(startPage);
		pageDTO.setEndPage(endPage);
		pageDTO.setPageCount(pageCount);
		
		model.addAttribute("boardList", boardList);
		model.addAttribute("pageDto",pageDTO);
		return "notice/notice"; // 폴더명/파일이름
	} // insertPro 메서드
	
	
	@RequestMapping(value = "/board/content", method = RequestMethod.GET)
	public String content(Model model, HttpServletRequest request) {
		int idx=Integer.parseInt(request.getParameter("idx"));
		BoardDTO boardDTO=boardService.getBoard(idx);
		model.addAttribute("boardDTO", boardDTO);
		return "center/content";
	} 
	
	@RequestMapping(value = "/board/update", method = RequestMethod.GET)
	public String update(HttpServletRequest request, Model model) {
		int idx=Integer.parseInt(request.getParameter("idx"));
		BoardDTO boardDTO=boardService.getBoard(idx);
		model.addAttribute("boardDTO", boardDTO);
		return "/center/updateForm"; 
	}
	
	@RequestMapping(value = "/board/updatePro", method = RequestMethod.POST)
	public String updatePro(BoardDTO boardDTO, Model model, HttpServletRequest request) {
		boardService.updateBoard(boardDTO);
		return "center/content";
	} // updatePro 메서드
	
	
	
	
	

}
