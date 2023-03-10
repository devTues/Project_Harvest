package com.itwillbs.service;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.ProjectDTO;

public interface ProjectInfoService {

	public List<ProjectDTO> getProjectList();

	public ProjectDTO getProjectInfo(Map<String, String> param);
	
	public String setAlram(Map<String, String> param);
	
	public String setLike(Map<String, String> param);
	
	public List<ProjectDTO> getAlram2List(ProjectDTO projectDTO);
	
}
