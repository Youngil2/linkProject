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
	
	$('input[name="status"]').on('change', function() {
	    fn_menuList(); // 라디오 버튼이 변경될 때 리스트 재조회
	});
});

// 리스트 호출
function fn_menuList(action = null,chk) {
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
	const status = $('input[name="status"]:checked').val();
	
    const param = JSON.stringify({
        pageUnit: pageUnitTop,
        pageIndexTop: pageIndex,
        searchKeyword: searchKeyword,
        searchType: searchType,
		status: status
    });

	    $.ajax({
	        url: '/admin/linkHistory',
	        method: 'POST',
	        dataType: 'json',
	        contentType: 'application/json',
	        data: param,
	        beforeSend: function (xhr) {
	            xhr.setRequestHeader(header, token);
	        },
	        success: function (data) {
				console.log(data);
	            const historyList = data.historyList || [];
	            const totalCount = data.totalCount || 0;
	            const pageSize = pageUnitTop;
	            // 총 페이지 수 계산
	            totalTopPages = Math.ceil(totalCount / pageSize);
				
	
	            // 테이블 생성
	            let html = "";
	            historyList.forEach(function (item, index) {
					const status = item.status === 'FAIL';
					const redStyle = status ? 'style="color: #dc3545; font-weight: bold;"' : '';
					const truncatedMessage = item.message.length > 20
					  ? item.message.substring(0, 10) + '...' 
					  : item.message;
				    const countDisplay = status ? item.errorCount : item.processedCount;
					const executeTime = item.executeTime.substring(0, 16);
					 html += `
					     <tr>
					         <td ${redStyle}>${(pageIndex - 1) * pageSize + index + 1}</td>
							 <td ${redStyle}>${item.configId}</td>
					         <td ${redStyle}>${item.apiName}</td>
					         <td ${redStyle}>${executeTime}</td>
					         <td ${redStyle}>${item.status}</td>
					         <td ${redStyle}>${truncatedMessage}</td>
					         <td ${redStyle}>${countDisplay}</td>
							 <td>
							     <button type="button" class="btn btn-outline-primary btn-sm me-1" onclick="fn_linkForm('${item.historyId}','${item.configId}')">상세보기</button>
							 </td>
					     </tr>						 
					`;
	            });
	            $("#tablelist").html(html);
	            // 결과 수 표시
	            $("#totalCountTop").text(totalCount);
	
	            // 페이지네이션 렌더링
	            renderTopPagination(pageIndex, totalTopPages);
	        },
	        error: function (xhr) {
	            console.error("Error fetching top menu list", xhr);
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


function fn_menuLink(_page,seq){
	url = '/admin';
	if(_page=='regist'){
		url += '/link_create';
	}else if(_page =='datalink_mng'){
		url += '/datalink_mng';
		url += '/link_mng'; 
	}else if(_page == 'update'){
		url += '/datalink_update';
		url += '?config_id='+seq
	}
	window.location.href = url.toLowerCase();
}
function fn_linkForm(seq,id){
	url = '/admin/selectHistory?history_id=' + seq + '&config_id='+id;
	window.location.href = url;
}