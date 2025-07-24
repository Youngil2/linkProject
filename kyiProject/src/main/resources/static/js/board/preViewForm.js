$(document).ready(function() { 
    
    $('#boardPreview').on('click', function() {
        previewBoard();
    });
	
});

// 미리보기 함수 - 개선된 버전
function previewBoard() {
    const title = $('#boardTitle').val();
    const contents = $('#contents').val();
    const subMenuNm = $('#subMenuNm').val();
    const secretYn = $('#secretYn').val();
    
    if (!title || !contents) {
        alert('제목과 내용을 입력해주세요.');
        return;
    }
    
    const secretText = secretYn === 'Y' ? '비공개' : '공개';
    const currentDate = new Date().toLocaleString('ko-KR');
    
    // 첨부파일 목록 HTML 생성
    let fileListHtml = '';
    if (selectedFiles.length > 0) {
        fileListHtml = `
            <div class="mb-4">
                <label class="form-label fw-bold">첨부파일</label>
                <div class="border rounded p-3 bg-light">
                    ${selectedFiles.map(file => `
                        <div class="d-flex align-items-center py-2 border-bottom">
                            <i class="${getFileIcon(file.name)} me-2"></i>
                            <span class="text-decoration-none">${file.name}</span>
                            <small class="text-muted ms-auto">${formatFileSize(file.size)}</small>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }
    
    const previewWindow = window.open('', 'preview', 'width=1000,height=800,scrollbars=yes');
    previewWindow.document.write(`
        <!DOCTYPE html>
        <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>미리보기 - ${title}</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">

            </head>
            <body class="bg-light">
                <div class="container py-5">
                    <div class="row justify-content-center">
                        <div class="col-lg-10 col-md-12">
                            <div class="card shadow-sm">
                                <div class="card-header text-white bg-primary text-center fw-bold fs-5">
                                    <i class="fas fa-eye me-2"></i>게시판 미리보기
                                </div>
                                <div class="card-body">
                                    <div>
                                        <!-- 기본 정보 섹션 -->
                                        <div class="row">
                                            <div class="col-md-6">
                                                <!-- 제목 -->
                                                <div class="mb-3">
                                                    <label for="boardTitle" class="form-label fw-bold">제목</label>
                                                    <input type="text" class="form-control" id="boardTitle" name="boardTitle" 
                                                           value="${title}" readonly>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <!-- 서브메뉴명 (읽기 전용) -->
                                                <div class="mb-3">
                                                    <label for="subMenuNm" class="form-label fw-bold">게시판</label>
                                                    <input type="text" class="form-control" id="subMenuNm" name="subMenuNm" 
                                                           value="${subMenuNm}" readonly>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 내용 섹션 -->
                                        <div class="mb-4">
                                            <label class="form-label fw-bold">내용</label>
                                            <div class="border rounded p-3 bg-light" style="min-height: 300px; white-space: pre-wrap;">${contents}</div>
                                        </div>
                                        
                                        <!-- 첨부파일 섹션 -->
                                        ${fileListHtml}

                                        <!-- 게시글 정보 섹션 -->
                                        <div class="row">
                                            <div class="col-md-3">
                                                <!-- 공개 여부 -->
                                                <div class="mb-3">
                                                    <label for="secretYn" class="form-label fw-bold">공개 여부</label>
                                                    <input type="text" class="form-control" id="secretYn" name="secretYn" 
                                                           value="${secretText}" readonly>
                                                </div>
                                            </div>
                                            <div class="col-md-3">
                                                <!-- 조회수 -->
                                                <div class="mb-3">
                                                    <label for="openCount" class="form-label fw-bold">조회수</label>
                                                    <input type="text" class="form-control" id="openCount" name="openCount" 
                                                           value="0" readonly>
                                                </div>
                                            </div>
                                            <div class="col-md-3">
                                                <!-- 등록일 -->
                                                <div class="mb-3">
                                                    <label for="registDate" class="form-label fw-bold">등록일</label>
                                                    <input type="text" class="form-control" id="registDate" name="registDate" 
                                                           value="${currentDate}" readonly>
                                                </div>
                                            </div>
                                            <div class="col-md-3">
                                                <!-- 작성자 -->
                                                <div class="mb-3">
                                                    <label for="registUser" class="form-label fw-bold">작성자</label>
                                                    <input type="text" class="form-control" id="registUser" name="registUser" 
                                                           value="사용자" readonly>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- 안내 메시지 -->
                                        <div class="alert alert-info mt-4">
                                            <i class="fas fa-info-circle me-2"></i>
                                            <strong>안내:</strong> 이것은 미리보기입니다. 실제 저장된 내용과 다를 수 있습니다.
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    // 창 크기 조정
                    window.addEventListener('load', function() {
                        document.title = '미리보기 - ${title}';
                    });
                    
                    // ESC 키로 창 닫기
                    document.addEventListener('keydown', function(e) {
                        if (e.key === 'Escape') {
                            window.close();
                        }
                    });
                </script>
            </body>
        </html>
    `);
    previewWindow.document.close();
}