let currentTopPage = 1;
let totalTopPages = 1;

$(document).ready(function () {
    // 초기 로딩
    fn_memberList();

    // 검색 버튼 클릭 시
    $('#searchButton').on('click', function () {
        currentTopPage = 1; // 검색 시 첫 페이지로 초기화
        fn_memberList();
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
        fn_memberList();
    });
	
	$('input[name="approveYnChk"]').on('change', function() {
	    currentTopPage = 1; // 라디오 버튼 변경 시 첫 페이지로 초기화
	    fn_memberList(); // 라디오 버튼이 변경될 때 리스트 재조회
	});
	
	$('input[name="blockYnChk"]').on('change', function() {
	    currentTopPage = 1; // 라디오 버튼 변경 시 첫 페이지로 초기화
	    fn_memberList(); // 라디오 버튼이 변경될 때 리스트 재조회
	});
});


// 회원 리스트 호출
function fn_memberList(action = null) {
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
    const approveYn = $('input[name="approveYnChk"]:checked').val();
    const blockYn = $('input[name="blockYnChk"]:checked').val();
    
    const param = JSON.stringify({
        pageUnit: pageUnitTop,
        pageIndexTop: pageIndex,
        searchKeyword: searchKeyword,
        searchType: searchType,
        approveYn: approveYn,
        blockYn: blockYn
    });

    $.ajax({
        url: '/admin/client_mng/member_mng_list',
        method: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: param,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function (data) {
            const memberList = data.topMenuList || [];
            const totalCount = data.totalCount || 0;
            const pageSize = pageUnitTop;
            const currentUserId = data.currentUserId || ''; // 서버에서 현재 사용자 ID 받기
            
            // 총 페이지 수 계산
            totalTopPages = Math.ceil(totalCount / pageSize);
            
            // 테이블 생성
            let html = "";
            memberList.forEach(function (item, index) {
                const isBlocked = item.blockYn === 'Y';
                const isNotApproved = item.approveYn === 'N';
                const isAdmin = item.memberRole === 'ADMIN';
                const isCurrentUser = item.memberId === currentUserId; // 현재 사용자 여부 확인
                console.log(isCurrentUser +" "+ currentUserId)
                const redStyle = isBlocked ? 'style="color: #dc3545; font-weight: bold;"' : '';
                const grayStyle = isNotApproved ? 'style="color: #6c757d;"' : '';
                const styleToApply = isBlocked ? redStyle : (isNotApproved ? grayStyle : '');

                html += `
                    <tr>
                        <td ${styleToApply}>${(pageIndex - 1) * pageSize + index + 1}</td>
                        <td ${styleToApply}>${item.memberId || ''}</td>
                        <td ${styleToApply}>${item.email || ''}</td>
                        <td ${styleToApply}>${item.memberName || ''}</td>
                        <td ${styleToApply}>${item.memberCompany || ''}</td>
                        <td ${styleToApply}>${item.registDate ? new Date(item.registDate).toLocaleDateString('ko-KR') : ''}</td>
                        <td ${styleToApply}>
                            <span class="badge ${
                                item.blockYn === 'Y'
                                    ? 'bg-danger'
                                    : (item.approveYn === 'Y' ? 'bg-success' : 'bg-warning')
                            }">
                                ${item.blockYn === 'Y'
                                    ? '차단'
                                    : (item.approveYn === 'Y' ? '승인' : '미승인')
                                }
                            </span>
                        </td>
                        <td ${styleToApply}>${item.approveDate ? new Date(item.approveDate).toLocaleDateString('ko-KR') : ''}</td>
                        <td>
                            ${
                                !isCurrentUser && item.approveYn === 'Y'
                                ? `<button type="button" class="btn btn-outline-primary btn-sm me-1" onclick="fn_changeRole('${item.memberId}', '${item.memberRole}')">권한변경</button>`
                                : ``
                            }
                            
                            ${
                                !isCurrentUser && !isAdmin && item.blockYn !== 'Y' ? (
                                    item.approveYn === 'N'
                                    ? `<button type="button" class="btn btn-outline-success btn-sm me-1" onclick="fn_approveMember('${item.memberId}', '${item.memberName}', 'Y')">승인</button>`
                                    : `<button type="button" class="btn btn-outline-warning btn-sm me-1" onclick="fn_approveMember('${item.memberId}', '${item.memberName}', 'N')">불승인</button>`
                                ) : ''
                            }
                            
                            ${
                                !isCurrentUser && !isAdmin ? (
                                    item.blockYn === 'Y'
                                    ? `<button type="button" class="btn btn-outline-danger btn-sm" onclick="fn_blockMember('${item.memberId}', '${item.memberName}', 'N')">차단해제</button>`
                                    : `<button type="button" class="btn btn-outline-danger btn-sm" onclick="fn_blockMember('${item.memberId}', '${item.memberName}', 'Y')">차단</button>`
                                ) : ''
                            }
                        </td>
                    </tr>
                `;
            });
            $("#memberTable").html(html);

            // 결과 수 표시
            $("#totalCountTop").text(totalCount);

            // 페이지네이션 렌더링
            renderTopPagination(pageIndex, totalTopPages);
        },
        error: function (xhr) {
            console.error("Error fetching member list", xhr);
            alert("회원 목록을 불러오는데 실패했습니다.");
        }
    });
}

