let currentTopPage = 1;
let totalTopPages = 1;

$(document).ready(function () {
    // 초기 로딩
    fn_menuList();

    // 검색 버튼 클릭 시
    $('#searchButton').on('click', function () {
        currentTopPage = 1; // 검색 시 첫 페이지로 초기화
        fn_menuList();
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
	
	$('input[name="deleteYnChk"]').on('change', function() {
	    fn_menuList(); // 라디오 버튼이 변경될 때 리스트 재조회
	});
	
	$('.board-regist-btn').click(function() {
	    const subMenu = $(this).data('submenu');
		const topMenu = $(this).data('topmenu');
	    fn_menuLink('boardRegist', subMenu, topMenu);
	});
	
	$(document).on('click', '.selectBoard', function() {
	    const fqaSeq = $(this).data('faqseq');
	    const subMenu = $(this).data('submenu');
	    const topMenu = $(this).data('topmenu');
	    fn_menuLink('boardDetail', subMenu, topMenu, fqaSeq.toLowerCase());
	});
});

// 아코디언 클릭 이벤트 처리 함수 (별도 함수로 분리)
function handleAccordionClick(targetId) {
    const $target = $(targetId);
    const $button = $(`[data-bs-target="${targetId}"]`);
    const $allCollapses = $('#faqAccordion .accordion-collapse');
    const $allButtons = $('#faqAccordion .accordion-button');
    
    console.log('Clicked target:', targetId, 'Element found:', $target.length);
    
    // 현재 클릭한 아코디언이 열려있는지 확인
    const isCurrentlyOpen = $target.hasClass('show');
    
    // 모든 아코디언 닫기
    $allCollapses.removeClass('show');
    $allButtons.addClass('collapsed').attr('aria-expanded', 'false');
    
    // 클릭한 아코디언이 닫혀있었다면 열기 (토글 기능)
    if (!isCurrentlyOpen) {
        $target.addClass('show');
        $button.removeClass('collapsed').attr('aria-expanded', 'true');
        
        // 내용이 제대로 표시되는지 확인
        setTimeout(() => {
            console.log('Content after opening:', $target.find('.faq-detail-content').html());
        }, 100);
    }
}

function fn_menuLink(_page, subMenu, topMenu, boardSeq = null) {
	url = '/user';

	if (_page == 'boardRegist') {
	    url += '/' + topMenu + '/' + subMenu + '/regist';
	} else if (_page == 'boardDetail') {
	    url += '/' + topMenu + '/' + subMenu + '/board_view';
	    if (boardSeq) {
	        url += '?board_seq=' + boardSeq;
	    }
	}

	window.location.href = url;
}

function fn_menuList(action = null){
	const pageUnitTop = parseInt($('.pageUnitTop').val(), 10);
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
	
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
	    pageIndexTop: pageIndex,
	    searchKeyword: searchKeyword,
	    searchType: searchType,
	});
	
	$.ajax({
		url : '/user/faq_list_up',
		method : 'POST',
		dataType : 'json',
		contentType : 'application/json',
		data : param,
		beforeSend: function(xhr){
			xhr.setRequestHeader(header, token);
		},
		success: function(data){
			const list = data.list || [];
			const totalCount = data.totalCount || 0;
			const pageSize = pageUnitTop;
						
			// 총 페이지 수 계산
			totalTopPages = Math.ceil(totalCount / pageSize);
			
			// 아코디언 아이템 생성
			let html = "";
			list.forEach(function(item, index) {
				
				const currentUser = data.currentUserId.toUpperCase();
				// 수정 권한 확인: 작성자이거나 admin인 경우
				const canEdit = (currentUser === item.registUser) || (currentUserRole === 'ADMIN');
				
				const editButtonHtml = canEdit ? 
				    `<button type="button" class="btn btn-sm btn-outline-primary ms-2" onclick="editItem(${item.faqSeq})">수정</button>` : 
				    '';
				
			    const itemNumber = (pageIndex - 1) * pageSize + index + 1;
			    // 고유한 ID 생성 (boardSeq + 페이지 인덱스 + 아이템 인덱스로 완전히 고유하게)
			    const accordionId = `collapse_${item.boardSeq}_${pageIndex}_${index}`;
				const content = item.answer || '내용이 없습니다.';
				html += `
				<div class="accordion-item">
				    <h2 class="accordion-header">
				        <button class="accordion-button collapsed" type="button" 
				                data-bs-target="#${accordionId}" 
				                aria-expanded="false" 
				                aria-controls="${accordionId}"
				                onclick="handleAccordionClick('#${accordionId}'); return false;">
				            <div class="d-flex justify-content-between align-items-center w-100 me-3">
				                <div class="fw-semibold">
				                    <span class="badge bg-primary me-2">${itemNumber}</span>
				                    ${item.question || '제목 없음'}
				                </div>
				            </div>
				        </button>
				    </h2>
				    <div id="${accordionId}" class="accordion-collapse collapse">
				        <div class="accordion-body">
				            <div class="faq-content">
				                <div class="faq-detail-content" style="min-height: 50px; padding: 10px;">
				                    ${content ? content.replace(/\n/g, '<br>') : '<p>내용이 없습니다.</p>'}
				                </div>
				            </div>
							<div class="d-flex justify-content-end mt-2">
							    ${editButtonHtml}
							</div>
				        </div>
				    </div>
				</div>
				`;
		   });
		   
		   $("#faqAccordion").html(html);
		   // 결과 수 표시
		   $("#totalCountTop").text(totalCount);			
		   renderTopPagination(pageIndex, totalTopPages);
	   },
	   error: function (xhr) {
	       console.error("Error fetching menu list", xhr);
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