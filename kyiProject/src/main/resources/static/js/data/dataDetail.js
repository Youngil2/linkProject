let currentTopPage = 1;
let totalTopPages = 1;

$(document).ready(function () {
    // 초기 로딩
    fn_menuList();

    // 검색 버튼 클릭 시
    $('#searchButton').on('click', function () {
		
        currentTopPage = 1; // 검색 시 첫 페이지로 초기화
        fn_menuList();  // 함수 호출 부분 수정
    });
	
	$('#searchKeyword').on('keypress', function (e) {
	    if (e.which === 13) { // 13 = Enter 키
	        e.preventDefault(); // 폼 전송 방지
	        $('#searchButton').click(); // 검색 버튼 클릭 트리거
	    }
	});

    // 페이지 수 변경 시 처리 (자동 바인딩됨)
    $('.pageUnitTop').on('change', function () {
        currentTopPage = 1;
        fn_menuList();
    });

	$('.board-list-btn').click(function() {
	    const subMenu = $(this).data('submenu');
		const topMenu = $(this).data('topmenu');
	    fn_menuLink('list',topMenu, subMenu);
	});
	
	$('#excelDownload').click(function(){
	    fn_excel('current'); // 현재 페이지만
	});

	$('#excelDownloadall').click(function(){
	    fn_excel('all'); // 전체 데이터
	});

});

function fn_menuList(action = null){
	getColumnName();
	getDataList(action);
}

// 더 간단한 해결책: Bootstrap 툴팁 사용
function getColumnName(){
	    const token = $("meta[name='_csrf']").attr("content");
	    const header = $("meta[name='_csrf_header']").attr("content");

	    const urlParams = new URLSearchParams(window.location.search);
	    const targetTable = urlParams.get('target_table');

	    const schema = targetTable.substring(0,4);
	    const tableName = targetTable.substring(5);

	    const tableInfo = {
	        schema: schema,
	        tableName: tableName
	    };
	    
	    $.ajax({
	        url: '/user/getColumnName',
	        type: 'POST',
	        data: JSON.stringify(tableInfo),
	        contentType: 'application/json',
	        beforeSend: function (xhr) {
	            xhr.setRequestHeader(header, token);
	        },
	        success: function(data) {
	            console.log('컬럼명 조회 성공:', data);

	            // 기존 테이블 헤더 초기화
	            $('#tablelistTitle').empty();

	            // 컬럼명으로 테이블 헤더 생성
				if (data.list && data.list.length > 0) {
				    let headerHtml = '';
				    
				    data.list.forEach(function(column, index) {
				        const columnName = column.columnname || '컬럼' + (index + 1);
				        const columnComment = column.columncomment || columnName; // 코멘트가 없으면 컬럼명 사용
				        
				        // 화면에는 코멘트를 표시하고, data 속성에는 실제 컬럼명을 저장
						headerHtml += `<th scope="col" class="column-header" 
						                  data-column-name="${columnName}"
						                  data-column-comment="${columnComment}"
						                  data-full-text="${columnComment}">${columnComment}</th>`;
				    });
				    
				    // 테이블 헤더에 추가
				    $('#tablelistTitle').html(headerHtml);
				    
				    console.log('테이블 헤더 생성 완료 - 컬럼 수:', data.list.length);
				} else {
				    // 컬럼이 없는 경우 기본 메시지
				    $('#tablelistTitle').html('<th scope="col">데이터 없음</th>');
				    console.log('컬럼 정보가 없습니다.');
				}
	        },
	        error: function (xhr) {
	            console.log('=== 이력통계 조회 에러 ===');
	            console.log('Status:', xhr.status);
	            console.log('Response Text:', xhr.responseText);
	            console.log('Response JSON:', xhr.responseJSON);
	        }
	    });
	}

