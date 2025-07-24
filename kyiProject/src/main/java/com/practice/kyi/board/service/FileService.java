package com.practice.kyi.board.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.practice.kyi.board.controller.BoardController;
import com.practice.kyi.board.dao.BoardFileDAO;
import com.practice.kyi.board.dao.vo.BoardFileVO;

@Service
public class FileService {
	
	private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private BoardFileDAO boardFileDAO;

    @Value("${upload.file.path}")
    private String uploadPath;

    /**
     * 멀티 파일 업로드 처리 (JavaScript 호환)
     */
    public List<BoardFileVO> uploadMultipleFiles(List<MultipartFile> files, String boardSeq, String userId) {
        List<BoardFileVO> uploadedFiles = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        // 업로드 경로 확인
        System.out.println("Upload path: " + uploadPath);
        
        for (MultipartFile file : files) {
            try {                
                // 파일 검증
                validateFile(file);

                BoardFileVO boardFile = uploadSingleFile(file, boardSeq, userId);
                uploadedFiles.add(boardFile);

            } catch (IllegalArgumentException e) {
                logger.error("Validation error for file " + file.getOriginalFilename() + ": " + e.getMessage());
                errorMessages.add(file.getOriginalFilename() + ": " + e.getMessage());
            } catch (IOException e) {
            	logger.error("IO error for file " + file.getOriginalFilename() + ": " + e.getMessage());
                e.printStackTrace();
                errorMessages.add("파일 저장 실패: " + file.getOriginalFilename() + " - " + e.getMessage());
            } catch (Exception e) {
            	logger.error("Unexpected error for file " + file.getOriginalFilename() + ": " + e.getMessage());
                e.printStackTrace();
                errorMessages.add("파일 업로드 실패: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        // 에러가 있으면 예외 던지기
        if (!errorMessages.isEmpty()) {
            throw new RuntimeException(String.join(", ", errorMessages));
        }

        return uploadedFiles;
    }

    /**
     * 단일 파일 업로드 처리
     */
    private BoardFileVO uploadSingleFile(MultipartFile file, String boardSeq, String userId) throws IOException {
        // 1. BoardFileVO 생성 및 파일 정보 설정
        BoardFileVO boardFileVO = generateBoardFileVO(file, boardSeq, userId);
        
        // 2. 물리적 파일 저장
        String savedPath = saveFile(file, boardFileVO.getFileNm());
        boardFileVO.setFilePath(savedPath);
        
        // 3. DB 저장
        boardFileDAO.insertBoardFile(boardFileVO);
        
        return boardFileVO;
    }

    /**
     * 파일 다운로드 처리
     */
    public ResponseEntity<Resource> downloadFile(String fileSeq) throws IOException {
        BoardFileVO boardFile = boardFileDAO.selectBoardFile(fileSeq);
        
        if (boardFile == null) {
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }

        Resource resource = loadFileAsResource(boardFile.getFilePath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + boardFile.getOriginalFileNm() + "\"")
                .body(resource);
    }

    /**
     * 파일 삭제 처리
     */
    public void deleteFile(String fileSeq) {
        BoardFileVO boardFile = boardFileDAO.selectBoardFile(fileSeq);
        
        if (boardFile == null) {
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }

        // 1. 물리적 파일 삭제
        deletePhysicalFile(boardFile.getFilePath());

        // 2. DB에서 삭제
        boardFileDAO.deleteBoardFile(fileSeq);
    }
    
    /**
     * 게시글의 파일 목록 조회
     */
    public List<BoardFileVO> getBoardFiles(String boardSeq) {
        return boardFileDAO.selectBoardFileList(boardSeq);
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        // JavaScript와 동일한 검증 로직
        final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx",
            "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
        );

        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }
    }

    /**
     * BoardFileVO 생성 및 파일 정보 설정
     */
    private BoardFileVO generateBoardFileVO(MultipartFile file, String boardSeq, String userId) {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String systemFileName = generateSystemFileName(fileExtension);

        BoardFileVO boardFileVO = new BoardFileVO();
        boardFileVO.setBoardSeq(boardSeq);
        boardFileVO.setOriginalFileNm(originalFileName);
        boardFileVO.setFileNm(systemFileName);
        boardFileVO.setFileExt(fileExtension);
        boardFileVO.setFileSize(file.getSize());
        boardFileVO.setRegistUser(userId);
        boardFileVO.setRegistDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return boardFileVO;
    }

    /**
     * 물리적 파일 저장
     */
    private String saveFile(MultipartFile file, String fileName) throws IOException {
        // 업로드 디렉토리 생성
        File uploadDir = new File(uploadPath);
        
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            logger.info("Directory created: " + created);
            if (!created) {
                throw new IOException("업로드 디렉토리를 생성할 수 없습니다: " + uploadPath);
            }
        }

        // 디렉토리 권한 확인
        if (!uploadDir.canWrite()) {
            throw new IOException("업로드 디렉토리에 쓰기 권한이 없습니다: " + uploadPath);
        }

        // 파일 저장
        String filePath = uploadPath + File.separator + fileName;
        
        try {
            file.transferTo(new File(filePath));
            logger.info("File saved successfully: " + filePath);
        } catch (IOException e) {
        	logger.info("Failed to save file: " + filePath);
            throw e;
        }

        return filePath;
    }
    /**
     * 파일 리소스 로드
     */
    private Resource loadFileAsResource(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다: " + filePath);
        }

        return resource;
    }

    /**
     * 물리적 파일 삭제
     */
    private void deletePhysicalFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + filePath);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 시스템 파일명 생성 (타임스탬프 기반)
     */
    private String generateSystemFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        return "FILE_" + timestamp + "." + extension;
    }
    
    /**
     * 단일 파일 정보 조회
     */
    public BoardFileVO getBoardFile(String fileSeq) {
        return boardFileDAO.selectBoardFile(fileSeq);
    }

    /**
     * 파일 소유자 확인
     */
    public boolean isFileOwner(String fileSeq, String userId) {
        BoardFileVO boardFile = boardFileDAO.selectBoardFile(fileSeq);
        return boardFile != null && boardFile.getRegistUser().equals(userId);
    }
}