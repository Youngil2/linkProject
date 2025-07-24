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
	
	$('input[name="useYn"]').on('change', function() {
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
	const useYn = $('input[name="useYn"]:checked').val();
	
    const param = JSON.stringify({
        pageUnit: pageUnitTop,
        pageIndexTop: pageIndex,
        searchKeyword: searchKeyword,
        searchType: searchType,
		useYn: useYn
    });

	    $.ajax({
	        url: '/admin/linkList',
	        method: 'POST',
	        dataType: 'json',
	        contentType: 'application/json',
	        data: param,
	        beforeSend: function (xhr) {
	            xhr.setRequestHeader(header, token);
	        },
	        success: function (data) {
	            const apiConfigList = data.apiConfigList || [];
	            const totalCount = data.totalCount || 0;
	            const pageSize = pageUnitTop;
	            // 총 페이지 수 계산
	            totalTopPages = Math.ceil(totalCount / pageSize);
				
	
	            // 테이블 생성
	            let html = "";
	            apiConfigList.forEach(function (item, index) {
					const useYn = item.useYn === 'N';
					const redStyle = useYn ? 'style="color: #dc3545; font-weight: bold;"' : '';
					// 실행 설정 값 설정 (CRON 또는 INTERVAL)
					 let executionSetting = '';
					 if (item.scheduleType === 'CRON') {
					     executionSetting = item.cronExpression || '';
					 } else if (item.scheduleType === 'INTERVAL') {
					     executionSetting = item.intervalMinutes ? `${item.intervalMinutes}분 간격` : '';
					 }
					 
					 // 실행/중지 버튼 조건
					 let controlButton = '';
					 if (item.scheduleStatus === 'RUNNING') {
					     controlButton = `<button type="button" class="btn btn-danger btn-sm me-1" onclick="stopSchedule('${item.configId}', '${item.apiName}')">
					                         <i class="fa fa-stop"></i> 중지
					                     </button>`;
					 } else {
					     controlButton = `<button type="button" class="btn btn-success btn-sm me-1" onclick="startSchedule('${item.configId}', '${item.apiName}')">
					                         <i class="fa fa-play"></i> 시작
					                     </button>`;
					 }
					 
					 // 실행 결과 상태 뱃지 (lastExecuteStatus)
					 let statusLabel = '<span class="badge bg-secondary">-</span>';
					 if (item.lastExecuteStatus === 'SUCCESS') {
					     statusLabel = '<span class="badge bg-primary">성공</span>';
					 } else if (item.lastExecuteStatus === 'FAIL') {
					     statusLabel = '<span class="badge bg-danger">실패</span>';
					 }

					 // 스케줄 상태 뱃지 (scheduleStatus)
					 let scheduleLabel = '<span class="badge bg-secondary">중지됨</span>';
					 if (item.scheduleStatus === 'RUNNING') {
					     scheduleLabel = '<span class="badge bg-success">실행 중</span>';
					 } else if (item.scheduleStatus === 'STOPPED') {
					     scheduleLabel = '<span class="badge bg-danger">중지됨</span>';
					 }
					 
					 html += `
					     <tr>
					         <td ${redStyle}>${(pageIndex - 1) * pageSize + index + 1}</td>
							 <td ${redStyle}>${item.apiName || ''}</td>
					         <td ${redStyle}>${item.scheduleType || ''}</td>
					         <td ${redStyle}>${executionSetting}</td>
					         <td ${redStyle}>${item.nextExecuteTime || ''}</td>
					         <td ${redStyle}>${item.lastExecuteTime || ''}</td>
					         <td ${redStyle}>${statusLabel}</td>
							 <td ${redStyle}>${scheduleLabel}</td>
							 <td>
								 ${controlButton}
							     <button type="button" class="btn btn-outline-primary btn-sm me-1" onclick="fn_menuLink('update','${item.configId}')">수정</button>
							     ${useYn
							         ? `<button type="button" class="btn btn-outline-success btn-sm" onclick="fn_toggleSchedule('${item.configId}', 'Y')">활성화</button>`
							         : `<button type="button" class="btn btn-outline-danger btn-sm" onclick="fn_toggleSchedule('${item.configId}', 'N')">비활성화</button>`
							     }
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

// 페이지네이션 렌더링
function renderTopPagination(current, total) {
    let html = `<ul class="pagination pagination-sm mb-0">`;

    // 이전 버튼 - 수정됨
    html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
        <button class="page-link" onclick="fn_menuList('prev')">이전</button>
    </li>`;

    // 페이지 번호 - 수정됨
    for (let i = 1; i <= total; i++) {
        html += `<li class="page-item ${current === i ? 'active' : ''}">
            <button class="page-link" onclick="fn_menuList(${i})">${i}</button>
        </li>`;
    }

    // 다음 버튼 - 수정됨
    html += `<li class="page-item ${current === total ? 'disabled' : ''}">
        <button class="page-link" onclick="fn_menuList('next')">다음</button>
    </li>`;

    html += `</ul>`;
    $('#topPagination').html(html);
}

// 스케줄 시작
function startSchedule(configId, apiName) {
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
    if(confirm(apiName + ' 스케줄을 시작하시겠습니까?')) {
        $.ajax({
            url: '/admin/startSchedule',
            type: 'POST',
            data: { configId: configId },
			beforeSend: function (xhr) {
			    xhr.setRequestHeader(header, token);
			},
            success: function(result) {
				console.log(configId +"연계 시작");
                alert(result.message);
				console.log(result);
                if(result.success) {
                    location.reload();
                }
            },
            error: function() {
                alert('처리 중 오류가 발생했습니다.');
            }
        });
    }
}

// 스케줄 중지
function stopSchedule(configId, apiName) {
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
    if(confirm(apiName + ' 스케줄을 중지하시겠습니까?')) {
        $.ajax({
            url: '/admin/stopSchedule',
            type: 'POST',
            data: { configId: configId },
			beforeSend: function (xhr) {
			    xhr.setRequestHeader(header, token);
			},
            success: function(result) {
                alert(result.message);
                if(result.success) {
                    location.reload();
                }
            },
            error: function() {
                alert('처리 중 오류가 발생했습니다.');
            }
        });
    }
}

// 전체 스케줄 시작
function startAllSchedules() {
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
    if(confirm('모든 스케줄을 시작하시겠습니까?')) {
        $.ajax({
            url: '/admin/startAllSchedules',
            type: 'POST',
			beforeSend: function (xhr) {
			    xhr.setRequestHeader(header, token);
			},
            success: function(result) {
                alert(result.message);
                if(result.success) {
                    location.reload();
                }
            },
            error: function() {
                alert('처리 중 오류가 발생했습니다.');
            }
        });
    }
}

// 전체 스케줄 중지
function stopAllSchedules() {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");
    
    if(confirm('모든 스케줄을 중지하시겠습니까?')) {
        $.ajax({
            url: '/admin/stopAllSchedules',
            type: 'POST',
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(result) {
                alert(result.message);
                if(result.success) {
                    // 알람 후 setTimeout으로 새로고침하여 중복 방지
                    setTimeout(() => {
                        location.reload();
                    }, 100);
                }
            },
            error: function() {
                alert('처리 중 오류가 발생했습니다.');
            }
        });
    }
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