function getDataList(action = null){
	const pageUnitTop = parseInt($('.pageUnitTop').val(), 10);
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    const urlParams = new URLSearchParams(window.location.search);
    const targetTable = urlParams.get('target_table');

    const schema = targetTable.substring(0,4);
    const tableName = targetTable.substring(5);
	
	let pageIndex = currentTopPage;

	// 페이지 이동 처리
	if (action === 'prev' && pageIndex > 1) {
	    pageIndex--;
	} else if (action === 'next' && pageIndex < totalTopPages) {
	    pageIndex++;
	} else if (typeof action === 'number') {
	    pageIndex = action;
	}
	
	currentTopPage = pageIndex;

	const searchKeyword = $('#searchKeyword').val();
	const searchType = $('#searchType').val();
	
	const param = JSON.stringify({
	    pageUnit: pageUnitTop,
	    pageIndexTop: currentTopPage,
	    searchKeyword: searchKeyword,
	    searchType: searchType,
		schema: schema,
		tableName: tableName
	});

    $.ajax({
        url: '/user/getDataList',
        method: 'POST',
		dataType: 'json',
        contentType: 'application/json',
        data: param,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(data) {
            console.log('데이터 조회 성공:', data);
			const totalCount = data.totalCount || 0;
			const pageSize = pageUnitTop;
			
			// 총 페이지 수 계산
			totalTopPages = Math.ceil(totalCount / pageSize);
			
            // 반응형 테이블 스타일 적용
            applyResponsiveTableStyles();
            
            // 기존 테이블 바디 초기화
            $('#tablelist').empty();
			
			// 컬럼 순서 가져오기 (헤더에서 실제 컬럼명)
            const columnOrder = [];
            $('#tablelistTitle th').each(function() {
                const columnName = $(this).data('column-name'); // data-column-name 속성 사용
                columnOrder.push(columnName);
            });
            
            console.log('컬럼 순서:', columnOrder);
            
            // 데이터로 테이블 바디 생성
            if (data.list && data.list.length > 0) {
                let bodyHtml = '';
                
                data.list.forEach(function(row, index) {
                    bodyHtml += '<tr>';
                    
                    // 컬럼 순서에 따라 값 출력 (수정된 부분)
                    columnOrder.forEach(function(columnName) {
                        const value = row[columnName];
                        // null이나 undefined 처리
                        const displayValue = value !== null && value !== undefined ? String(value) : '';
                        const cleanValue = displayValue.trim();
                        
                        bodyHtml += `<td data-full-text="${cleanValue}" title="${cleanValue}">${cleanValue}</td>`;
                    });
                    
                    bodyHtml += '</tr>';
                });
                
                // 테이블 바디에 추가
                $('#tablelist').html(bodyHtml);
			    // 결과 수 표시
				$("#totalCountTop").text(totalCount);

				// 페이지네이션 렌더링
				renderTopPagination(pageIndex, totalTopPages);
                // 툴팁 이벤트 바인딩
                bindSafeTooltipEvents();
                
                console.log('테이블 데이터 생성 완료 - 행 수:', data.list.length);
            } else {
                // 데이터가 없는 경우
                const colCount = $('#tablelistTitle th').length || 1;
                $('#tablelist').html(`<tr><td colspan="${colCount}" class="text-center">데이터가 없습니다.</td></tr>`);
            }
        },
        error: function (xhr) {
 			console.error("Error fetching top menu list", xhr);
            
            // 에러 시 테이블에 에러 메시지 표시
            const colCount = $('#tablelistTitle th').length || 1;
            $('#tablelist').html(`<tr><td colspan="${colCount}" class="text-center text-danger">데이터를 불러오는 중 오류가 발생했습니다.</td></tr>`);
        }
    });
}
function fn_excel(type = 'current') {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    const urlParams = new URLSearchParams(window.location.search);
    const targetTable = urlParams.get('target_table');
    
    const schema = targetTable.substring(0,4);
    const tableName = targetTable.substring(5);
    
    const searchKeyword = $('#searchKeyword').val();
    const searchType = $('#searchType').val();
    
    // 컬럼 정보 수집 (헤더에서)
    const columns = [];
    $('#tablelistTitle th').each(function() {
        const columnName = $(this).data('column-name');
        const columnComment = $(this).data('column-comment');
        if (columnName) {
            columns.push({
                name: columnName,
                comment: columnComment || columnName
            });
        }
    });
    
    const param = {
        schema: schema,
        tableName: tableName,
        searchKeyword: searchKeyword,
        searchType: searchType,
        columns: columns,
        downloadType: type // 'current' 또는 'all'
    };
    
    // 현재 페이지만 다운로드하는 경우 페이지 정보 추가
    if (type === 'current') {
        const pageUnitTop = parseInt($('.pageUnitTop').val(), 10);
        param.pageUnit = pageUnitTop;
        param.pageIndexTop = currentTopPage;
    }
    
    console.log('엑셀 다운로드 요청:', param);
    
    // 다운로드 요청
    $.ajax({
        url: '/user/downloadExcel',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(param),
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            if (response.success) {
                // 파일 다운로드 링크 생성
                const link = document.createElement('a');
                link.href = '/user/downloadFile?fileName=' + encodeURIComponent(response.fileName);
                link.download = response.originalFileName;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                
                console.log('엑셀 파일 다운로드 시작:', response.originalFileName);
            } else {
                alert('엑셀 파일 생성에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
            }
        },
        error: function (xhr) {
            console.error('엑셀 다운로드 에러:', xhr);
            alert('엑셀 다운로드 중 오류가 발생했습니다.');
        }
    });
}
// 페이지네이션 렌더링 (10단위로 끊어서 표시)
function renderTopPagination(current, total) {
    const pageGroupSize = 10; // 한 번에 보여줄 페이지 개수
    
    // 현재 페이지가 속한 그룹 계산
    const currentGroup = Math.ceil(current / pageGroupSize);
    const startPage = (currentGroup - 1) * pageGroupSize + 1;
    const endPage = Math.min(startPage + pageGroupSize - 1, total);
    
    let html = `<ul class="pagination pagination-sm mb-0">`;

    // 맨 처음 페이지로 이동 버튼 (현재 그룹이 첫 번째 그룹이 아닐 때)
    if (startPage > 1) {
        html += `<li class="page-item">
            <button class="page-link" onclick="fn_menuList(1)" title="첫 페이지">≪</button>
        </li>`;
    }

    // 이전 그룹으로 이동 버튼 (이전 그룹이 있을 때)
    if (startPage > pageGroupSize) {
        const prevGroupLastPage = startPage - 1;
        html += `<li class="page-item">
            <button class="page-link" onclick="fn_menuList(${prevGroupLastPage})" title="이전 ${pageGroupSize}페이지">‹</button>
        </li>`;
    }

    // 이전 페이지 버튼
    html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
        <button class="page-link" onclick="fn_menuList('prev')">이전</button>
    </li>`;

    // 현재 그룹의 페이지 번호들만 표시
    for (let i = startPage; i <= endPage; i++) {
        html += `<li class="page-item ${current === i ? 'active' : ''}">
            <button class="page-link" onclick="fn_menuList(${i})">${i}</button>
        </li>`;
    }

    // 다음 페이지 버튼
    html += `<li class="page-item ${current === total ? 'disabled' : ''}">
        <button class="page-link" onclick="fn_menuList('next')">다음</button>
    </li>`;

    // 다음 그룹으로 이동 버튼 (다음 그룹이 있을 때)
    if (endPage < total) {
        const nextGroupFirstPage = endPage + 1;
        html += `<li class="page-item">
            <button class="page-link" onclick="fn_menuList(${nextGroupFirstPage})" title="다음 ${pageGroupSize}페이지">›</button>
        </li>`;
    }

    // 맨 마지막 페이지로 이동 버튼 (현재 그룹이 마지막 그룹이 아닐 때)
    if (endPage < total) {
        html += `<li class="page-item">
            <button class="page-link" onclick="fn_menuList(${total})" title="마지막 페이지">≫</button>
        </li>`;
    }

    html += `</ul>`;
    $('#topPagination').html(html);
}

function fn_menuLink(_page, topMenu, subMenu) {
    let url = '/user';

    if(_page == "list"){
        url += '/' + topMenu + '/' + subMenu;
    }
	
    window.location.href = url;
}

function applyResponsiveTableStyles() {
    // 기존 스타일 제거
    $('head').find('#dynamic-table-styles').remove();
    
    // 새로운 스타일 추가
    const styles = `
        <style id="dynamic-table-styles">
        /* 테이블 헤더 말줄임표 처리 */
        .table th {
            max-width: 150px; /* 더 넓게 설정 */
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            position: relative;
            cursor: pointer;
            padding: 8px 12px;
        }
        
        /* 테이블 데이터 셀 말줄임표 처리 */
        .table td {
            max-width: 150px; /* 더 넓게 설정 */
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            position: relative;
            cursor: pointer;
            padding: 8px 12px;
        }
        
        /* 커스텀 툴팁 스타일 */
        .custom-tooltip {
            position: absolute;
            background-color: #333;
            color: white;
            padding: 8px 12px;
            border-radius: 4px;
            font-size: 12px;
            z-index: 9999;
            white-space: nowrap;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            pointer-events: none;
            opacity: 1;
            max-width: 300px;
            word-wrap: break-word;
            white-space: normal;
        }
        
        /* 긴 텍스트용 툴팁 */
        .custom-tooltip.long-text {
            max-width: 400px;
            white-space: normal;
            word-wrap: break-word;
        }
        
        /* 테이블 반응형 처리 */
        .table-responsive {
            width: 100%;
            overflow-x: auto;
        }
        
        .table {
            width: 100%;
            min-width: 800px;
        }
        </style>
    `;
    
    $('head').append(styles);
}

// Bootstrap 툴팁 초기화 함수
function initBootstrapTooltips() {
    // 기존 툴팁 제거
    $('[data-bs-toggle="tooltip"]').tooltip('dispose');
    
    // 새로운 툴팁 초기화
    $('[data-bs-toggle="tooltip"]').tooltip();
}

// 또는 jQuery만 사용하는 더 안전한 버전
function bindSafeTooltipEvents() {
    // 기존 이벤트 제거
    $(document).off('mouseenter.safetooltip mouseleave.safetooltip');
    
    // 마우스 오버 시 툴팁 표시
    $(document).on('mouseenter.safetooltip', '.column-header, .table td', function(e) {
        const $this = $(this);
        
        // 헤더의 경우 코멘트 표시, 데이터 셀의 경우 전체 텍스트 표시
        let fullText;
        if ($this.hasClass('column-header')) {
            fullText = $this.attr('data-column-comment') || $this.text().trim();
        } else {
            fullText = $this.attr('data-full-text') || $this.text().trim();
        }
        
        const element = this;
        
        // 빈 텍스트는 툴팁 안 보여줌
        if (!fullText) return;
        
        // 텍스트가 잘렸는지 확인
        const isTextTruncated = element.scrollWidth > element.clientWidth || 
                               element.scrollHeight > element.clientHeight ||
                               fullText.length > 10;
        
        if (isTextTruncated) {
            // 기존 툴팁 제거
            $('.custom-tooltip').remove();
            
            // 툴팁 생성
            const tooltip = $('<div class="custom-tooltip show">' + fullText + '</div>');
            $('body').append(tooltip);
            
            // 툴팁 위치 설정
            const mouseX = e.pageX;
            const mouseY = e.pageY;
            const tooltipWidth = tooltip.outerWidth();
            const tooltipHeight = tooltip.outerHeight();
            const windowWidth = $(window).width();
            const windowHeight = $(window).height();
            const scrollTop = $(window).scrollTop();
            
            let left = mouseX - (tooltipWidth / 2);
            let top = mouseY - tooltipHeight - 10;
            
            // 경계 체크
            if (left < 10) left = 10;
            if (left + tooltipWidth > windowWidth - 10) {
                left = windowWidth - tooltipWidth - 10;
            }
            if (top < scrollTop + 10) {
                top = mouseY + 10;
            }
            
            tooltip.css({
                left: left,
                top: top,
                position: 'absolute',
                zIndex: 9999
            });
        }
    });
    
    // 마우스 아웃 시 툴팁 제거
    $(document).on('mouseleave.safetooltip', '.column-header, .table td', function() {
        $('.custom-tooltip').remove();
    });
    
    // 스크롤 시 툴팁 제거
    $(window).on('scroll.safetooltip', function() {
        $('.custom-tooltip').remove();
    });
}