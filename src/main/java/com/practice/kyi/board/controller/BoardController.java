package com.practice.kyi.board.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
import org.springframework.web.multipart.MultipartFile;

import com.practice.kyi.board.dao.vo.BoardFileVO;
import com.practice.kyi.board.dao.vo.BoardVO;
import com.practice.kyi.board.service.BoardService;
import com.practice.kyi.board.service.FileService;
import com.practice.kyi.config.PageUtil;
import com.practice.kyi.config.Pagination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class BoardController {
	
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	@Autowired 
	BoardService boardService;
	
	@Autowired
	FileService fileService;
	 
	
    @GetMapping("/{topMenu}/{subMenu}")
    public String userPage(@PathVariable("topMenu") String topMenu,
                           @PathVariable("subMenu") String subMenu,
                           Model model) {
    	
    	String subMenuNm = boardService.subMenuNm(subMenu.toUpperCase());
    	model.addAttribute("subMenuNm", subMenuNm); 
    	model.addAttribute("topMenu",topMenu);
    	model.addAttribute("subMenu",subMenu);
    	
    	
    	if(topMenu.contains("data")) {
    		return "board/dataList";
    	}else {
    		return "board/boardList";
    	}
    }
    @PostMapping("/list_up")
    @ResponseBody
    public Map<String, Object> listUp(@RequestBody Map<String, Object> request){
    	Map<String, Object> data = new HashMap<>();
    	
	    // 요청값 추출
	    int pageUnit = request.get("pageUnit") != null ? Integer.parseInt(request.get("pageUnit").toString()) : 10;
	    int pageIndex = request.get("pageIndexTop") != null ? Integer.parseInt(request.get("pageIndexTop").toString()) : 1;
	    String searchKeyword = request.get("searchKeyword") != null ? request.get("searchKeyword").toString() : "";
	    String searchType = request.get("searchType") != null ? request.get("searchType").toString() : "";
	    String subMenu = request.get("subMenu") != null ? request.get("subMenu").toString() : "";
	    
	    BoardVO vo = new BoardVO();
	    vo.setPageUnit(pageUnit);
	    vo.setPageIndex(pageIndex);
	    vo.setSearchKeyword(searchKeyword);
	    vo.setSearchType(searchType);
	    vo.setSubMenu(subMenu);
	    
	    // 페이징 처리
	    Pagination pagination = PageUtil.setPagination(vo);
	    
	    // 리스트 조회
	    List<BoardVO> list = boardService.selectBoardList(vo);
	    
	    int totalCount = 0;
	    if (!list.isEmpty()) {
	        totalCount = list.get(0).getTotalCount(); // VO에 totalCount 들어오는 경우
	    }
	
	    pagination.setTotalRecordCount(totalCount);
	
	    // 응답 구성
	    data.put("list", list);
	    data.put("totalCount", totalCount);
	
    	
    	return data;
    }
    
    @GetMapping("/{topMenu}/{subMenu}/regist")
    public String boardRegist(@PathVariable String topMenu,
                             @PathVariable String subMenu,
                             Model model) {
        
        String subMenuNm = boardService.subMenuNm(subMenu.toUpperCase());
        model.addAttribute("subMenu", subMenu);
        model.addAttribute("topMenu", topMenu);
        model.addAttribute("subMenuNm", subMenuNm);
        
        return "/board/boardForm";
    }
    
    @PostMapping("/board_form_regist")
    @ResponseBody
    public Map<String, Object> boardFormRegist(BoardVO vo, @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request){
    	Map<String, Object> data = new HashMap<>();
    	
	    
    	try {
    		HttpSession session = request.getSession();
    		String currentUserId = (String) session.getAttribute("memberId");
    		
    		vo.setRegistUser(currentUserId.toUpperCase());
    		
            boardService.insertBoard(vo);
            String boardSeq = vo.getBoardSeq();
            
            List<BoardFileVO> uploadedFiles = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                // 빈 파일 제거
                files = files.stream()
                        .filter(file -> !file.isEmpty())
                        .collect(Collectors.toList());
                
                if (!files.isEmpty()) {
                    uploadedFiles = fileService.uploadMultipleFiles(files, boardSeq, currentUserId);
                }
            }
            
            data.put("success", true);
            data.put("message", "게시글이 등록되었습니다.");
            data.put("boardSeq", boardSeq);
            data.put("uploadedFiles", uploadedFiles);
    		
    	}catch (IllegalArgumentException e) {
            data.put("success", false);
            data.put("message", e.getMessage());
            
        } catch (Exception e) {
            logger.error("게시글 등록 중 오류 발생", e);
            data.put("success", false);
            data.put("message", "게시글 등록 중 오류가 발생했습니다.");
        }
        
        return data;
    }
    @GetMapping("/{topMenu}/{subMenu}/update")
    public String boardUpdate(@RequestParam("board_seq") String boardSeq,
    						 @PathVariable String topMenu,
                             @PathVariable String subMenu,
                             Model model) {
        
        BoardVO board = boardService.boardForm(boardSeq.toUpperCase());
        // 파일 리스트 조회
        List<BoardFileVO> files = fileService.getBoardFiles(boardSeq.toUpperCase());
        
        // 등록일 포맷팅
        if (board.getRegistDate() != null && board.getRegistDate().length() >= 16) {
            String registDate = board.getRegistDate().substring(0, 16);
            board.setRegistDate(registDate);
        }
        
        // 수정일 포맷팅
        if (board.getUpdateDate() != null && !board.getUpdateDate().isEmpty() && board.getUpdateDate().length() >= 16) {
            String updateDate = board.getUpdateDate().substring(0, 16);
            board.setUpdateDate(updateDate);
        }
        
        model.addAttribute("boardSeq", boardSeq);
        model.addAttribute("subMenu", subMenu);
        model.addAttribute("topMenu", topMenu);
        model.addAttribute("files", files);
        model.addAttribute("board", board);
        
        return "board/boardForm";
    }
    
    @PostMapping("/board_form_update")
    @ResponseBody
    public Map<String, Object> boardUpdateForm(BoardVO vo, 
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteFileSeqs", required = false) List<String> deleteFileSeqs,
            HttpServletRequest request){
        
        Map<String, Object> data = new HashMap<>();

        try {
            // 1. 사용자 정보 가져오기
            HttpSession session = request.getSession();
            String currentUserId = (String) session.getAttribute("memberId");
            String currentUserRole = (String) session.getAttribute("memberRole");
            vo.setUpdateUser(currentUserId.toUpperCase());


            // 2. 권한 체크
            BoardVO existingBoard = boardService.boardChk(vo.getBoardSeq());
            boolean isAuthor = existingBoard.getRegistUser().equals(currentUserId);
            boolean isAdmin = "ADMIN".equals(currentUserRole);

            if (!isAuthor && !isAdmin) {
                data.put("success", false);
                data.put("message", "수정 권한이 없습니다.");
                return data;
            }

            // 3. 게시글 수정
            boardService.updateBoard(vo);

            // 4. 파일 삭제 처리
            List<String> deletedFiles = new ArrayList<>();
            if (deleteFileSeqs != null && !deleteFileSeqs.isEmpty()) {
                logger.info("삭제할 파일 개수: " + deleteFileSeqs.size());
                
                for (String fileSeq : deleteFileSeqs) {
                    try {
                        BoardFileVO fileToDelete = fileService.getBoardFile(fileSeq);
                        if (fileToDelete != null) {
                            // 파일 소유자 확인 (작성자 또는 관리자)
                            if (isAuthor || isAdmin) {
                                fileService.deleteFile(fileSeq);
                                deletedFiles.add(fileToDelete.getOriginalFileNm());
                                logger.info("파일 삭제 완료: " + fileToDelete.getOriginalFileNm());
                            } else {
                                logger.warn("파일 삭제 권한 없음: " + fileSeq + ", 사용자: " + currentUserId);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("파일 삭제 실패: " + fileSeq, e);
                    }
                }
            }

            // 5. 새 파일 업로드
            List<BoardFileVO> uploadedFiles = new ArrayList<>();
            if (files != null && !files.isEmpty()) {
                files = files.stream()
                        .filter(file -> !file.isEmpty())
                        .collect(Collectors.toList());

                if (!files.isEmpty()) {
                    uploadedFiles = fileService.uploadMultipleFiles(files, vo.getBoardSeq(), currentUserId);
                    logger.info("파일 업로드 완료: " + uploadedFiles.size() + "개");
                }
            }

            // 6. 성공 응답
            data.put("success", true);
            data.put("message", "게시글이 수정되었습니다.");
            data.put("boardSeq", vo.getBoardSeq()); // boardSeq 추가
            data.put("deletedFiles", deletedFiles);
            data.put("uploadedFiles", uploadedFiles);

        } catch (Exception e) {
            logger.error("게시글 수정 중 오류 발생", e);
            data.put("success", false);
            data.put("message", "게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }

        return data;
    }

    
    /**
     * 파일 다운로드
     */
    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file_seq") String fileSeq) {
        try {
            logger.info("파일 다운로드 요청 - fileSeq: {}", fileSeq);
            return fileService.downloadFile(fileSeq);
        } catch (Exception e) {
            logger.error("파일 다운로드 중 오류 발생 - fileSeq: {}", fileSeq, e);
            return ResponseEntity.notFound().build();
        }
    }    
    /**
     * 게시판 읽기
     */
    @GetMapping("/{topMenu}/{subMenu}/board_view")
    public String boardView(@RequestParam("board_seq") String boardSeq, 
                           Model model,
                           @PathVariable String topMenu,
                           @PathVariable String subMenu) {
        
        try {
            // 게시글 조회
            BoardVO board = boardService.boardForm(boardSeq.toUpperCase());
            
            if (board == null) {
                // 게시글이 없는 경우 처리
                model.addAttribute("errorMessage", "게시글을 찾을 수 없습니다.");
                return "error/404";
            }
            
            // 등록일 포맷팅
            if (board.getRegistDate() != null && board.getRegistDate().length() >= 16) {
                String registDate = board.getRegistDate().substring(0, 16);
                board.setRegistDate(registDate);
            }
            
            // 수정일 포맷팅
            if (board.getUpdateDate() != null && !board.getUpdateDate().isEmpty() && board.getUpdateDate().length() >= 16) {
                String updateDate = board.getUpdateDate().substring(0, 16);
                board.setUpdateDate(updateDate);
            }
            
            // 파일 리스트 조회
            List<BoardFileVO> files = fileService.getBoardFiles(boardSeq.toUpperCase());
            
            model.addAttribute("boardSeq", boardSeq);
            model.addAttribute("subMenu", subMenu);
            model.addAttribute("topMenu", topMenu);
            model.addAttribute("files", files);
            model.addAttribute("board", board);
            
            return "board/boardView";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "게시글 조회 중 오류가 발생했습니다.");
            return "error/500";
        }
    }
}
