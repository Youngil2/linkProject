package com.practice.kyi.admin_link.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.practice.kyi.admin_link.dao.ApiConfigDAO;
import com.practice.kyi.admin_link.dao.PublicDataDAO;
import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.dao.vo.PublicDataVO;
import com.practice.kyi.admin_link.service.PublicDataService;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("PublicDataService")
public class PublicDataServiceImpl extends EgovAbstractServiceImpl implements PublicDataService {
    
    @Autowired
    private ApiConfigDAO apiConfigDAO;
    
    @Autowired
    private PublicDataDAO publicDataDAO;
    
    private static final Logger logger = LoggerFactory.getLogger(PublicDataServiceImpl.class);
    
    /**
     * 모든 활성 API 연계 실행
     */
    @Override
    public void executeAllApiConnections() throws Exception {
        List<ApiConfigVO> activeConfigs = apiConfigDAO.selectActiveApiConfigList();
        
        for (ApiConfigVO config : activeConfigs) {
            try {
                executeApiConnection(config);
                logger.info("API 연계 완료: {}", config.getApiName());
            } catch (Exception e) {
                logger.error("API 연계 실패 - {}: {}", config.getApiName(), e.getMessage());
                // 개별 실패는 로그만 남기고 계속 진행
            }
        }
    }
    
