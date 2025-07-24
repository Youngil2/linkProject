// 파일 업로드 관련 변수
let selectedFiles = [];
const maxFiles = 10;
const maxFileSize = 10 * 1024 * 1024; // 10MB

// 중복 제출 방지 변수
let isSubmitting = false;

$(document).ready(function() { 
    
    $('.board-list-btn').click(function() {
        const subMenu = $(this).data('submenu');
        const topMenu = $(this).data('topmenu');
        fn_menuLink(topMenu, subMenu);
    });
    
    $('#boardCreate').on('click', function() {
        if (isSubmitting) {
            alert('처리 중입니다. 잠시만 기다려주세요.');
            return;
        }
        
        if (validateForm()) {
            submitBoard.call(this, 'create');
        }
    });
    
    $('#boardUpdate').on('click', function() {
        if (isSubmitting) {
            alert('처리 중입니다. 잠시만 기다려주세요.');
            return;
        }
        
        if (validateForm()) {
            submitBoard.call(this, 'update');
        }
    });
    
    // 파일 제거 (이벤트 위임 사용)
    $(document).on('click', '.file-item .btn-outline-danger', function() {
        const index = $(this).data('index');
        selectedFiles.splice(index, 1);
        updateFileList();
        updateFileInput();
    });
    
    // 전체 파일 삭제
	$('#clearAllFiles').on('click', function() {
	    if (confirm('모든 파일을 삭제하시겠습니까?')) {
	        // 1. 새로 선택된 파일들 삭제
	        selectedFiles = [];
	        updateFileList();
	        updateFileInput();
	        
	        // 2. 기존 파일들도 삭제 처리
	        $('.existing-files .file-item').each(function() {
	            const fileSeq = $(this).find('.delete-existing-btn').data('file-seq');
	            if (fileSeq && !deletedFileSeqs.includes(fileSeq)) {
	                deletedFileSeqs.push(fileSeq);
	            }
	        });
	        
	        // 3. 기존 파일 영역을 화면에서 숨기기
	        $('.existing-files').parent().hide();
	        
	        // 4. "선택된 파일이 없습니다" 메시지 표시
	        $('#noFileMessage').show();
	    }
	});
    
    // 파일 선택 이벤트
    $('#fileInput').on('change', function(e) {
        const files = Array.from(e.target.files);
        processFiles(files);
    });
    
    // 파일 선택 버튼 클릭 이벤트
    $('#fileSelectBtn').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $('#fileInput').click();
    });
    
    // 드래그 앤 드롭 이벤트
    const $fileListContainer = $('.border.rounded.p-3.bg-light');
    
    $fileListContainer.on('dragover', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).addClass('drag-over');
    });
    
    $fileListContainer.on('dragleave', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).removeClass('drag-over');
    });
    
    $fileListContainer.on('drop', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(this).removeClass('drag-over');
        
        const files = Array.from(e.originalEvent.dataTransfer.files);
        processFiles(files);
    });

	$(document).on('click', '.delete-existing-btn', function(e) {
	    e.preventDefault();
	    
	    const fileSeq = $(this).data('file-seq');
	    const $fileItem = $(this).closest('.file-item');
	    const fileName = $fileItem.find('.file-name').text();
	    
	    if (confirm(`"${fileName}" 파일을 삭제하시겠습니까?`)) {
	        // 삭제할 파일 목록에 추가
	        deletedFileSeqs.push(fileSeq);
	        
	        // 화면에서 제거 (애니메이션 효과)
	        $fileItem.fadeOut(300, function() {
	            $(this).remove();
	            
	            // 기존 파일이 모두 삭제되었으면 전체 기존 파일 영역 숨기기
	            if ($('.existing-files .file-item').length === 0) {
	                $('.existing-files').parent().hide();
	            }
	        });
	    }
	});
	
});
let deletedFileSeqs = [];
// 게시글 제출
function submitBoard(mode) {
    // 중복 제출 방지
    if (isSubmitting) {
        return;
    }
    
    isSubmitting = true;
    
    // 버튼 비활성화
    const $submitBtn = mode === 'create' ? $('#boardCreate') : $('#boardUpdate');
    $submitBtn.prop('disabled', true).text('처리 중...');
    
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    const subMenu = $(this).data('submenu');
    const topMenu = $(this).data('topmenu');
    
    const formData = new FormData();
    
    // 기본 데이터 추가
    formData.append('boardSeq', $('#boardSeq').val());
    formData.append('subMenu', $('#subMenu').val().toUpperCase());
    formData.append('subMenuNm', $('#subMenuNm').val());
    formData.append('boardTitle', $('#boardTitle').val());
    formData.append('contents', $('#contents').val());
    formData.append('secretYn', $('#secretYn').val());
    
    // 새로운 파일 데이터 추가
    selectedFiles.forEach(file => {
        formData.append('files', file);
    });
    
    // 삭제할 기존 파일 목록 추가 (수정 시에만)
    if (mode === 'update' && deletedFileSeqs.length > 0) {
        deletedFileSeqs.forEach(fileSeq => {
            formData.append('deleteFileSeqs', fileSeq);
        });
    }
    
    const url = mode === 'create' ? '/user/board_form_regist' : '/user/board_form_update';
    
    $.ajax({
        url: url,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        timeout: 30000,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            resetSubmitButton($submitBtn, mode);
            
            if (response.success) {
                let message = mode === 'create' ? '게시글이 등록되었습니다.' : '게시글이 수정되었습니다.';
                
                // 파일 삭제/업로드 결과 메시지 추가
                if (mode === 'update' && response.deletedFiles && response.deletedFiles.length > 0) {
                    message += `\n삭제된 파일: ${response.deletedFiles.join(', ')}`;
                }
                if (response.uploadedFiles && response.uploadedFiles.length > 0) {
                    message += `\n업로드된 파일: ${response.uploadedFiles.length}개`;
                }
                
                alert(message);
                
                var seq = response.boardSeq.toLowerCase();
                window.location.href = '/user/' + topMenu + '/' + subMenu + '/board_view?board_seq=' + seq;
            } else {
                alert('오류가 발생했습니다: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            resetSubmitButton($submitBtn, mode);
            
            if (status === 'timeout') {
                alert('요청 시간이 초과되었습니다. 다시 시도해주세요.');
            } else {
                alert('서버 오류가 발생했습니다.');
            }
            console.error(error);
        },
        complete: function() {
            setTimeout(function() {
                if (isSubmitting) {
                    resetSubmitButton($submitBtn, mode);
                }
            }, 100);
        }
    });
}

// 제출 버튼 리셋 함수
function resetSubmitButton($button, mode) {
    isSubmitting = false;
    $button.prop('disabled', false);
    $button.text(mode === 'create' ? '등록' : '수정');
}

// 파일 처리 함수 (공통) - 기존과 동일
function processFiles(files) {
    let errorMessages = [];
    let validFiles = [];
    let hasMaxFileError = false;
    let hasFileSizeError = false;
    let hasDuplicateError = false;
    
    // 사전 체크: 파일 개수 제한
    if (selectedFiles.length + files.length > maxFiles) {
        const availableSlots = maxFiles - selectedFiles.length;
        if (availableSlots > 0) {
            errorMessages.push(`최대 ${maxFiles}개의 파일만 업로드할 수 있습니다. (${availableSlots}개만 추가 가능)`);
            // 가능한 개수만큼만 처리
            files = files.slice(0, availableSlots);
        } else {
            errorMessages.push(`최대 ${maxFiles}개의 파일만 업로드할 수 있습니다.`);
            if (errorMessages.length > 0) {
                alert(errorMessages.join('\n'));
            }
            return;
        }
    }
    
    // 각 파일 유효성 검사
    files.forEach(file => {
        // 파일 크기 체크
        if (file.size > maxFileSize) {
            if (!hasFileSizeError) {
                errorMessages.push(`파일 크기는 ${formatFileSize(maxFileSize)}를 초과할 수 없습니다.`);
                hasFileSizeError = true;
            }
            return;
        }
        
        // 중복 파일 체크
        if (selectedFiles.some(f => f.name === file.name && f.size === file.size)) {
            if (!hasDuplicateError) {
                errorMessages.push('이미 선택된 파일이 있습니다.');
                hasDuplicateError = true;
            }
            return;
        }
        
        validFiles.push(file);
    });
    
    // 유효한 파일들만 추가
    validFiles.forEach(file => {
        selectedFiles.push(file);
    });
    
    // 오류 메시지가 있으면 한 번만 표시
    if (errorMessages.length > 0) {
        alert(errorMessages.join('\n'));
    }
    
    updateFileList();
    updateFileInput();
}

// 파일 크기 포맷팅
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// 파일 아이콘 반환
function getFileIcon(fileName) {
    const ext = fileName.split('.').pop().toLowerCase();
    const icons = {
        pdf: 'fas fa-file-pdf',
        doc: 'fas fa-file-word',
        docx: 'fas fa-file-word',
        xls: 'fas fa-file-excel',
        xlsx: 'fas fa-file-excel',
        ppt: 'fas fa-file-powerpoint',
        pptx: 'fas fa-file-powerpoint',
        txt: 'fas fa-file-alt',
        jpg: 'fas fa-file-image',
        jpeg: 'fas fa-file-image',
        png: 'fas fa-file-image',
        gif: 'fas fa-file-image',
        zip: 'fas fa-file-archive',
        rar: 'fas fa-file-archive'
    };
    return icons[ext] || 'fas fa-file';
}

// 파일 목록 업데이트
function updateFileList() {
    $('#fileList').empty();
    
    if (selectedFiles.length === 0) {
        $('#noFileMessage').show();
        $('#fileList').append($('#noFileMessage'));
        return;
    }
    
    $('#noFileMessage').hide();
    
    selectedFiles.forEach((file, index) => {
        const fileItem = $(`
            <div class="file-item">
                <div class="file-info">
                    <i class="${getFileIcon(file.name)} file-icon"></i>
                    <span class="file-name">${file.name}</span>
                    <span class="file-size">(${formatFileSize(file.size)})</span>
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm" data-index="${index}">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `);
        $('#fileList').append(fileItem);
    });
}

// 파일 입력 업데이트
function updateFileInput() {
    const dt = new DataTransfer();
    selectedFiles.forEach(file => dt.items.add(file));
    $('#fileInput')[0].files = dt.files;
}

// 폼 유효성 검사
function validateForm() {
    let isValid = true;
    
    // 제목 검사
    const title = $('#boardTitle').val().trim();
    if (!title) {
        $('#titleDiv').text('제목을 입력해주세요.');
        isValid = false;
    } else {
        $('#titleDiv').text('');
    }
    
    // 내용 검사
    const contents = $('#contents').val().trim();
    if (!contents) {
        $('#contentsDiv').text('내용을 입력해주세요.');
        isValid = false;
    } else {
        $('#contentsDiv').text('');
    }
    
    return isValid;
}

function fn_menuLink(topMenu, subMenu) {
    var url = '/user/' + topMenu + '/' + subMenu;
    window.location.href = url;
}