// 회원 권한부여
function fn_changeRole(memberId, memberRole) {
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
	
	let actionText = memberRole === 'ADMIN' ? "일반유저" : "관리자";
	
	if(!confirm(`${memberId} 회원을 ${actionText} 변경하시겠습니까?`)){
		alert(`${memberId} 회원을 ${actionText} 변경을 취소하였습니다.`);
		return;
	}
	
	const param = JSON.stringify({
		memberId : memberId,
		actionText : memberRole === 'ADMIN' ? 'USER' : 'ADMIN'
	});
	
	$.ajax({
		url : '/admin/changeRole',
		method : 'POST',
		dataType : 'json',
		contentType: 'application/json',
		data: param,
		beforeSend : function(xhr){
			xhr.setRequestHeader(header, token);
		},
		suceess:function(data){
			consle.log(data);
			alert('완료되었습니다.');
			fn_memberList();  			
		},
		error: function (xhr) {
			console.error("Error approving member", xhr);
		    alert('승인 처리 중 오류가 발생했습니다.');
		}
	});
}

// 회원 승인
function fn_approveMember(memberId, memberName,chk) {
        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");
		
		let actionText = chk === 'Y' ? "승인" : "미승인";

    if (!confirm(`${memberName} 회원을 ${actionText}하시겠습니까?`)) {
		   alert(`${memberName} 회원을 ${actionText}취소하였습니다.`);
		   return;
		}
		
		const param = JSON.stringify({
			memberId: memberId,
			approveYn: chk === 'Y' ? 'Y' : 'N'
		});

        $.ajax({
            url: '/admin/approve_member',
            method: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            data: param,
            beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function (data) {
				console.log(data)                
                    alert('완료되었습니다.');
                    fn_memberList();                
            },
            error: function (xhr) {
                console.error("Error approving member", xhr);
                alert('승인 처리 중 오류가 발생했습니다.');
            }
        });
    
}
function fn_blockMember(memberId, memberName,chk){
	const token = $("meta[name='_csrf']").attr("content");
	       const header = $("meta[name='_csrf_header']").attr("content");
		
		let actionText = chk === 'Y' ? "차단" : "차단 해제";

	   if (!confirm(`${memberName} 회원을 ${actionText}하시겠습니까?`)) {
		   alert(`${memberName} 회원을 ${actionText}취소하였습니다.`);
		   return;
		}
		
		const param = JSON.stringify({
			memberId: memberId,
			blockYn: chk === 'Y' ? 'Y' : 'N'
		});

	       $.ajax({
	           url: '/admin/block_member',
	           method: 'POST',
	           dataType: 'json',
	           contentType: 'application/json',
	           data: param,
	           beforeSend: function (xhr) {
	               xhr.setRequestHeader(header, token);
	           },
	           success: function (data) {
				console.log(data)                
	                   alert('완료되었습니다.');
	                   fn_memberList();                
	           },
	           error: function (xhr) {
	               console.error("Error approving member", xhr);
	               alert('승인 처리 중 오류가 발생했습니다.');
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