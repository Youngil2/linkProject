$(document).ready(function () {
		const token = $("meta[name='_csrf']").attr("content");
	    const header = $("meta[name='_csrf_header']").attr("content");
		
		fn_topMenuNm()
		
			// 중복 체크 버튼
			$("#subMenuChk").on("click", function () {
				const menuField = $("#subMenu");
				const menuNmField = $("#subMenuNm");

				const subMenu =  $("#subMenu").val().toUpperCase();
				const subMenuNm = $("#subMenuNm").val();
				
				const _subMenu = $("#_subMenu").val();
				const _subMenuNm = $("#_subMenuNm").val();
				const topMenuNm = $("#topMenuNm").val();
				
				const isSubMenuChanged = subMenu !== _subMenu;
				const isSubMenuNmChanged = subMenuNm !== _subMenuNm;
				
				if (!topMenuNm) {
					alert("상위메뉴를 선택하세요");
					return;
				}
				
				if (!subMenu) {
					alert("하위메뉴 영문명 입력 후 버튼을 눌러주세요");
					menuField.focus();
					return;
				}
				if (!subMenuNm) {
					alert("하위메뉴 한글명 입력 후 버튼을 눌러주세요");
					menuNmField.focus();
					return;
				}
				
				// 중복 체크가 불필요한 경우
				if (!isSubMenuChanged && !isSubMenuNmChanged) {
					menuField.attr('status', 'yes');
					menuNmField.attr('statusNm', 'yes');

					$(".checkSubMenuSpan").remove();
					$("#subMenuDiv").append("<span class='checkSubMenuSpan text-primary'>기존과 동일한 영문명입니다.</span>");

					$(".checkSubMenuNmSpan").remove();
					$("#subMenuNmDiv").append("<span class='checkSubMenuNmSpan text-primary'>기존과 동일한 한글명입니다.</span>");
					return;
				}
				// 영문명만 바뀐 경우
				if (isSubMenuChanged) {
					$.ajax({
						url: '/admin/subMenuChk',
						type: 'POST',
						data: JSON.stringify({ subMenu: subMenu }),
						contentType: "application/json; charset=utf-8",
						beforeSend: function (xhr) {
							xhr.setRequestHeader(header, token);
						},
						success: function (result) {
							$(".checkSubMenuSpan").remove();
							if (result != 0) {
								menuField.attr('status', 'no').val('');
								$("#subMenuDiv").append("<span class='checkSubMenuSpan text-danger'>이미 존재하는 하위메뉴 영문명입니다.</span>");
							} else {
								menuField.attr('status', 'yes');
								menuNmField.attr('statusNm', 'yes');
								$("#subMenuDiv").append("<span class='checkSubMenuSpan text-primary'>사용 가능한 하위메뉴 영문명입니다.</span>");
							}
						},
						error: function () {
							alert("하위메뉴 영문명 중복체크 중 에러가 발생하였습니다.");
						}
					});
				}

				// 한글명만 바뀐 경우
				if (isSubMenuNmChanged) {
					$.ajax({
						url: '/admin/subMenuNmChk',
						type: 'POST',
						data: JSON.stringify({ subMenuNm: subMenuNm }),
						contentType: "application/json; charset=utf-8",
						beforeSend: function (xhr) {
							xhr.setRequestHeader(header, token);
						},
						success: function (nmResult) {
							$(".checkSubMenuNmSpan").remove();
							if (nmResult != 0) {
								menuNmField.attr('statusNm', 'no').val('');
								$("#subMenuNmDiv").append("<span class='checkSubMenuNmSpan text-danger'>이미 존재하는 하위메뉴 한글명입니다.</span>");
							} else {
								menuField.attr('status', 'yes');
								menuNmField.attr('statusNm', 'yes');
								$("#subMenuNmDiv").append("<span class='checkSubMenuNmSpan text-primary'>사용 가능한 하위메뉴 한글명입니다.</span>");
							}
						},
						error: function () {
							alert("하위메뉴 한글명 중복체크 중 에러가 발생하였습니다.");
						}
					});
				}
			});
			
				// 생성 버튼
				$("#subMenuCreate").on("click", function () {
					const subMenu = $("#subMenu").val().toUpperCase();
					const subMenuNm = $("#subMenuNm").val();
					const menuStatus = $("#subMenu").attr('status');
					const menuNmStatus = $("#subMenuNm").attr('statusNm');

					if (!subMenu) {
						alert("하위메뉴 영문명을 작성해주세요");
						$("#subMenu").focus();
						return;
					}
					if (!subMenuNm) {
						alert("하위메뉴 한글명을 작성해주세요");
						$("#subMenuNm").focus();
						return;
					}
					if (menuStatus !== "yes" || menuNmStatus !== "yes") {
						alert("중복 체크를 해주세요.");
						return;
					}

					if (confirm(`${subMenuNm} 메뉴를 생성하시겠습니까?`)) {
						const formData = new FormData(document.getElementById("subMenuForm"));
						formData.set("subMenu", subMenu); // 영문 대문자 설정
						
						$.ajax({
							url: '/admin/subMenuCreate',
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
									window.location.href = '/admin/menu_mng/sub_menu_mng';
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
				$("#subMenuUpdate").on("click", function () {
					const subMenu = $("#subMenu").val().toUpperCase();
					const subMenuNm = $("#subMenuNm").val();
							
					const subMenuSeq = $("#subMenuSeq").val();
					const menuStatus = $("#subMenu").attr('status');
					const menuNmStatus = $("#subMenuNm").attr('statusNm');

					if (!subMenu) {
						alert("하위메뉴 영문명을 작성해주세요");
						$("#subMenu").focus();
						return;
					}
					if (!subMenuNm) {
						alert("하위메뉴 한글명을 작성해주세요");
						$("#subMenuNm").focus();
						return;
					}
					
					if (menuStatus !== "yes" || menuNmStatus !== "yes") {
						alert("중복 체크를 해주세요.");
						return;
					}
					const _subMenuNm = $("#_subMenuNm").val();
					if (confirm(`${_subMenuNm} 메뉴를 수정하시겠습니까?`)) {
						const formData = new FormData(document.getElementById("subMenuForm"));
						formData.set("subMenu", subMenu);
						formData.set("subMenuNm", subMenuNm);

						$.ajax({
							url: '/admin/subMenuUpdate',
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
									window.location.href = '/admin/sub_menu_update?sub_menu_seq='+subMenuSeq;
								} else {
									alert("하위메뉴 수정에 실패했습니다.");
								}
							},
							error: function () {
								alert("하위메뉴 수정 요청 중 오류가 발생했습니다.");
							}
						});
					} else {
						alert("수정이 취소되었습니다.");
					}
				});
		
})

//상위메뉴 이름조회(하위메뉴 생성 폼)
function fn_topMenuNm(){
	const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

	const selectedTopMenuNm = $("#_topMenuNm").val();
	
    $.ajax({
        url: '/admin/selectTopList',
        type: 'POST',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(data) {
			console.log(data.topMenuList)
			console.log("하위메뉴 셀렉트리스트 : ",data.topMenuList)
            var topMenuNm = $('#topMenuNm');
            topMenuNm.empty(); // 기존 옵션 삭제

            topMenuNm.append('<option value="">선택하세요</option>');
			$.each(data.topMenuList, function(index, item) {
			               const isSelected = (item.topMenuNm === selectedTopMenuNm) ? ' selected' : '';
			               topMenuNm.append('<option value="' + item.topMenuNm + '"' + isSelected + '>' + item.topMenuNm + '</option>');
			           });
        },
        error: function() {
            alert('데이터를 불러오는 중 오류가 발생했습니다.');
        }
    });
}