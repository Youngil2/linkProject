$(document).ready(function() {
    initChat();
});

let isOpen = false;

function initChat() {
    // 이벤트 바인딩
    bindChatEvents();
    
    // 초기 설정
    setupChatInput();
}

function bindChatEvents() {
    // 채팅 토글 버튼
    $(document).on('click', '#chatToggleBtn', function() {
        toggleChatWindow();
    });
    
    // 채팅창 닫기 버튼
    $(document).on('click', '#chatCloseBtn', function() {
        closeChatWindow();
    });
    
    // 메시지 전송 버튼
    $(document).on('click', '#chatSendButton', function() {
        sendMessage();
    });
    
    // Enter 키로 메시지 전송 (Shift+Enter는 줄바꿈)
    $(document).on('keypress', '#chatMessageInput', function(e) {
        if (e.which === 13 && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    
    // 텍스트에리어 자동 높이 조절
    $(document).on('input', '#chatMessageInput', function() {
        autoResizeTextarea(this);
    });
    
    // 채팅창 외부 클릭시 닫기 (선택사항)
    $(document).on('click', function(e) {
        if (isOpen && !$(e.target).closest('#chatWindow, #chatToggleBtn').length) {
            // 외부 클릭시 닫기를 원하지 않으면 이 부분 주석처리
            // closeChatWindow();
        }
    });
}

function setupChatInput() {
    // 텍스트에리어 초기 설정
    $('#chatMessageInput').on('focus', function() {
        $(this).attr('placeholder', '');
    }).on('blur', function() {
        if (!$(this).val()) {
            $(this).attr('placeholder', '메시지를 입력하세요...');
        }
    });
}

function toggleChatWindow() {
    if (isOpen) {
        closeChatWindow();
    } else {
        openChatWindow();
    }
}

function openChatWindow() {
    isOpen = true;
    $('#chatWindow').fadeIn(300);
    $('#chatToggleBtn').addClass('active');
    hideChatBadge();
    
    // 입력창에 포커스
    setTimeout(function() {
        $('#chatMessageInput').focus();
    }, 350);
    
    // 스크롤을 맨 아래로
    scrollToBottom();
}

function closeChatWindow() {
    isOpen = false;
    $('#chatWindow').fadeOut(300);
    $('#chatToggleBtn').removeClass('active');
}

function showChatBadge() {
    $('#chatBadge').fadeIn(200);
}

function hideChatBadge() {
    $('#chatBadge').fadeOut(200);
}

function addMessage(content, isUser = false) {
    const messageClass = isUser ? 'user-message' : 'bot-message';
    const messageHtml = `
        <div class="message ${messageClass}">
            <div class="message-bubble">
                ${escapeHtml(content)}
            </div>
        </div>
    `;
    
    $('#chatMessages').append(messageHtml);
    scrollToBottom();
    
    // 채팅창이 닫혀있고 봇 메시지인 경우 알림 표시
    if (!isOpen && !isUser) {
        showChatBadge();
        
        // 선택사항: 브라우저 알림 (권한 필요)
        showBrowserNotification('새 메시지', content);
    }
}

function showTypingIndicator() {
    $('#typingIndicator').slideDown(200);
    scrollToBottom();
}

function hideTypingIndicator() {
    $('#typingIndicator').slideUp(200);
}

function scrollToBottom() {
    const chatMessages = $('#chatMessages')[0];
    if (chatMessages) {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}

function autoResizeTextarea(element) {
    $(element).css('height', 'auto');
    const newHeight = Math.min(element.scrollHeight, 100);
    $(element).css('height', newHeight + 'px');
}

function escapeHtml(text) {
    return $('<div>').text(text).html();
}

async function sendMessage() {
    const $input = $('#chatMessageInput');
    const message = $input.val().trim();
    
    if (!message) {
        $input.focus();
        return;
    }
    
    // 사용자 메시지 추가
    addMessage(message, true);
    
    // 입력창 초기화
    $input.val('').css('height', 'auto');
    
    // 전송 버튼 비활성화
    $('#chatSendButton').prop('disabled', true);
    
    // 타이핑 인디케이터 표시
    showTypingIndicator();
    
    try {
        const response = await $.ajax({
            url: '/api/chat',
            method: 'POST',
            contentType: 'application/json',
            headers: {
                'X-CSRF-TOKEN': $('meta[name="_csrf"]').attr('content')
            },
            data: JSON.stringify({ message: message }),
            timeout: 30000 // 30초 타임아웃
        });
        
        hideTypingIndicator();
        
        if (response && response.response) {
            addMessage(response.response);
        } else {
            addMessage('응답을 받지 못했습니다.');
        }
        
    } catch (error) {
        hideTypingIndicator();
        console.error('채팅 오류:', error);
        
        let errorMessage = '죄송합니다. 오류가 발생했습니다.';
        
        if (error.status === 0) {
            errorMessage = '네트워크 연결을 확인해주세요.';
        } else if (error.status === 401) {
            errorMessage = '인증이 필요합니다. 다시 로그인해주세요.';
        } else if (error.status === 403) {
            errorMessage = '접근 권한이 없습니다.';
        } else if (error.status === 500) {
            errorMessage = '서버 오류가 발생했습니다.';
        } else if (error.statusText === 'timeout') {
            errorMessage = '응답 시간이 초과되었습니다.';
        }
        
        addMessage(errorMessage);
        
    } finally {
        // 전송 버튼 활성화
        $('#chatSendButton').prop('disabled', false);
        
        // 입력창에 포커스
        $input.focus();
    }
}