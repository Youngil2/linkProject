let currentTopPage = 1;
let totalTopPages = 1;

$(document).ready(function () {
    // 초기 로딩
    fn_menuList();

    // 검색 버튼 클릭 시
    $('#searchButton').on('click', function () {
        currentTopPage = 1; // 검색 시 첫 페이지로 초기화
        fn_menuList()();
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
	    const boardSeq = $(this).data('boardseq');
	    const subMenu = $(this).data('submenu');
	    const topMenu = $(this).data('topmenu');
	    fn_menuLink('boardDetail', subMenu, topMenu, boardSeq.toLowerCase());
	});
});


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
	const subMenu = $('#subMenu').val();
	const topMenu = $('#topMenu').val();
	
	const param = JSON.stringify({
	    pageUnit: pageUnitTop,
	    pageIndexTop: pageIndex,
	    searchKeyword: searchKeyword,
	    searchType: searchType,
		subMenu: subMenu.toUpperCase()
	});
	
	$.ajax({
		url : '/user/list_up',
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
			console.log(data);
			// 총 페이지 수 계산
			totalTopPages = Math.ceil(totalCount / pageSize);
			
			// 테이블 생성
			let html = "";
			list.forEach(function(item, index) {
				html += `
				<tr>
				    <td class="text-center">
				        ${(pageIndex - 1) * pageSize + index + 1}
				    </td>
				    <td class="text-start px-3">
					<a href="javascript:void(0);" class="text-decoration-none text-dark fw-semibold selectBoard" data-boardSeq="${item.boardSeq}" data-submenu="${subMenu}" data-topmenu="${topMenu}">
					    ${item.boardTitle || '제목 없음'}
					</a>
				    </td>
				    <td class="text-center">
				        ${item.updateUser || item.registUser}
				    </td>
				    <td class="text-center text-muted small">
				        ${item.updateDate ? formatDate(item.updateDate) : formatDate(item.registDate)}
				    </td>
				    <td class="text-center">
				        ${item.openCount || 0}
				    </td>
				</tr>
			  `;
		   });
		   
		   $("#tablelist").html(html);
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
// 날짜 포맷팅 함수
function formatDate(dateString) {
    if (!dateString) return "";
    
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}