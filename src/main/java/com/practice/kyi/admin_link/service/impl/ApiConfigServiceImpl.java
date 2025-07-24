package com.practice.kyi.admin_link.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.practice.kyi.admin_link.dao.ApiConfigDAO;
import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.service.ApiConfigService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("ApiConfigService")
public class ApiConfigServiceImpl extends EgovAbstractServiceImpl implements ApiConfigService {

    
    @Autowired
    private ApiConfigDAO apiConfigDAO;
    
    @Override
    public List<ApiConfigVO> selectApiConfigList(ApiConfigVO searchVO) throws Exception {
        return apiConfigDAO.selectApiConfigList(searchVO);
    }
    
    @Override
    public List<ApiConfigVO> selectActiveApiConfigList() throws Exception {
        return apiConfigDAO.selectActiveApiConfigList();
    }
    
    @Override
    public ApiConfigVO selectApiConfig(ApiConfigVO searchVO) throws Exception {
        return apiConfigDAO.selectApiConfig(searchVO);
    }
    
    @Override
    public int insertApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return apiConfigDAO.insertApiConfig(apiConfigVO);
    }
    
    @Override
    public int updateApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return apiConfigDAO.updateApiConfig(apiConfigVO);
    }
    
    @Override
    public int deleteApiConfig(ApiConfigVO apiConfigVO) throws Exception {
        return apiConfigDAO.deleteApiConfig(apiConfigVO);
    }
}
