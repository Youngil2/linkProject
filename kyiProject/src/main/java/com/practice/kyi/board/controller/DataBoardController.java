package com.practice.kyi.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.kyi.admin_link.dao.vo.ApiConfigVO;
import com.practice.kyi.admin_link.dao.vo.PublicDataVO;
import com.practice.kyi.admin_link.service.ApiConfigService;
import com.practice.kyi.board.service.DataLinkService;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

@Controller
@RequestMapping("/user")
public class DataBoardController {
	
	private static final Logger logger = LoggerFactory.getLogger(DataBoardController.class);
	
	@Autowired
	ApiConfigService apiConfigService;
	
	@Autowired
	DataLinkService dataLinkService;
	
	@PostMapping("/data_list_up")
	@ResponseBody
	public Map<String, Object> dataListUp(@RequestBody Map<String, Object> request) throws Exception{
		Map<String, Object> data = new HashMap<>();
		
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
		
	    ApiConfigVO vo = new ApiConfigVO();
	    vo.setPageUnit(pageUnit);
	    vo.setPageIndex(pageIndex);
	    vo.setSearchKeyword(searchKeyword);
	    vo.setSearchType(searchType);
	    
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(vo);
	    
	    // 리스트 조회
	    List<ApiConfigVO> apiConfigList = apiConfigService.selectApiConfigList(vo);
	    
	    int totalCount = 0;
	    if (!apiConfigList.isEmpty()) {
	        totalCount = apiConfigList.get(0).getTotalCount(); // VO에 totalCount 들어오는 경우
	    }
	
	    pagination.setTotalRecordCount(totalCount);
	    
	    data.put("totalCount", totalCount);
	    data.put("apiConfigList", apiConfigList);
	    
		return data;
	}
	
	@GetMapping("/{topMenu}/{subMenu}/data_board_view")
	public String selectData(@RequestParam("target_table") String targetTable,
							 @PathVariable String topMenu,
				             @PathVariable String subMenu,
				             Model model
				             ) {
		
		String apiNm = dataLinkService.selectApiNm(targetTable);
		model.addAttribute("apiNm", apiNm);
		
		return "board/dataDetail";
	}
	
	@PostMapping("getColumnName")
	@ResponseBody
	public Map<String, Object> getColumnName(@RequestBody Map<String, String> requestData) throws Exception{
		Map<String, Object> data = new HashMap<>();
		
	    String schema = requestData.get("schema");
	    String tableName = requestData.get("tableName");
		
		// 리스트 조회
	    List<Map<String, Object>> list = dataLinkService.getColumnNames(schema, tableName);
	    data.put("list", list);
	    
		return data;
	}
	
	@PostMapping("getDataList")
	@ResponseBody
	public Map<String, Object> getDataList(@RequestBody Map<String, Object> request) throws Exception{
		Map<String, Object> data = new HashMap<>();
		
	    String schema = (String) request.get("schema");
	    String tableName = (String) request.get("tableName");
	    
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    
	    
	    PublicDataVO vo = new PublicDataVO();	    		
	    vo.setPageUnit(pageUnit);
	    vo.setPageIndex(pageIndex);
	    vo.setSearchKeyword(searchKeyword);
	    
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(vo);
	    
	    
		// 리스트 조회
	    List<Map<String, Object>> list = dataLinkService.getDataListWithoutSyncDt(schema,tableName,vo);
	    
	    int totalCount = 0;
	    if (!list.isEmpty()) {
	        Object totalCountObj = list.get(0).get("totalcount");
	        if (totalCountObj != null) {
	            totalCount = Integer.parseInt(totalCountObj.toString());
	        }
	    }
	    pagination.setTotalRecordCount(totalCount);
		
	    data.put("list", list);
	    data.put("totalCount", totalCount);
	    
		return data;
	}
	
	@PostMapping("/downloadExcel")
	@ResponseBody
	public Map<String, Object> downloadExcel(@RequestBody Map<String, Object> request) throws Exception {
	    Map<String, Object> response = new HashMap<>();
	    
	    try {
	        String schema = (String) request.get("schema");
	        String tableName = (String) request.get("tableName");
	        String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	        String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	        String downloadType = (String) request.get("downloadType"); // "current" 또는 "all"
	        
	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> columns = (List<Map<String, Object>>) request.get("columns");
	        
	        // API 명 조회
	        String targetTable = schema + "." + tableName;
	        String apiNm = dataLinkService.selectApiNm(targetTable);
	        
	        // 데이터 조회를 위한 VO 설정
	        PublicDataVO vo = new PublicDataVO();
	        vo.setSearchKeyword(searchKeyword);
	        vo.setSearchType(searchType);
	        
	        List<Map<String, Object>> dataList;
	        
	        if ("current".equals(downloadType)) {
	            // 현재 페이지 데이터만
	            int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	            int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	            
	            vo.setPageUnit(pageUnit);
	            vo.setPageIndex(pageIndex);
	            
	            // 페이징 처리
	            Pagination pagination = PageUtil.setPagination(vo);
	            
	            dataList = dataLinkService.getDataListWithoutSyncDt(schema, tableName, vo);
	        } else {
	            // 전체 데이터 (페이징 없이)
	            dataList = dataLinkService.getAllDataList(schema, tableName, vo);
	        }
	        
	        // 엑셀 파일 생성 (API명 사용)
	        String fileName = createExcelFile(apiNm, columns, dataList, downloadType);
	        
	        response.put("success", true);
	        response.put("fileName", fileName);
	        response.put("originalFileName", apiNm + "_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx");
	        
	    } catch (Exception e) {
	        logger.error("엑셀 다운로드 오류: ", e);
	        response.put("success", false);
	        response.put("message", e.getMessage());
	    }
	    
	    return response;
	}


