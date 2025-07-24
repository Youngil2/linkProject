package com.practice.kyi.admin_link.service;

import java.util.List;

import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;

public interface ApiConfigService {
    List<ApiConfigVO> selectApiConfigList(ApiConfigVO searchVO) throws Exception;
    List<ApiConfigVO> selectActiveApiConfigList() throws Exception;
    ApiConfigVO selectApiConfig(ApiConfigVO searchVO) throws Exception;
    int insertApiConfig(ApiConfigVO apiConfigVO) throws Exception;
    int updateApiConfig(ApiConfigVO apiConfigVO) throws Exception;
    int deleteApiConfig(ApiConfigVO apiConfigVO) throws Exception;
}