$(document).ready(function () {
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");

	// 중복 체크 버튼
	$("#topMenuChk").on("click", function () {
		const menuField = $("#topMenu");
		const menuNmField = $("#topMenuNm");

		const topMenu =  $("#topMenu").val().toUpperCase();
		const topMenuNm = $("#topMenuNm").val();
		
		const _topMenu = $("#_topMenu").val();
		const _topMenuNm = $("#_topMenuNm").val();
		
		const isTopMenuChanged = topMenu !== _topMenu;
		const isTopMenuNmChanged = topMenuNm !== _topMenuNm;
		
		if (!topMenu) {
			alert("상위메뉴 영문명 입력 후 버튼을 눌러주세요");
			menuField.focus();
			return;
		}
		if (!topMenuNm) {
			alert("상위메뉴 한글명 입력 후 버튼을 눌러주세요");
			menuNmField.focus();
			return;
		}
		
		// 중복 체크가 불필요한 경우
		if (!isTopMenuChanged && !isTopMenuNmChanged) {
			menuField.attr('status', 'yes');
			menuNmField.attr('statusNm', 'yes');

			$(".checkTopMenuSpan").remove();
			$("#topMenuDiv").append("<span class='checkTopMenuSpan text-primary'>기존과 동일한 영문명입니다.</span>");

			$(".checkTopMenuNmSpan").remove();
			$("#topMenuNmDiv").append("<span class='checkTopMenuNmSpan text-primary'>기존과 동일한 한글명입니다.</span>");
			return;
		}
		// 영문명만 바뀐 경우
		if (isTopMenuChanged) {
			$.ajax({
				url: '/admin/topMenuChk',
				type: 'POST',
				data: JSON.stringify({ topMenu: topMenu }),
				contentType: "application/json; charset=utf-8",
				beforeSend: function (xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function (result) {
					$(".checkTopMenuSpan").remove();
					if (result != 0) {
						menuField.attr('status', 'no').val('');
						$("#topMenuDiv").append("<span class='checkTopMenuSpan text-danger'>이미 존재하는 상위메뉴 영문명입니다.</span>");
					} else {
						menuField.attr('status', 'yes');
						menuNmField.attr('statusNm', 'yes');
						$("#topMenuDiv").append("<span class='checkTopMenuSpan text-primary'>사용 가능한 상위메뉴 영문명입니다.</span>");
					}
				},
				error: function () {
					alert("상위메뉴 영문명 중복체크 중 에러가 발생하였습니다.");
				}
			});
		}

		// 한글명만 바뀐 경우
		if (isTopMenuNmChanged) {
			$.ajax({
				url: '/admin/topMenuNmChk',
				type: 'POST',
				data: JSON.stringify({ topMenuNm: topMenuNm }),
				contentType: "application/json; charset=utf-8",
				beforeSend: function (xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function (nmResult) {
					$(".checkTopMenuNmSpan").remove();
					if (nmResult != 0) {
						menuNmField.attr('statusNm', 'no').val('');
						$("#topMenuNmDiv").append("<span class='checkTopMenuNmSpan text-danger'>이미 존재하는 상위메뉴 한글명입니다.</span>");
					} else {
						menuField.attr('status', 'yes');
						menuNmField.attr('statusNm', 'yes');
						$("#topMenuNmDiv").append("<span class='checkTopMenuNmSpan text-primary'>사용 가능한 상위메뉴 한글명입니다.</span>");
					}
				},
				error: function () {
					alert("상위메뉴 한글명 중복체크 중 에러가 발생하였습니다.");
				}
			});
		}
	});

	// 생성 버튼
	$("#topMenuCreate").on("click", function () {
		const topMenu = $("#topMenu").val().toUpperCase();
		const topMenuNm = $("#topMenuNm").val();
		const menuStatus = $("#topMenu").attr('status');
		const menuNmStatus = $("#topMenuNm").attr('statusNm');

		if (!topMenu) {
			alert("상위메뉴 영문명을 작성해주세요");
			$("#topMenu").focus();
			return;
		}
		if (!topMenuNm) {
			alert("상위메뉴 한글명을 작성해주세요");
			$("#topMenuNm").focus();
			return;
		}
		if (menuStatus !== "yes" || menuNmStatus !== "yes") {
			alert("중복 체크를 해주세요.");
			return;
		}

		if (confirm(`${topMenuNm} 메뉴를 생성하시겠습니까?`)) {
			const formData = new FormData(document.getElementById("topMenuForm"));
			formData.set("topMenu", topMenu); // 영문 대문자 설정

			$.ajax({
				url: '/admin/menuCreate',
				method: 'POST',
				dataType: 'json',
				data: formData,
				contentType: false,
				processData: false,
				beforeSend: function (xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function (data) {
					if (data.state === true) {
						window.location.href = '/admin/menu_mng/top_menu_mng';
					} else {
						alert("상위메뉴 생성에 실패했습니다.");
					}
				},
				error: function () {
					alert("상위메뉴 생성 요청 중 오류가 발생했습니다.");
				}
			});
		} else {
			alert("생성이 취소되었습니다.");
		}
	});
	
	// 수정 버튼
	$("#topMenuUpdate").on("click", function () {
		const topMenu = $("#topMenu").val().toUpperCase();
		const topMenuNm = $("#topMenuNm").val();
				
		const topMenuSeq = $("#topMenuSeq").val();
		const menuStatus = $("#topMenu").attr('status');
		const menuNmStatus = $("#topMenuNm").attr('statusNm');

		if (!topMenu) {
			alert("상위메뉴 영문명을 작성해주세요");
			$("#topMenu").focus();
			return;
		}
		if (!topMenuNm) {
			alert("상위메뉴 한글명을 작성해주세요");
			$("#topMenuNm").focus();
			return;
		}
		
		if (menuStatus !== "yes" || menuNmStatus !== "yes") {
			alert("중복 체크를 해주세요.");
			return;
		}

		if (confirm(`${topMenuNm} 메뉴를 수정하시겠습니까?`)) {
			const formData = new FormData(document.getElementById("topMenuForm"));
			formData.set("topMenu", topMenu);
			formData.set("topMenuNm", topMenuNm);

			$.ajax({
				url: '/admin/topMenuUpdate',
				method: 'POST',
				dataType: 'json',
				data: formData,
				contentType: false,
				processData: false,
				beforeSend: function (xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function (data) {
					if (data.state === true) {
						window.location.href = '/admin/top_menu_update?top_menu_seq='+topMenuSeq;
					} else {
						alert("상위메뉴 수정에 실패했습니다.");
					}
				},
				error: function () {
					alert("상위메뉴 수정 요청 중 오류가 발생했습니다.");
				}
			});
		} else {
			alert("수정이 취소되었습니다.");
		}
	});
});
