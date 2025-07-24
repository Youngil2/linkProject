let currentTopPage = 1;
let totalTopPages = 1;

let sortable = null;
let hasOrderChanged = false;
let originalMenuOrder = [];
let currentMenuList = [];


$(document).ready(function () {
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

	// 순서 변경 버튼 이벤트
	$('#orderUpdate').on('click', function() {
	    enterOrderEditMode();
	});

	// 순서 저장 버튼 이벤트 (동적 생성되는 버튼)
	$(document).on('click', '#saveOrderBtn', function() {
	    saveMenuOrder();
	});


	// 페이지 수 변경 시 처리 (자동 바인딩됨)
	$('.pageUnitTop').on('change', function () {
	    currentTopPage = 1;
	    fn_menuList();
	});

	$('input[name="deleteYnChk"]').on('change', function() {
	    fn_menuList(); // 라디오 버튼이 변경될 때 리스트 재조회
	});
	
	// 순서 변경 취소 버튼 이벤트 (동적 생성되는 버튼)
	$(document).on('click', '#cancelOrderBtn', function() {
	    cancelOrderChange();
	});
});


// 리스트 호출
function fn_menuList(action = null, chk) {
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
	const deleteYn = $('input[name="deleteYnChk"]:checked').val();
	
    const param = JSON.stringify({
        pageUnit: pageUnitTop,
        pageIndexTop: pageIndex,
        searchKeyword: searchKeyword,
        searchType: searchType,
		deleteYn: deleteYn
    });

    $.ajax({
        url: '/admin/menu_mng/top_menu_mng_list',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: param,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function (data) {
            const topMenuList = data.topMenuList || [];
            const totalCount = data.totalCount || 0;
            const pageSize = pageUnitTop;
            
            // 현재 메뉴 리스트 저장
            currentMenuList = topMenuList;
            
            // 원본 순서 저장
            originalMenuOrder = topMenuList.map((item, index) => ({
                seq: item.topMenuSeq,
                order: item.topMenuOrder || index + 1
            }));
            
            // 총 페이지 수 계산
            totalTopPages = Math.ceil(totalCount / pageSize);
			
            // 테이블 생성
            let html = "";
            topMenuList.forEach(function (item, index) {
				const isDeleted = item.deleteYn === 'Y'; 
				const redStyle = isDeleted ? 'style="color: #dc3545; font-weight: bold;"' : '';
                html += `
                    <tr class="menu-row" data-seq="${item.topMenuSeq}" data-order="${item.topMenuOrder || index + 1}">
                        <td ${redStyle}>
                            <span class="order-display">${(pageIndex - 1) * pageSize + index + 1}</span>
                        </td>
                        <td ${redStyle}>${item.topMenu || ''}</td>
                        <td ${redStyle}>${item.topMenuNm || ''}</td>
						<td>
						    <button type="button" class="btn btn-outline-primary btn-sm me-1" onclick="fn_menuLink('menuUpdate', '${item.topMenuSeq}')">수정</button>
							${
								item.deleteYn === 'Y'
									? `<button type="button" class="btn btn-outline-success btn-sm" onclick="fn_activeMenu('${item.topMenuSeq}', '${item.topMenuNm}','Y')">활성화</button>`
									: `<button type="button" class="btn btn-outline-danger btn-sm" onclick="fn_activeMenu('${item.topMenuSeq}', '${item.topMenuNm}','N')">비활성화</button>`
							}
						</td>
                    </tr>
                `;
            });
            $("#topMenuTable").html(html);

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

// 순서 편집 모드 진입
function enterOrderEditMode() {
    // 테이블에 편집 모드 클래스 추가
    $('#topMenuTable').addClass('order-edit-mode');
    
    // 테이블 행에 드래그 가능 시각적 표시
    $('.menu-row').css({
        'cursor': 'grab',
        'user-select': 'none'
    });
    
    // 순서 변경 버튼 숨기기
    $('#orderUpdate').hide();
    
    // 저장/취소 버튼 추가
    if (!$('#saveOrderBtn').length) {
        const actionButtons = `
            <button class="btn btn-success me-2" id="saveOrderBtn" style="display: none;">
                <i class="fas fa-save me-1"></i>순서 저장
            </button>
            <button class="btn btn-outline-danger me-2" id="cancelOrderBtn">
                <i class="fas fa-times me-1"></i>취소
            </button>
        `;
        $('#orderUpdate').after(actionButtons);
    }
    
    // Sortable 초기화
    initSortable();
}

// 순서 편집 모드 종료
function exitOrderEditMode() {
    // 테이블 편집 모드 클래스 제거
    $('#topMenuTable').removeClass('order-edit-mode');
    
    // 테이블 행 스타일 복원
    $('.menu-row').css({
        'cursor': 'default',
        'user-select': 'auto'
    });
    
    // 순서 변경 버튼 다시 표시
    $('#orderUpdate').show();
    
    // 추가 버튼들 제거
    $('#saveOrderBtn, #cancelOrderBtn').remove();
    
    // Sortable 제거
    if (sortable) {
        sortable.destroy();
        sortable = null;
    }
    
    // 변경 상태 초기화
    hasOrderChanged = false;
}

// Sortable 초기화
function initSortable() {
    const tableBody = document.getElementById('topMenuTable');
    
    if (sortable) {
        sortable.destroy();
    }
    
    sortable = new Sortable(tableBody, {
        animation: 200,
        ghostClass: 'sortable-ghost',
        chosenClass: 'sortable-chosen',
        dragClass: 'sortable-drag',
        forceFallback: true,
        fallbackTolerance: 3,
        onStart: function(evt) {
            tableBody.classList.add('sorting');
            // 드래그 시작할 때 시각적 피드백
            evt.item.style.opacity = '0.8';
            evt.item.style.cursor = 'grabbing';
        },
        onEnd: function(evt) {
            tableBody.classList.remove('sorting');
            evt.item.style.opacity = '1';
            evt.item.style.cursor = 'grab';
            
            // 순서가 실제로 변경되었는지 확인
            if (evt.oldIndex !== evt.newIndex) {
                updateOrderNumbers();
                hasOrderChanged = true;
                $('#saveOrderBtn').show();
            }
        },
        onMove: function(evt) {
            // 드래그 중 시각적 피드백
            return true;
        }
    });
}

// 순서 번호 업데이트
function updateOrderNumbers() {
    const rows = $('#topMenuTable tr.menu-row');
    rows.each(function(index) {
        const newOrder = index + 1;
        $(this).find('.order-display').text(newOrder);
        $(this).attr('data-order', newOrder);
    });
}

// 메뉴 순서 저장
function saveMenuOrder() {
    if (!hasOrderChanged) {
        alert('변경된 순서가 없습니다.');
        return;
    }
    
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    // 현재 표시된 메뉴들의 새로운 순서 정보 수집
    const updatedMenuList = [];
    $('#topMenuTable tr.menu-row').each(function(index) {
        const seq = $(this).data('seq');
        const newOrder = index + 1;
        
        updatedMenuList.push({
            topMenuSeq: seq,
            topMenuOrder: newOrder
        });
    });
    
    // 로딩 표시
    showLoadingOverlay(true);
    
    // 메뉴 순서 업데이트 AJAX 호출
    $.ajax({
        url: '/admin/updateTopMenuOrder',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({ menuList: updatedMenuList }),
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            showLoadingOverlay(false);
            
            if (response.success || response.result === 'success') {
                alert('메뉴 순서가 성공적으로 저장되었습니다.');
                
                // 편집 모드 종료
                exitOrderEditMode();
                
                // 리스트 새로고침
                fn_menuList();
				$(document).trigger('menuOrderSaved');
            } else {
                alert('순서 저장에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
            }
        },
        error: function(xhr, status, error) {
            showLoadingOverlay(false);
            console.error('순서 저장 실패:', error);
            alert('순서 저장 중 오류가 발생했습니다.');
        }
    });
}

// 원본 순서로 복원
function restoreOriginalOrder() {
    const tbody = $('#topMenuTable');
    const rows = tbody.find('tr.menu-row').toArray();
    
    // 원본 순서대로 정렬
    rows.sort((a, b) => {
        const seqA = $(a).data('seq');
        const seqB = $(b).data('seq');
        const orderA = originalMenuOrder.find(item => item.seq === seqA)?.order || 999;
        const orderB = originalMenuOrder.find(item => item.seq === seqB)?.order || 999;
        return orderA - orderB;
    });
    
    // DOM 재배치
    tbody.empty().append(rows);
    
    // 순서 번호 복원
    rows.forEach((row, index) => {
        const seq = $(row).data('seq');
        const originalOrder = originalMenuOrder.find(item => item.seq === seq)?.order || index + 1;
        $(row).find('.order-display').text(originalOrder);
        $(row).attr('data-order', originalOrder);
    });
    
    hasOrderChanged = false;
}

// 로딩 오버레이 표시/숨김
function showLoadingOverlay(show) {
    if (show) {
        if (!$('#loadingOverlay').length) {
            $('body').append(`
                <div id="loadingOverlay" style="
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
                    background-color: rgba(0, 0, 0, 0.5); z-index: 9999;
                    display: flex; justify-content: center; align-items: center;
                ">
                    <div style="text-align: center; color: white;">
                        <div class="spinner-border mb-3"></div>
                        <div>저장 중...</div>
                    </div>
                </div>
            `);
        } else {
            $('#loadingOverlay').show();
        }
    } else {
        $('#loadingOverlay').hide();
    }
}

// 순서 변경 취소
function cancelOrderChange() {
    if (hasOrderChanged) {
        if (!confirm('변경된 순서가 취소됩니다. 계속하시겠습니까?')) {
            return;
        }
    }
    
    // 원본 순서로 복원
    restoreOriginalOrder();
    exitOrderEditMode();
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
	if(_page=='menuRegist'){
		url += '/top_menu_create';
	}else if(_page =='topMenuList'){
		url += '/menu_mng';
		url += '/top_menu_mng'; 
	}else if(_page == 'menuUpdate'){
		url += '/top_menu_update';
		url += '?top_menu_seq='+seq
	}
	window.location.href = url.toLowerCase();
}

function fn_activeMenu(topMenuSeq, topMenuNm, chk){
	
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
	
	let actionText = chk === 'Y' ? "활성화" : "비활성화";
	
	if (!confirm(`'${topMenuNm}' 메뉴를 ${actionText} 하시겠습니까?`)) {
		alert(`${actionText}를 취소하였습니다.`);
		return;
	}
	
	const param = JSON.stringify({
		topMenuSeq: topMenuSeq,
		deleteYn: chk === 'Y' ? 'N' : 'Y'
	});
	
	$.ajax({
		url: '/admin/top_menu_active',
		method: 'POST',
		contentType: 'application/json',
		dataType: 'json',
		data: param,
		beforeSend: function (xhr) {
			xhr.setRequestHeader(header, token);
		},
		success: function (data) {
			alert(`'${topMenuNm}' 메뉴가 ${actionText} 처리되었습니다.`);
			fn_menuList();
		},
		error: function (xhr) {
			console.error("Error updating top menu", xhr);
			alert("처리 중 오류가 발생했습니다.");
		}
	});
}