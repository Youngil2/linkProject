package com.practice.kyi.admin_link.service;

import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;

public interface PublicDataService {
	
	 void executeAllApiConnections() throws Exception;
	 int executeApiConnection(ApiConfigVO configVO) throws Exception;
	 //void savePublicData(String tableName, List<Map<String, Object>> dataList, String mappingConfig) throws Exception;
	 
	 ApiConfigVO selectApiForm(String configId);
	 int apiConfigUpdate(ApiConfigVO vo);
	

}
