package com.itwillbs.dao;

import java.util.List;
import java.util.Map;

import com.itwillbs.domain.ProjectDTO;

public interface ProjectInfoDAO {
	public List<ProjectDTO> getProjectList();
	public Map<String, String> getProjectInfo(Map<String, String> param);
	public Map<String, String> getOpenPjInfo(Map<String, String> param);
	public String getLike(Map<String, String> param);
	public int setLike(Map<String, String> param);
	public int delLike(Map<String, String> param);
	public String getAlram(Map<String, String> param);
	public int setAlram(Map<String, String> param);
	public int delAlram(Map<String, String> param);
	public int deleteAlram2();
	public List<ProjectDTO> getAlram2List(ProjectDTO projectDTO);
}