    /**
     * 특정 API 연계 실행 - 응답 데이터 검증 강화 및 조건부 TRUNCATE 적용
     */
    @Override
    public int executeApiConnection(ApiConfigVO configVO) throws Exception {
    	try {
            logger.info("API 연계 시작 - {}", configVO.getApiName());
            
            int currentPage = 1;
            int totalProcessedCount = 0;
            boolean hasMoreData = true;
            boolean isFirstPage = true;
            
            // 페이지별로 호출하고 즉시 저장
            while (hasMoreData) {
                logger.info("페이지 {} 처리 중 - {}", currentPage, configVO.getApiName());
                
                // 1. 현재 페이지 API 호출
                String responseData = callPublicApiWithPaging(configVO, currentPage);
                
                // 2. 응답 데이터 검증
                if (responseData == null || responseData.trim().isEmpty()) {
                    logger.warn("페이지 {} - API 응답 데이터가 비어있습니다. - {}", currentPage, configVO.getApiName());
                    break;
                }
                
                // 3. 응답 데이터 형식 감지 및 파싱
                String actualFormat = detectResponseFormat(responseData);
                List<Map<String, Object>> parsedData = parseApiResponse(responseData, actualFormat);
                
                // 4. 파싱된 데이터 확인
                if (parsedData == null || parsedData.isEmpty()) {
                    logger.info("페이지 {} - 파싱된 데이터가 없습니다. 페이징 종료 - {}", currentPage, configVO.getApiName());
                    break;
                }
                
                // 5. 현재 페이지 데이터 즉시 저장
                savePageDataImmediately(configVO.getTargetTable(), parsedData, configVO.getMappingConfig(), isFirstPage);
                totalProcessedCount += parsedData.size();
                
                logger.info("페이지 {} 저장 완료 - {}: {} 건 (누적: {} 건)", 
                           currentPage, configVO.getApiName(), parsedData.size(), totalProcessedCount);
                
                // 6. 다음 페이지 존재 여부 확인
                hasMoreData = checkHasMoreData(responseData, parsedData, currentPage, configVO);
                
                // 디버깅 로그 추가
                logger.info("=== 페이징 디버깅 정보 ===");
                logger.info("현재 페이지: {}, 현재 페이지 데이터 건수: {}, 누적 데이터 건수: {}", 
                           currentPage, parsedData.size(), totalProcessedCount);
                logger.info("다음 페이지 존재 여부: {}", hasMoreData);
                
                if (hasMoreData) {
                    currentPage++;
                    isFirstPage = false; // 첫 페이지 이후로는 false
                    
                    // 과도한 요청 방지를 위한 지연
                    Thread.sleep(100); // 0.1초 대기
                } else {
                    logger.info("페이징 종료 - 마지막 페이지: {}", currentPage);
                }
            }
            
            logger.info("전체 페이징 완료 - {}: 총 {} 페이지, {} 건", 
                       configVO.getApiName(), currentPage, totalProcessedCount);
            return totalProcessedCount;
            
        } catch (Exception e) {
            logger.error("API 연계 중 오류 - {}: {}", configVO.getApiName(), e.getMessage());
            throw e;
        }
    }
    /**
     * 페이지별 데이터 즉시 저장 - 첫 페이지는 조건부 TRUNCATE, 이후 페이지는 APPEND
     */
    private void savePageDataImmediately(String fullTableName, List<Map<String, Object>> dataList, 
                                       String mappingConfig, boolean isFirstPage) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            logger.info("저장할 데이터가 없습니다. - 테이블: {}, 첫 페이지: {}", fullTableName, isFirstPage);
            return;
        }
        
        try {
            // 1. 테이블 정보 설정
            PublicDataVO tableInfo = new PublicDataVO();
            tableInfo.parseFullTableName(fullTableName);
            
            // 2. 테이블 존재 여부 확인
            if (publicDataDAO.checkTableExists(tableInfo) == 0) {
                throw new Exception("테이블이 존재하지 않습니다: " + fullTableName);
            }
            
            // 3. 테이블 컬럼 정보 조회 (첫 페이지에서만)
            if (isFirstPage) {
                List<Map<String, Object>> tableColumns = publicDataDAO.selectTableColumns(tableInfo);
                logger.info("=== 테이블 컬럼 조회 (첫 페이지) ===");
                logger.info("테이블 정보 - 스키마: {}, 테이블명: {}", tableInfo.getSchemaName(), tableInfo.getTableName());
                logger.info("조회된 컬럼 정보: {}", tableColumns);
            }
            
            Set<String> validColumns = getValidColumns(publicDataDAO.selectTableColumns(tableInfo));
            
            // 4. 데이터 전처리
            Map<String, String> columnMapping = parseColumnMapping(mappingConfig);
            List<PublicDataVO> processedDataList = new ArrayList<>();
            
            for (Map<String, Object> data : dataList) {
                Map<String, Object> processedData = processDataForTruncateInsert(data, columnMapping, validColumns);
                if (!processedData.isEmpty()) {
                    PublicDataVO vo = createPublicDataVOFromMap(fullTableName, processedData);
                    processedDataList.add(vo);
                }
            }
            
            // 5. 처리된 데이터가 없는 경우
            if (processedDataList.isEmpty()) {
                logger.info("매핑된 유효한 데이터가 없습니다. - 테이블: {}, 원본 데이터 건수: {}, 첫 페이지: {}", 
                           fullTableName, dataList.size(), isFirstPage);
                return;
            }
            
            // 6. 첫 페이지는 조건부 TRUNCATE + INSERT, 이후 페이지는 APPEND INSERT
            if (isFirstPage) {
                // 첫 페이지: 기존 데이터가 있으면 TRUNCATE 후 INSERT
                publicDataDAO.conditionalTruncateAndInsertData(tableInfo, processedDataList);
                logger.info("첫 페이지 데이터 저장 완료 (조건부 TRUNCATE) - 테이블: {}, 저장 건수: {}", 
                           fullTableName, processedDataList.size());
            } else {
                // 이후 페이지: APPEND INSERT만 수행
                publicDataDAO.appendInsertData(tableInfo, processedDataList);
                logger.info("페이지 데이터 추가 저장 완료 (APPEND) - 테이블: {}, 저장 건수: {}", 
                           fullTableName, processedDataList.size());
            }
            
        } catch (Exception e) {
            logger.error("페이지 데이터 저장 실패 - 테이블: {}, 첫 페이지: {}", fullTableName, isFirstPage, e);
            throw e;
        }
    }
    /**
     * 페이징 처리된 공공데이터 API 호출
     */
    private String callPublicApiWithPaging(ApiConfigVO configVO, int pageIndex) throws Exception {
        String apiUrl = null;
        HttpURLConnection urlConn = null;
        
        try {
            // URL 생성 (페이지 정보 포함)
            apiUrl = buildApiUrlWithPaging(configVO, pageIndex);
            logger.info("페이지 {} API 호출 URL: {}", pageIndex, apiUrl);
            
            // 기존 callPublicApi와 동일한 로직
            URL url = new URL(apiUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            
            urlConn.setRequestMethod("GET");
            urlConn.setConnectTimeout(10000);
            urlConn.setReadTimeout(30000);
            urlConn.setDoOutput(false);
            
            urlConn.setRequestProperty("User-Agent", "Java/" + System.getProperty("java.version"));
            urlConn.setRequestProperty("Accept", "*/*");
            
            int responseCode = urlConn.getResponseCode();
            logger.info("페이지 {} 응답 코드: {}", pageIndex, responseCode);
            
            StringBuilder responseBody = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 
                            ? urlConn.getInputStream() 
                            : urlConn.getErrorStream(), 
                        StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line).append('\n');
                }
            }
            
            String response = responseBody.toString();
            
            if (responseCode >= 200 && responseCode < 300) {
                if (response == null || response.trim().isEmpty()) {
                    throw new Exception("페이지 " + pageIndex + " - API 응답 본문이 비어있습니다.");
                }
                
                // 서비스 키 관련 에러 체크
                if (response.contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR") || 
                    response.contains("SERVICE ERROR") ||
                    response.contains("INVALID_REQUEST_PARAMETER_ERROR")) {
                    throw new Exception("서비스 키 오류: " + response.substring(0, Math.min(300, response.length())));
                }
                
                return response;
            } else {
                throw new Exception("페이지 " + pageIndex + " - API 호출 실패 - HTTP " + responseCode);
            }
            
        } catch (Exception e) {
            logger.error("페이지 {} API 호출 오류: {}", pageIndex, e.getMessage());
            throw e;
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
    }
    private void addDynamicParameters(StringBuilder urlBuilder, String parameterConfig, int pageIndex) {
        logger.info("=== addDynamicParameters 시작 ===");
        logger.info("입력된 parameterConfig: {}", parameterConfig);
        logger.info("현재 pageIndex: {}", pageIndex);
        logger.info("파라미터 추가 전 URL: {}", urlBuilder.toString());
        
        if (parameterConfig == null || parameterConfig.trim().isEmpty()) {
            logger.warn("parameterConfig가 비어 있어 파라미터를 추가하지 않습니다.");
            return;
        }

        try {
            logger.info("=== 파라미터 JSON 원문 ===");
            logger.info(parameterConfig);

            ObjectMapper mapper = new ObjectMapper();

            // 페이징 관련 파라미터 목록 (일반적으로 사용되는 페이징 파라미터들)
            Set<String> pagingParams = Set.of("pageIndex", "currentPage", "page", "pageNo", "pageNum");

            // 파라미터 설정이 JSON 배열인지 확인
            if (parameterConfig.trim().startsWith("[")) {
                logger.info("JSON 배열 형태로 파라미터 파싱 시도");
                
                List<Map<String, String>> parameters = mapper.readValue(
                    parameterConfig,
                    new TypeReference<List<Map<String, String>>>() {}
                );

                logger.info("파싱된 파라미터 수: {}", parameters.size());

                for (int i = 0; i < parameters.size(); i++) {
                    Map<String, String> param = parameters.get(i);
                    logger.info("파라미터 {} 처리 중: {}", i, param);
                    
                    String paramName = param.get("paramName");
                    String paramValue = param.get("paramValue");

                    // paramName이 null이거나 비어있는 경우 다른 키들도 확인
                    if (paramName == null || paramName.trim().isEmpty()) {
                        paramName = param.get("name");
                        if (paramName == null) paramName = param.get("key");
                        if (paramName == null) paramName = param.get("parameter");
                        logger.info("대체 paramName 찾기 결과: {}", paramName);
                    }

                    // paramValue가 null이거나 비어있는 경우 다른 키들도 확인
                    if (paramValue == null || paramValue.trim().isEmpty()) {
                        paramValue = param.get("value");
                        if (paramValue == null) paramValue = param.get("val");
                        logger.info("대체 paramValue 찾기 결과: {}", paramValue);
                    }

                    logger.info("최종 파라미터 확인 - Name: '{}', Value: '{}'", paramName, paramValue);

                    if (paramName != null && !paramName.trim().isEmpty()) {
                        String beforeAdd = urlBuilder.toString();
                        
                        // 페이징 관련 파라미터인 경우 동적으로 pageIndex 값 사용
                        if (pagingParams.contains(paramName.trim())) {
                            paramValue = String.valueOf(pageIndex);
                            logger.info("페이징 파라미터 감지 - {}: {} -> {}", paramName.trim(), param.get("paramValue"), paramValue);
                        }
                        
                        urlBuilder.append("&").append(URLEncoder.encode(paramName.trim(), StandardCharsets.UTF_8));

                        if (paramValue != null && !paramValue.trim().isEmpty()) {
                            urlBuilder.append("=").append(URLEncoder.encode(paramValue.trim(), StandardCharsets.UTF_8));
                        } else {
                            urlBuilder.append("=");
                        }

                        String afterAdd = urlBuilder.toString();
                        logger.info("파라미터 추가됨:");
                        logger.info("  이전: {}", beforeAdd);
                        logger.info("  이후: {}", afterAdd);
                        logger.info("  추가된 부분: {}={}", paramName.trim(), paramValue);
                    } else {
                        logger.warn("파라미터 이름이 비어있음. 전체 파라미터: {}", param);
                    }
                }
            } else if (parameterConfig.trim().startsWith("{")) {
                logger.info("JSON 객체 형태로 파라미터 파싱 시도");
                
                Map<String, String> parameters = mapper.readValue(
                    parameterConfig,
                    new TypeReference<Map<String, String>>() {}
                );

                logger.info("파싱된 파라미터 맵: {}", parameters);

                for (Map.Entry<String, String> entry : parameters.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue();

                    // 페이징 관련 파라미터인 경우 동적으로 pageIndex 값 사용
                    if (pagingParams.contains(paramName.trim())) {
                        paramValue = String.valueOf(pageIndex);
                        logger.info("페이징 파라미터 감지 - {}: {} -> {}", paramName.trim(), entry.getValue(), paramValue);
                    }

                    logger.info("파라미터 처리: {} = {}", paramName, paramValue);

                    if (paramName != null && !paramName.trim().isEmpty()) {
                        urlBuilder.append("&").append(URLEncoder.encode(paramName.trim(), StandardCharsets.UTF_8));

                        if (paramValue != null && !paramValue.trim().isEmpty()) {
                            urlBuilder.append("=").append(URLEncoder.encode(paramValue.trim(), StandardCharsets.UTF_8));
                        } else {
                            urlBuilder.append("=");
                        }

                        logger.info("추가된 파라미터: {}={}", paramName.trim(), paramValue);
                    }
                }
            } else {
                logger.info("일반 텍스트 형태로 파라미터 파싱 시도");
                
                String[] pairs = parameterConfig.split("&");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] keyValue = pair.split("=", 2);
                        String key = keyValue[0].trim();
                        String value = keyValue.length > 1 ? keyValue[1].trim() : "";
                        
                        // 페이징 관련 파라미터인 경우 동적으로 pageIndex 값 사용
                        if (pagingParams.contains(key)) {
                            value = String.valueOf(pageIndex);
                            logger.info("페이징 파라미터 감지 - {}: {} -> {}", key, keyValue.length > 1 ? keyValue[1].trim() : "", value);
                        }
                        
                        if (!key.isEmpty()) {
                            urlBuilder.append("&").append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                            urlBuilder.append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                            logger.info("추가된 파라미터: {}={}", key, value);
                        }
                    }
                }
            }
            
            logger.info("=== addDynamicParameters 완료 ===");
            logger.info("최종 URL: {}", urlBuilder.toString());
            
        } catch (Exception e) {
            logger.error("파라미터 설정 파싱 오류: {}", e.getMessage(), e);
            logger.error("문제의 parameterConfig 내용: {}", parameterConfig);
        }
    }
    
    /**
     * 페이징 정보가 포함된 API URL 생성
     */
    private String buildApiUrlWithPaging(ApiConfigVO configVO, int pageIndex) {
    	try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(configVO.getBaseUrl().trim());
            
            String processedServiceKey = processServiceKey(configVO.getServiceKey()).trim();
            
            char separator = configVO.getBaseUrl().contains("?") ? '&' : '?';
            urlBuilder.append(separator).append("serviceKey=").append(processedServiceKey);
            
            // 기존 동적 파라미터 추가 (페이징 파라미터 포함)
            String parameterConfig = configVO.getParameterConfig();
            if (parameterConfig != null && !parameterConfig.trim().isEmpty()) {
                addDynamicParameters(urlBuilder, parameterConfig, pageIndex);
            }
            
            String finalUrl = urlBuilder.toString();
            logger.info("페이지 {} URL 생성 완료: {}", pageIndex, finalUrl);
            
            return finalUrl;
            
        } catch (Exception e) {
            logger.error("페이징 URL 생성 중 오류 발생", e);
            throw new RuntimeException("페이징 URL 생성 실패: " + e.getMessage(), e);
        }
    }


	/**
	 * 한 페이지당 조회 건수 설정 (기본값 또는 설정값)
	 */
	private int getNumOfRows(ApiConfigVO configVO) {
	    // parameterConfig에서 numOfRows, perPage 등의 값을 찾거나 기본값 사용
	    String paramConfig = configVO.getParameterConfig();
	    if (paramConfig != null && !paramConfig.trim().isEmpty()) {
	        try {
	            ObjectMapper mapper = new ObjectMapper();
	            
	            if (paramConfig.trim().startsWith("[")) {
	                // JSON 배열 형태
	                List<Map<String, String>> parameters = mapper.readValue(
	                    paramConfig,
	                    new TypeReference<List<Map<String, String>>>() {}
	                );
	                
	                for (Map<String, String> param : parameters) {
	                    String paramName = param.get("paramName");
	                    if (paramName == null) paramName = param.get("name");
	                    if (paramName == null) paramName = param.get("key");
	                    
	                    if (paramName != null && 
	                        (paramName.equals("numOfRows") || paramName.equals("perPage") || 
	                         paramName.equals("pageSize") || paramName.equals("limit"))) {
	                        
	                        String paramValue = param.get("paramValue");
	                        if (paramValue == null) paramValue = param.get("value");
	                        
	                        if (paramValue != null && !paramValue.trim().isEmpty()) {
	                            return Integer.parseInt(paramValue.trim());
	                        }
	                    }
	                }
	            } else if (paramConfig.trim().startsWith("{")) {
	                // JSON 객체 형태
	                Map<String, String> params = mapper.readValue(paramConfig, Map.class);
	                
	                for (String key : Arrays.asList("numOfRows", "perPage", "pageSize", "limit")) {
	                    String value = params.get(key);
	                    if (value != null && !value.trim().isEmpty()) {
	                        return Integer.parseInt(value.trim());
	                    }
	                }
	            }
	        } catch (Exception e) {
	            logger.warn("numOfRows 파싱 실패, 기본값 사용: {}", e.getMessage());
	        }
	    }
	    
	    return 1000;
	}
    /**
     * 다음 페이지 데이터 존재 여부 확인
     */
    private boolean checkHasMoreData(String responseData, List<Map<String, Object>> parsedData, 
                                    int currentPage, ApiConfigVO configVO) {
        try {
            // 1. 파싱된 데이터가 없으면 종료
            if (parsedData == null || parsedData.isEmpty()) {
                logger.info("파싱된 데이터가 없으므로 페이징 종료");
                return false;
            }
            
            // 2. JSON 응답에서 totalCount 확인
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseData);
            
            if (rootNode.has("response") && rootNode.get("response").has("body")) {
                JsonNode body = rootNode.get("response").get("body");
                
                if (body.has("totalCount")) {
                    int totalCount = body.get("totalCount").asInt();
                    
                    // numOfRows는 API 응답에서 실제 값을 가져오되, 없으면 현재 페이지 데이터 건수 사용
                    int actualNumOfRows = parsedData.size();
                    if (body.has("numOfRows")) {
                        actualNumOfRows = body.get("numOfRows").asInt();
                    }
                    
                    int processedCount = (currentPage - 1) * actualNumOfRows + parsedData.size();
                    
                    logger.info("페이징 정보 - 총 건수: {}, 실제 페이지당 건수: {}, 현재까지 처리된 건수: {}", 
                               totalCount, actualNumOfRows, processedCount);
                    
                    return processedCount < totalCount;
                }
            }
            
            // 3. totalCount 정보가 없는 경우, 현재 페이지 데이터 건수로 판단
            // 일반적으로 마지막 페이지는 설정된 numOfRows보다 적은 데이터를 반환함
            int configuredNumOfRows = getNumOfRows(configVO);
            
            // 현재 페이지에서 반환된 데이터가 설정된 페이지당 건수와 같으면 더 있을 가능성이 높음
            boolean hasMore = parsedData.size() >= configuredNumOfRows;
            
            logger.info("데이터 건수 기반 판단 - 현재 페이지 건수: {}, 설정된 페이지당 건수: {}, 다음 페이지 존재 여부: {}", 
                       parsedData.size(), configuredNumOfRows, hasMore);
            
            return hasMore;
            
        } catch (Exception e) {
            logger.error("다음 페이지 존재 여부 확인 중 오류: {}", e.getMessage());
            return false; // 오류 시 안전하게 종료
        }
    }
    /**
     * 공공데이터 DB 저장 (조건부 TRUNCATE + INSERT 방식) - 에러 처리 개선
     */
    public void savePublicDataWithConditionalTruncate(String fullTableName, List<Map<String, Object>> dataList, String mappingConfig) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            logger.info("저장할 데이터가 없습니다. - 테이블: {}", fullTableName);
            return; // 에러가 아닌 정상 종료
        }
        try {
            // 1. 테이블 정보 설정
            PublicDataVO tableInfo = new PublicDataVO();
            tableInfo.parseFullTableName(fullTableName);
            
            // 2. 테이블 존재 여부 확인
            if (publicDataDAO.checkTableExists(tableInfo) == 0) {
                throw new Exception("테이블이 존재하지 않습니다: " + fullTableName);
            }
            
            // 3. 테이블 컬럼 정보 조회
            List<Map<String, Object>> tableColumns = publicDataDAO.selectTableColumns(tableInfo);
            Map<String, Object> col = tableColumns.get(0);
            for (String key : col.keySet()) {
                System.out.println("Key = " + key + ", Value = " + col.get(key));
            }
            // 디버깅 로그 추가
            logger.info("=== 테이블 컬럼 조회 디버깅 ===");
            logger.info("테이블 정보 - 스키마: {}, 테이블명: {}", tableInfo.getSchemaName(), tableInfo.getTableName());
            logger.info("조회된 컬럼 정보: {}", tableColumns);
            logger.info("컬럼 개수: {}", tableColumns != null ? tableColumns.size() : "null");


            Set<String> validColumns = getValidColumns(tableColumns);

            logger.info("최종 유효 컬럼: {}", validColumns);
            // 4. 데이터 전처리
            Map<String, String> columnMapping = parseColumnMapping(mappingConfig);
            List<PublicDataVO> processedDataList = new ArrayList<>();
            
            for (Map<String, Object> data : dataList) {
                Map<String, Object> processedData = processDataForTruncateInsert(data, columnMapping, validColumns);
                if (!processedData.isEmpty()) {
                    PublicDataVO vo = createPublicDataVOFromMap(fullTableName, processedData);
                    processedDataList.add(vo);
                }
            }
            
            // 5. 처리된 데이터가 없는 경우 - 에러가 아닌 정상 처리
            if (processedDataList.isEmpty()) {
                logger.info("매핑된 유효한 데이터가 없습니다. - 테이블: {}, 원본 데이터 건수: {}", 
                           fullTableName, dataList.size());
                logger.info("컬럼 매핑 정보: {}", columnMapping.isEmpty() ? "기본 매핑 사용" : columnMapping);
                logger.info("테이블 유효 컬럼: {}", validColumns);
                return; // 에러가 아닌 정상 종료
            }
            
            // 6. 조건부 TRUNCATE + INSERT 실행
            publicDataDAO.conditionalTruncateAndInsertData(tableInfo, processedDataList);
            
            logger.info("데이터 저장 완료 - 테이블: {}, 저장 건수: {}", fullTableName, processedDataList.size());
            
        } catch (Exception e) {
            logger.error("데이터 저장 실패 - 테이블: {}", fullTableName, e);
            throw e;
        }
    }
    /**
     * 유효한 DB 컬럼명 추출 (데이터 타입 기반이 아닌, 컬럼명 기준)
     */
    private Set<String> getValidColumns(List<Map<String, Object>> tableColumns) {
        Set<String> validColumns = new HashSet<>();

        for (Map<String, Object> column : tableColumns) {
            String columnName = (String) column.get("columnname");
            if (columnName == null) continue;

            // 필요 시 제외할 컬럼
            if ("sync_dt".equalsIgnoreCase(columnName)) continue;

            validColumns.add(columnName);
        }

        logger.info("=== 추출된 유효 컬럼명 목록 ===");
        logger.info("{}", validColumns);

        return validColumns;
    }
    /**
     * 응답 데이터 형식 자동 감지
     */
    private String detectResponseFormat(String responseData) {
        if (responseData == null || responseData.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String trimmed = responseData.trim();
        
        // HTML 응답 체크
        if (trimmed.toLowerCase().startsWith("<!doctype html") || 
            trimmed.toLowerCase().startsWith("<html")) {
            return "HTML";
        }
        
        // XML 응답 체크
        if (trimmed.startsWith("<?xml") || trimmed.startsWith("<")) {
            return "XML";
        }
        
        // JSON 응답 체크
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "JSON";
        }
        
        return "UNKNOWN";
    }
    
    
    /**
     * 서비스 키 처리 개선 - 공공데이터포털 특화
     */
    private String processServiceKey(String serviceKey) {
        try {
            logger.info("원본 서비스키 길이: {}, 내용: {}", serviceKey.length(), serviceKey);
            
            // 1. 공백 제거
            String trimmedKey = serviceKey.trim();
            
            // 2. 이미 인코딩된 키인지 확인 (%가 포함되어 있으면 인코딩된 것으로 간주)
            if (trimmedKey.contains("%")) {
                // 이미 인코딩된 경우, 디코딩 후 재인코딩
                try {
                    String decoded = URLDecoder.decode(trimmedKey, StandardCharsets.UTF_8);
                    String reEncoded = URLEncoder.encode(decoded, StandardCharsets.UTF_8);
                    logger.info("재인코딩된 서비스키: {}", reEncoded);
                    return reEncoded;
                } catch (Exception e) {
                    logger.warn("서비스키 디코딩 실패, 원본 사용: {}", e.getMessage());
                    return trimmedKey;
                }
            } else {
                // 인코딩되지 않은 경우, 인코딩 적용
                String encoded = URLEncoder.encode(trimmedKey, StandardCharsets.UTF_8);
                logger.info("새로 인코딩된 서비스키: {}", encoded);
                return encoded;
            }
            
        } catch (Exception e) {
            logger.error("서비스키 처리 중 오류: {}", e.getMessage(), e);
            return serviceKey; // 오류 시 원본 반환
        }
    }
    

    /**
     * API 응답 데이터 파싱 - 오류 처리 강화
     */
    private List<Map<String, Object>> parseApiResponse(String responseData, String format) throws Exception {
        if (responseData == null || responseData.trim().isEmpty()) {
            throw new Exception("파싱할 응답 데이터가 없습니다.");
        }

        // HTML 응답인 경우 오류 처리
        if ("HTML".equals(format)) {
            logger.error("HTML 응답을 받았습니다. 오류 페이지일 가능성이 높습니다.");
            logger.error("HTML 내용: {}", responseData.length() > 1000 ?
                responseData.substring(0, 1000) + "..." : responseData);
            throw new Exception("API에서 HTML 응답(오류 페이지)을 받았습니다. API URL과 파라미터를 확인해주세요.");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if ("JSON".equalsIgnoreCase(format)) {
                return parseJsonResponse(responseData, objectMapper);
            } else if ("XML".equalsIgnoreCase(format)) {
                return parseXmlResponse(responseData);
            } else {
                throw new Exception("지원하지 않는 응답 형식: " + format +
                    ". 지원 형식: JSON, XML");
            }
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            logger.error("JSON 파싱 오류 발생");
            logger.error("오류 위치: line {}, column {}", e.getLocation().getLineNr(), e.getLocation().getColumnNr());
            logger.error("파싱 시도한 데이터: {}", responseData.length() > 500 ?
                responseData.substring(0, 500) + "..." : responseData);
            throw new Exception("JSON 파싱 실패: " + e.getMessage() +
                ". 응답 데이터가 유효한 JSON 형식이 아닙니다.");
        } catch (Exception e) {
            logger.error("응답 데이터 파싱 중 오류 발생: {}", e.getMessage());
            throw new Exception("응답 데이터 파싱 실패: " + e.getMessage());
        }
    }
    
    /**
     * JSON 응답 파싱 - 에러 처리 개선 및 빈 데이터 정상 처리
     */
    private List<Map<String, Object>> parseJsonResponse(String jsonData, ObjectMapper objectMapper) throws Exception {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonData);
            List<Map<String, Object>> result = new ArrayList<>();
            
            logger.info("JSON 루트 노드 구조: {}", rootNode.getNodeType());
            
            // 데이터 추출을 위한 다단계 탐색
            JsonNode dataNode = findDataNode(rootNode);
            
            if (dataNode != null) {
                result = extractDataFromNode(dataNode, objectMapper);
            }
            
            if (result.isEmpty()) {
                logger.info("파싱 가능한 데이터가 없습니다. (정상 상황)");
                logger.debug("전체 JSON 구조: {}", objectMapper.writeValueAsString(rootNode));
            } else {
                logger.info("JSON 파싱 완료, 결과 건수: {}", result.size());
            }
            
            return result;
            
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            logger.error("JSON 파싱 예외 발생: {}", e.getMessage());
            throw e;
        }
    }
    /**
     * JSON 노드에서 실제 데이터 노드를 찾는 메서드
     */
    private JsonNode findDataNode(JsonNode rootNode) {
        logger.info("데이터 노드 탐색 시작");
        
        // 1. 공공데이터 포털 표준 구조들 확인
        JsonNode dataNode = checkStandardStructures(rootNode);
        if (dataNode != null) {
            return dataNode;
        }
        
        // 2. 일반적인 데이터 필드명들로 탐색
        dataNode = checkCommonDataFields(rootNode);
        if (dataNode != null) {
            return dataNode;
        }
        
        // 3. 배열이나 객체를 포함한 필드 자동 탐색
        dataNode = autoDetectDataField(rootNode);
        if (dataNode != null) {
            return dataNode;
        }
        
        // 4. 루트 자체가 데이터인 경우
        if (rootNode.isArray() || (rootNode.isObject() && isDataObject(rootNode))) {
            logger.info("루트 노드 자체가 데이터입니다.");
            return rootNode;
        }
        
        return null;
    }
    /**
     * 배열이나 객체를 포함한 필드 자동 탐색
     */
    private JsonNode autoDetectDataField(JsonNode rootNode) {
        if (!rootNode.isObject()) {
            return null;
        }
        
        // 객체의 모든 필드를 순회하며 데이터 필드 찾기
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        List<JsonNode> candidateNodes = new ArrayList<>();
        
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            
            // 메타데이터성 필드는 제외
            if (isMetadataField(fieldName)) {
                continue;
            }
            
            // 배열이거나 객체인 경우 후보로 추가
            if (fieldValue.isArray() || (fieldValue.isObject() && isDataObject(fieldValue))) {
                logger.info("데이터 후보 필드 발견: '{}', 타입: {}", fieldName, fieldValue.getNodeType());
                candidateNodes.add(fieldValue);
            }
        }
        
        // 가장 적합한 후보 선택 (배열 우선, 크기가 큰 것 우선)
        if (!candidateNodes.isEmpty()) {
            candidateNodes.sort((a, b) -> {
                // 배열 우선
                if (a.isArray() && !b.isArray()) return -1;
                if (!a.isArray() && b.isArray()) return 1;
                
                // 크기 비교
                int sizeA = a.isArray() ? a.size() : 1;
                int sizeB = b.isArray() ? b.size() : 1;
                return Integer.compare(sizeB, sizeA);
            });
            
            return candidateNodes.get(0);
        }
        
        return null;
    }

    /**
     * 메타데이터 필드인지 확인
     */
    private boolean isMetadataField(String fieldName) {
        String[] metadataFields = {
            "resultCode", "resultMsg", "returnCode", "returnMsg", 
            "totalCount", "pageNo", "numOfRows", "pageSize", "totalPages",
            "status", "statusCode", "message", "error", "success",
            "timestamp", "version", "api_version", "requestId"
        };
        
        String lowerFieldName = fieldName.toLowerCase();
        for (String metaField : metadataFields) {
            if (lowerFieldName.contains(metaField.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 데이터 객체인지 확인 (메타데이터만 있는 객체가 아닌지)
     */
    private boolean isDataObject(JsonNode node) {
        if (!node.isObject()) {
            return false;
        }
        
        // 객체의 필드 중 메타데이터가 아닌 필드가 있는지 확인
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!isMetadataField(fieldName)) {
                return true;
            }
        }
        
        return false;
    }


	/**
	 * 공공데이터 포털 표준 구조들 확인
	 */
	private JsonNode checkStandardStructures(JsonNode rootNode) {
	    // 1. response > body > items > item 구조
	    if (rootNode.has("response")) {
	        JsonNode response = rootNode.get("response");
	        logger.info("response 노드 존재");
	        
	        if (response.has("body")) {
	            JsonNode body = response.get("body");
	            logger.info("body 노드 존재");
	            
	            // 오류 응답 체크
	            if (body.has("resultCode")) {
	                String resultCode = body.get("resultCode").asText();
	                String resultMsg = body.has("resultMsg") ? body.get("resultMsg").asText() : "Unknown error";
	                
	                if (!"00".equals(resultCode)) {
	                    throw new RuntimeException("API 오류 응답 - 코드: " + resultCode + ", 메시지: " + resultMsg);
	                }
	            }
	            
	            // totalCount 확인
	            if (body.has("totalCount")) {
	                int totalCount = body.get("totalCount").asInt();
	                logger.info("총 데이터 건수: {}", totalCount);
	                
	                if (totalCount == 0) {
	                    logger.info("API에서 반환된 데이터가 0건입니다.");
	                    return null;
	                }
	            }
	            
	            // items 확인
	            if (body.has("items")) {
	                JsonNode items = body.get("items");
	                logger.info("items 노드 존재, 타입: {}", items.getNodeType());
	                
	                if (items.isNull() || (items.isArray() && items.size() == 0)) {
	                    logger.info("items가 비어있습니다.");
	                    return null;
	                }
	                
	                if (items.has("item")) {
	                    return items.get("item");
	                }
	                return items;
	            }
	        }
	    }
	    
	    // 2. body > items 구조 (response 없이)
	    if (rootNode.has("body")) {
	        JsonNode body = rootNode.get("body");
	        if (body.has("items")) {
	            JsonNode items = body.get("items");
	            if (items.has("item")) {
	                return items.get("item");
	            }
	            return items;
	        }
	    }
	    
	    return null;
	}
	
	/**
	 * 일반적인 데이터 필드명들로 탐색
	 */
	private JsonNode checkCommonDataFields(JsonNode rootNode) {
	    String[] commonDataFields = {
	        "data", "result", "results", "list", "items", "item", 
	        "records", "rows", "content", "contents", "payload"
	    };
	    
	    for (String fieldName : commonDataFields) {
	        if (rootNode.has(fieldName)) {
	            JsonNode fieldNode = rootNode.get(fieldName);
	            logger.info("공통 데이터 필드 '{}' 발견, 타입: {}", fieldName, fieldNode.getNodeType());
	            
	            if (fieldNode.isNull() || (fieldNode.isArray() && fieldNode.size() == 0)) {
	                continue;
	            }
	            
	            return fieldNode;
	        }
	    }
	    
	    return null;
	}
	/**
	 * 데이터 노드에서 실제 데이터 추출
	 */
	private List<Map<String, Object>> extractDataFromNode(JsonNode dataNode, ObjectMapper objectMapper) {
	    List<Map<String, Object>> result = new ArrayList<>();
	    
	    if (dataNode.isArray()) {
	        logger.info("배열 형태 데이터, 크기: {}", dataNode.size());
	        
	        if (dataNode.size() == 0) {
	            logger.info("배열이 비어있습니다.");
	            return result;
	        }
	        
	        for (JsonNode item : dataNode) {
	            if (item.isObject()) {
	                Map<String, Object> dataMap = objectMapper.convertValue(item, Map.class);
	                result.add(dataMap);
	            } else {
	                // 배열 내부가 객체가 아닌 경우 (예: 문자열 배열)
	                Map<String, Object> dataMap = new HashMap<>();
	                dataMap.put("value", objectMapper.convertValue(item, Object.class));
	                result.add(dataMap);
	            }
	        }
	    } else if (dataNode.isObject()) {
	        logger.info("단일 객체 형태 데이터");
	        Map<String, Object> dataMap = objectMapper.convertValue(dataNode, Map.class);
	        result.add(dataMap);
	    } else {
	        logger.info("기본 타입 데이터");
	        Map<String, Object> dataMap = new HashMap<>();
	        dataMap.put("value", objectMapper.convertValue(dataNode, Object.class));
	        result.add(dataMap);
	    }
	    
	    return result;
	}

    
    /**
     * XML 응답 파싱
     */
    private List<Map<String, Object>> parseXmlResponse(String xmlData) throws Exception {
        // XML 파싱 로직 구현
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode rootNode = xmlMapper.readTree(xmlData);
        
        ObjectMapper objectMapper = new ObjectMapper();
        return parseJsonResponse(objectMapper.writeValueAsString(rootNode), objectMapper);
    }
    
    /**
     * 데이터 전처리 - 디버깅 로그 추가
     */
    private Map<String, Object> processDataForTruncateInsert(Map<String, Object> originalData, 
                                                   Map<String, String> columnMapping, 
                                                   Set<String> validColumns) {
        Map<String, Object> processedData = new HashMap<>();
        
        logger.info("=== 데이터 전처리 디버깅 ===");
        logger.info("원본 데이터: {}", originalData);
        logger.info("컬럼 매핑: {}", columnMapping);
        logger.info("유효 컬럼: {}", validColumns);
        
        for (Map.Entry<String, Object> entry : originalData.entrySet()) {
            String apiField = entry.getKey();
            Object value = entry.getValue();
            
            logger.debug("처리 중인 필드: {} = {}", apiField, value);
            
            // 매핑된 컬럼명 또는 원본 필드명 사용
            String columnName = columnMapping.getOrDefault(apiField, apiField);
            logger.debug("매핑된 컬럼명: {} -> {}", apiField, columnName);
            
            // 유효한 컬럼인지 확인
            if (!validColumns.contains(columnName)) {
                logger.debug("유효하지 않은 컬럼 제외: {} -> {}", apiField, columnName);
                continue;
            }
            
            // 값 처리
            Object processedValue = processValue(value);
            if (processedValue != null) {
                processedData.put(columnName, processedValue);
                logger.debug("매핑 완료: {} -> {} = {}", apiField, columnName, processedValue);
            } else {
                logger.debug("null 값으로 처리: {} -> {}", apiField, columnName);
            }
        }
        
        logger.info("처리된 데이터: {}", processedData);
        logger.info("처리된 데이터 건수: {}", processedData.size());
        return processedData;
    }
    
    /**
     * 값 처리 (null, 빈값 정리)
     */
    private Object processValue(Object value) {
    	 if (value == null) return null;

    	    if (value instanceof String) {
    	        String strVal = ((String) value).trim();

    	        if (strVal.isEmpty() || strVal.equalsIgnoreCase("null")) {
    	            return null;
    	        }

    	        // 숫자인 경우 자동 변환
    	        if (strVal.matches("^-?\\d+$")) {
    	            // 정수로 반환
    	            try {
    	                return Integer.parseInt(strVal);
    	            } catch (NumberFormatException e) {
    	                return new java.math.BigInteger(strVal);
    	            }
    	        } else if (strVal.matches("^-?\\d+\\.\\d+$")) {
    	            // 소수로 반환
    	            return new java.math.BigDecimal(strVal);
    	        }
    	    }

        
        return value;
    }
    
    /**
     * Map 기반 PublicDataVO 생성
     */
    private PublicDataVO createPublicDataVOFromMap(String tableName, Map<String, Object> dataMap) {
        PublicDataVO vo = new PublicDataVO();
        vo.setTableName(tableName);
        vo.setDataMap(dataMap);
        
        // List 방식도 함께 설정 (호환성을 위해)
        List<String> columns = new ArrayList<>(dataMap.keySet());
        List<Object> values = new ArrayList<>();
        
        for (String column : columns) {
            values.add(dataMap.get(column));
        }
        
        vo.setColumnList(columns);
        vo.setValueList(values);
        
        return vo;
    }
    
    /**
     * 컬럼 매핑 설정 파싱 - 배열/객체 형태 모두 처리
     */
    private Map<String, String> parseColumnMapping(String mappingConfig) {
        try {
            if (mappingConfig == null || mappingConfig.trim().isEmpty()) {
                logger.info("매핑 설정이 null 또는 빈 문자열입니다.");
                return new HashMap<>();
            }
            
            String trimmedConfig = mappingConfig.trim();
            
            // 1. JSON 배열 형태 처리: [{"apiField":"column1", "dbColumn":"col1"}, ...]
            if (trimmedConfig.startsWith("[")) {
                return parseJsonArrayMapping(trimmedConfig);
            } 
            // 2. JSON 객체 형태 처리: {"apiField1":"dbField1", "apiField2":"dbField2"}
            else if (trimmedConfig.startsWith("{")) {
                return parseJsonObjectMapping(trimmedConfig);
            } 
            // 3. 텍스트 형태 처리: "apifiled:xxx dbcolumn:yyy" 또는 "apifield:xxx,dbcolumn:yyy"
            else {
                return parseTextMapping(trimmedConfig);
            }
            
        } catch (Exception e) {
            logger.error("매핑 설정 파싱 실패", e);
            logger.error("설정값: {}", mappingConfig);
            return new HashMap<>();
        }
    }
    
    /**
     * JSON 배열 형태 매핑 파싱
     */
    private Map<String, String> parseJsonArrayMapping(String mappingConfig) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, String>> mappingList = objectMapper.readValue(
            mappingConfig, 
            new TypeReference<List<Map<String, String>>>() {}
        );
        
        logger.info("파싱된 매핑 리스트: {}", mappingList);
        
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> mapping : mappingList) {
            String apiField = mapping.get("apiField");
            
            // dbColumn 또는 dbField 둘 다 지원
            String dbField = mapping.get("dbColumn");
            if (dbField == null) {
                dbField = mapping.get("dbField");
            }
            
            logger.debug("매핑 처리: {} -> {}", apiField, dbField);
            
            if (apiField != null && dbField != null && 
                !apiField.trim().isEmpty() && !dbField.trim().isEmpty()) {
                result.put(apiField.trim(), dbField.trim());
            } else {
                logger.warn("유효하지 않은 매핑: apiField={}, dbField={}", apiField, dbField);
            }
        }
        
        logger.info("배열 형태 매핑 설정 파싱 완료: {} 개", result.size());
        logger.info("최종 매핑 결과: {}", result);
        return result;
    }

    /**
     * JSON 객체 형태 매핑 파싱
     */
    private Map<String, String> parseJsonObjectMapping(String mappingConfig) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
        Map<String, String> result = objectMapper.readValue(mappingConfig, typeRef);
        
        logger.info("객체 형태 매핑 설정 파싱 완료: {} 개", result.size());
        logger.info("최종 매핑 결과: {}", result);
        return result;
    }
    
    /**
     * 텍스트 형태 매핑 파싱
     */
    private Map<String, String> parseTextMapping(String mappingConfig) {
        logger.info("텍스트 형태 매핑 파싱 시작: {}", mappingConfig);
        Map<String, String> result = new HashMap<>();
        
        try {
            // 여러 구분자로 분할 시도 (공백, 쉼표, 세미콜론)
            String[] pairs = mappingConfig.split("[\\s,;]+");
            
            for (String pair : pairs) {
                if (pair == null || pair.trim().isEmpty()) {
                    continue;
                }
                
                String trimmedPair = pair.trim();
                logger.debug("처리 중인 페어: '{}'", trimmedPair);
                
                // apifield:dbfield 또는 apifiled:dbcolumn 형태 파싱
                if (trimmedPair.contains(":")) {
                    String[] parts = trimmedPair.split(":", 2); // 최대 2개로 분할
                    
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        
                        // apifield, apifiled, apiField 등 다양한 표기 지원
                        if (isApiFieldKey(key) && !value.isEmpty()) {
                            // value에서 실제 DB 컬럼명 추출
                            String dbColumn = extractDbColumn(value);
                            if (!dbColumn.isEmpty()) {
                                result.put(key, dbColumn);
                                logger.debug("매핑 추가: {} -> {}", key, dbColumn);
                            }
                        } else if (isDbFieldKey(key) && !value.isEmpty()) {
                            // dbcolumn:value 형태인 경우 (순서가 바뀐 경우)
                            result.put(value, key);
                            logger.debug("역순 매핑 추가: {} -> {}", value, key);
                        }
                    }
                }
                // "apifield dbcolumn" 형태 (콜론 없이 공백으로만 구분)
                else if (trimmedPair.contains(" ")) {
                    String[] parts = trimmedPair.split("\\s+", 2);
                    if (parts.length == 2) {
                        String first = parts[0].trim();
                        String second = parts[1].trim();
                        
                        if (isApiFieldKey(first) || (!isDbFieldKey(first) && !isDbFieldKey(second))) {
                            result.put(first, second);
                            logger.debug("공백 구분 매핑 추가: {} -> {}", first, second);
                        }
                    }
                }
            }
            
            // 특별한 경우: "apifiled:xxx dbcolumn:yyy" 형태를 하나의 매핑으로 처리
            if (result.isEmpty()) {
                result = parseComplexTextMapping(mappingConfig);
            }
            
        } catch (Exception e) {
            logger.error("텍스트 매핑 파싱 중 오류", e);
        }
        
        logger.info("텍스트 형태 매핑 설정 파싱 완료: {} 개", result.size());
        logger.info("최종 매핑 결과: {}", result);
        return result;
    }
    
    /**
     * 복합 텍스트 매핑 파싱 - "apifiled:xxx dbcolumn:yyy" 전체를 하나의 매핑으로 처리
     */
    private Map<String, String> parseComplexTextMapping(String mappingConfig) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // 정규식을 사용하여 apifield:value와 dbcolumn:value 패턴 추출
            String apiFieldPattern = "(?:api(?:field|filed))\\s*:\\s*([^\\s]+)";
            String dbColumnPattern = "(?:db(?:column|field))\\s*:\\s*([^\\s]+)";
            
            java.util.regex.Pattern apiPattern = java.util.regex.Pattern.compile(apiFieldPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Pattern dbPattern = java.util.regex.Pattern.compile(dbColumnPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            
            java.util.regex.Matcher apiMatcher = apiPattern.matcher(mappingConfig);
            java.util.regex.Matcher dbMatcher = dbPattern.matcher(mappingConfig);
            
            String apiField = null;
            String dbColumn = null;
            
            if (apiMatcher.find()) {
                apiField = apiMatcher.group(1);
                logger.debug("정규식으로 추출한 API 필드: {}", apiField);
            }
            
            if (dbMatcher.find()) {
                dbColumn = dbMatcher.group(1);
                logger.debug("정규식으로 추출한 DB 컬럼: {}", dbColumn);
            }
            
            if (apiField != null && dbColumn != null) {
                result.put(apiField, dbColumn);
                logger.info("복합 텍스트 매핑 성공: {} -> {}", apiField, dbColumn);
            }
            
        } catch (Exception e) {
            logger.error("복합 텍스트 매핑 파싱 오류", e);
        }
        
        return result;
    }
    
    /**
     * API 필드 키인지 확인
     */
    private boolean isApiFieldKey(String key) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase();
        return lowerKey.equals("apifield") || lowerKey.equals("apifiled") || 
               lowerKey.equals("api_field") || lowerKey.equals("apiField");
    }

    /**
     * DB 필드 키인지 확인
     */
    private boolean isDbFieldKey(String key) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase();
        return lowerKey.equals("dbcolumn") || lowerKey.equals("dbfield") || 
               lowerKey.equals("db_column") || lowerKey.equals("db_field") ||
               lowerKey.equals("dbColumn") || lowerKey.equals("dbField");
    }

    /**
     * DB 컬럼명 추출 (dbcolumn: 등의 접두사 제거)
     */
    private String extractDbColumn(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = value.trim();
        
        // dbcolumn:, dbfield: 등의 접두사가 있는 경우 제거
        if (trimmed.toLowerCase().startsWith("dbcolumn:")) {
            return trimmed.substring(9).trim();
        } else if (trimmed.toLowerCase().startsWith("dbfield:")) {
            return trimmed.substring(8).trim();
        } else if (trimmed.toLowerCase().startsWith("db_column:")) {
            return trimmed.substring(10).trim();
        } else if (trimmed.toLowerCase().startsWith("db_field:")) {
            return trimmed.substring(9).trim();
        }
        
        return trimmed;
    }

    

    @Override
    public ApiConfigVO selectApiForm(String configId) {
        return apiConfigDAO.selectApiForm(configId);
    }

    @Override
    public int apiConfigUpdate(ApiConfigVO vo) {
        return apiConfigDAO.apiConfigUpdate(vo);
    }
}