	@GetMapping("/downloadFile")
	public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String fileName) throws IOException {
	    try {
	        // 임시 디렉토리에서 파일 찾기
	        String tempDir = System.getProperty("java.io.tmpdir");
	        File file = new File(tempDir, fileName);
	        
	        if (!file.exists()) {
	            return ResponseEntity.notFound().build();
	        }
	        
	        FileInputStream fileInputStream = new FileInputStream(file);
	        InputStreamResource resource = new InputStreamResource(fileInputStream) {
	            @Override
	            public InputStream getInputStream() throws IOException {
	                return new FileInputStream(file) {
	                    @Override
	                    public void close() throws IOException {
	                        super.close();
	                        // 파일 스트림이 닫힐 때 임시 파일 삭제
	                        file.delete();
	                    }
	                };
	            }
	        };
	        
	        // 한글 파일명을 UTF-8로 인코딩
	        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
	                .replaceAll("\\+", "%20");
	        
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, 
	                    "attachment; filename*=UTF-8''" + encodedFileName)
	                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
	                .contentLength(file.length())
	                .body(resource);
	                
	    } catch (Exception e) {
	        logger.error("파일 다운로드 오류: ", e);
	        return ResponseEntity.internalServerError().build();
	    }
	}

	private String createExcelFile(String apiNm, List<Map<String, Object>> columns, 
            List<Map<String, Object>> dataList, String downloadType) throws IOException {

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(apiNm); // API명으로 시트명 설정
	    
	    // 헤더 스타일 생성
	    CellStyle headerStyle = workbook.createCellStyle();
	    Font headerFont = workbook.createFont();
	    headerFont.setBold(true);
	    headerFont.setColor(IndexedColors.WHITE.getIndex());
	    headerStyle.setFont(headerFont);
	    headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
	    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    headerStyle.setBorderBottom(BorderStyle.THIN);
	    headerStyle.setBorderTop(BorderStyle.THIN);
	    headerStyle.setBorderRight(BorderStyle.THIN);
	    headerStyle.setBorderLeft(BorderStyle.THIN);
	    
	    // 데이터 스타일 생성
	    CellStyle dataStyle = workbook.createCellStyle();
	    dataStyle.setBorderBottom(BorderStyle.THIN);
	    dataStyle.setBorderTop(BorderStyle.THIN);
	    dataStyle.setBorderRight(BorderStyle.THIN);
	    dataStyle.setBorderLeft(BorderStyle.THIN);
	    
	    // 헤더 행 생성
	    Row headerRow = sheet.createRow(0);
	    for (int i = 0; i < columns.size(); i++) {
	        Map<String, Object> column = columns.get(i);
	        Cell cell = headerRow.createCell(i);
	        cell.setCellValue((String) column.get("comment")); // 컬럼 코멘트를 헤더로 사용
	        cell.setCellStyle(headerStyle);
	        
	        // 컬럼 너비 자동 조정
	        sheet.setColumnWidth(i, 4000); // 기본 너비 설정
	    }
	    
	    // 데이터 행 생성
	    int rowNum = 1;
	    for (Map<String, Object> rowData : dataList) {
	        Row row = sheet.createRow(rowNum++);
	        
	        for (int i = 0; i < columns.size(); i++) {
	            Map<String, Object> column = columns.get(i);
	            String columnName = (String) column.get("name");
	            
	            Cell cell = row.createCell(i);
	            Object value = rowData.get(columnName);
	            
	            if (value != null) {
	                // 데이터 타입에 따른 처리
	                if (value instanceof Number) {
	                    cell.setCellValue(((Number) value).doubleValue());
	                } else if (value instanceof Date) {
	                    cell.setCellValue((Date) value);
	                } else if (value instanceof Boolean) {
	                    cell.setCellValue((Boolean) value);
	                } else {
	                    cell.setCellValue(String.valueOf(value));
	                }
	            }
	            cell.setCellStyle(dataStyle);
	        }
	    }
	    
	    // 컬럼 너비 자동 조정
	    for (int i = 0; i < columns.size(); i++) {
	        sheet.autoSizeColumn(i);
	        // 최대 너비 제한
	        if (sheet.getColumnWidth(i) > 8000) {
	            sheet.setColumnWidth(i, 8000);
	        }
	    }
	    
	    // 임시 파일로 저장
	    String tempDir = System.getProperty("java.io.tmpdir");
	    String fileName = apiNm + "_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + "_" + System.currentTimeMillis() + ".xlsx";
	    File tempFile = new File(tempDir, fileName);
	    
	    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
	        workbook.write(outputStream);
	    } finally {
	        workbook.close();
	    }
	    
	    return fileName;
	}
}
