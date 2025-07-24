package com.practice.kyi.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.practice.kyi.board.service.BoardService;
import com.practice.kyi.common.dao.vo.FaqVO;

@Service
public class ChatResponseService {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatResponseService.class);
	
	/* @Value("${company.name}") */
    private String companyName;
    
    @Value("${company.phone}")
    private String companyPhone;
    
	/* @Value("${company.description}") */
    private String companyDescription;
    
    @Autowired
    private BoardService boardService; // 기존 FAQ 서비스 주입
    
    private Map<Pattern, String> responsePatterns;
    
    public ChatResponseService() {
        initializeResponsePatterns();
    }
    
    private void initializeResponsePatterns() {
        responsePatterns = new HashMap<>();
        
        // 기본 인사말
        responsePatterns.put(
            Pattern.compile(".*안녕.*|.*처음.*|.*반가.*|.*hello.*|.*hi.*", Pattern.CASE_INSENSITIVE),
            "안녕하세요! " + companyName + " 챗봇입니다. 😊\n\n" +
            "무엇을 도와드릴까요?\n\n" +
            "• 사이트 소개\n" +
            "• 연락처 문의\n" +
            "• 업무시간 안내\n" +
            "• 기타 궁금한 사항"
        );
        
        // 감사 인사
        responsePatterns.put(
            Pattern.compile(".*고마.*|.*감사.*|.*thanks.*|.*thank you.*", Pattern.CASE_INSENSITIVE),
            "천만에요! 😊\n\n더 궁금한 것이 있으시면 언제든 말씀해 주세요.\n" +
            "저희 " + companyName + "를 이용해 주셔서 감사합니다!"
        );
    }
    
    /**
     * 사전 정의된 응답 확인
     */
    public String getPredefinedResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        String trimmedMessage = message.trim();
        
        // 패턴 매칭으로 응답 찾기
        for (Map.Entry<Pattern, String> entry : responsePatterns.entrySet()) {
            if (entry.getKey().matcher(trimmedMessage).matches()) {
            	logger.info("사전 정의된 응답 사용: {}", trimmedMessage);
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 기존 FAQ 데이터베이스에서 응답 찾기
     */
    public String getFaqResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        try {
            FaqVO searchVO = new FaqVO();
            searchVO.setUseYn("Y");
            
            List<FaqVO> faqList = boardService.findFaq(searchVO);
            
            if (faqList == null || faqList.isEmpty()) {
                logger.info("FAQ 리스트가 비어있음");
                return null;
            }
            
            logger.info("총 {} 개의 FAQ에서 검색 중: '{}'", faqList.size(), message);
            
            FaqVO matchedFaq = findBestMatchFaq(message, faqList);
            
            if (matchedFaq != null) {
                logger.info("FAQ 매칭 성공 - 질문: '{}', 키워드: '{}'", 
                           matchedFaq.getQuestion(), matchedFaq.getKeywords());
                return formatFaqResponse(matchedFaq);
            } else {
                logger.info("매칭되는 FAQ를 찾지 못함: '{}'", message);
            }
            
        } catch (Exception e) {
            logger.error("FAQ 조회 중 오류 발생", e);
        }
        
        return null;
    }
    
    /**
     * 메시지와 가장 유사한 FAQ 찾기
     */
    private FaqVO findBestMatchFaq(String message, List<FaqVO> faqList) {
        // 한글 처리를 위해 toLowerCase() 제거하고 직접 처리
        String normalizedMessage = normalizeText(message);
        
        int bestScore = 0;
        FaqVO bestMatch = null;
        
        for (FaqVO faq : faqList) {
            int score = calculateMatchScore(normalizedMessage, faq);
            if (score > bestScore && score >= 1) { // 최소 매칭 점수 설정
                bestScore = score;
                bestMatch = faq;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * 텍스트 정규화 (한글 처리)
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.trim()
                   .replaceAll("\\s+", " ") // 연속된 공백을 하나로
                   .replaceAll("[\\?!,.]", ""); // 특수문자 제거
    }

    /**
     * FAQ와 메시지 간의 매칭 점수 계산
     */
    private int calculateMatchScore(String message, FaqVO faq) {
        int score = 0;
        
        logger.info("=== FAQ 매칭 점수 계산 시작 ===");
        logger.info("입력 메시지: '{}'", message);
        logger.info("FAQ 번호: {}", faq.getFaqSeq());
        logger.info("FAQ 질문: '{}'", faq.getQuestion());
        logger.info("FAQ 키워드: '{}'", faq.getKeywords());
        
        // 1. 질문 제목에서 키워드 매칭 (가중치: 3)
        if (faq.getQuestion() != null) {
            String question = normalizeText(faq.getQuestion());
            int questionMatches = countKeywordMatches(message, question);
            int questionScore = questionMatches * 3;
            score += questionScore;
            logger.info("질문 매칭: {} 개 단어 × 3 = {} 점", questionMatches, questionScore);
        }
        
        // 2. 키워드 필드에서 매칭 (가중치: 2)
        if (faq.getKeywords() != null && !faq.getKeywords().trim().isEmpty()) {
            String[] keywords = faq.getKeywords().split("\\s*,\\s*");
            int keywordScore = 0;
            logger.info("키워드 목록: {}", java.util.Arrays.toString(keywords));
            
            for (String keyword : keywords) {
                String trimmedKeyword = keyword.trim();
                if (message.contains(trimmedKeyword)) {
                    keywordScore += 2;
                    logger.info("키워드 '{}' 매칭! +2점", trimmedKeyword);
                } else {
                    logger.info("키워드 '{}' 매칭 실패", trimmedKeyword);
                }
            }
            score += keywordScore;
            logger.info("키워드 총 점수: {}", keywordScore);
        } else {
            logger.info("키워드 필드가 비어있음 또는 null");
        }
        
        // 3. 답변 내용에서 키워드 매칭 (가중치: 1)
        if (faq.getAnswer() != null) {
            String answer = normalizeText(faq.getAnswer().replaceAll("<[^>]*>", ""));
            int answerMatches = countKeywordMatches(message, answer);
            score += answerMatches;
            logger.info("답변 매칭: {} 개 단어 × 1 = {} 점", answerMatches, answerMatches);
            logger.info("답변 내용 (처음 100자): '{}'", 
                       answer.length() > 100 ? answer.substring(0, 100) + "..." : answer);
        }
        
        logger.info("최종 점수: {} (최소 필요: 2)", score);
        logger.info("매칭 결과: {}", score >= 2 ? "성공" : "실패");
        logger.info("=== FAQ 매칭 점수 계산 종료 ===\n");
        
        return score;
    }

    /**
     * 키워드 매칭 개수 계산
     */
    private int countKeywordMatches(String message, String target) {
        int matches = 0;
        String[] messageWords = message.split("\\s+");
        
        for (String word : messageWords) {
            if (word.length() >= 2 && target.contains(word)) {
                matches++;
            }
        }
        
        return matches;
    }
       
    /**
     * FAQ 응답 포맷팅
     */
    private String formatFaqResponse(FaqVO faq) {
        StringBuilder response = new StringBuilder();
        
        if (faq.getAnswer() != null) {
            // HTML 태그 제거 (필요한 경우)
            String content = faq.getAnswer().replaceAll("<[^>]*>", "");
            response.append(content);
        }
        
        response.append("\n\n더 궁금한 사항이 있으시면 언제든 문의해 주세요! 😊");
        
        return response.toString();
    }
    
    /**
     * 키워드 기반 기본 응답 (FAQ에서 찾지 못한 경우)
     */
    public String getKeywordBasedResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        String lowerMessage = message.toLowerCase();
        companyName = "YI회사";
        companyDescription="이 웹사이트는 데이터 연계를 통하여 연계 데이터를 적극 이용하고자 하는 고객분들에게 서비스를 제공하는 웹사이트입니다.";
        // 사이트 소개 키워드
        if (containsAny(lowerMessage, "사이트", "회사", "서비스", "소개")) {
            return "안녕하세요! " + companyName + "입니다.\n\n" + companyDescription + 
                   "\n\n더 자세한 정보는 FAQ를 확인해 주시거나, 관리자에게 문의해 주세요!";
        }
        
        // 연락처 키워드
        if (containsAny(lowerMessage, "전화", "연락처", "관리자", "문의")) {
            return "📞 관리자 연락처: " + companyPhone + "\n"+ 
                   "\n⏰ 업무시간: 평일 09:00~18:00" +
                   "\n📧 이메일 문의도 가능합니다!";
        }
        
        // 시간 키워드
        if (containsAny(lowerMessage, "시간", "운영", "영업", "몇시")) {
            return "⏰ 업무시간 안내:\n" +
                   "• 평일: 09:00~18:00\n" +
                   "• 토요일: 09:00~13:00\n" +
                   "• 일요일/공휴일: 휴무\n" +
                   "• 점심시간: 12:00~13:00";
        }
        
        return null;
    }
    
    /**
     * 문자열이 주어진 키워드들 중 하나라도 포함하는지 확인
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    public String getDefaultResponse() {
        return "죄송합니다. 정확히 이해하지 못했습니다. 😅\n\n" +
               "다음과 같이 질문해 주시면 도움을 드릴 수 있습니다:\n\n" +
               "• '회사 소개해줘'\n" +
               "• '연락처 알려줘'\n" +
               "• '업무시간이 언제야?'\n" +
               "• '위치가 어디야?'\n\n" +
               "또는 관리자에게 직접 문의해 주세요: " + companyPhone;
    }

}
