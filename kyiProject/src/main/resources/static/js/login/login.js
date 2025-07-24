const token = $("meta[name='_csrf']").attr("content");
const header = $("meta[name='_csrf_header']").attr("content");


//아이디 중복체크
$(document).on("click", "#memberChk", function () {
	var memberId = $("#registerMemberId").val();	
	var memberIdField = $("#registerMemberId");
	var engCheck = /^[a-zA-Z]{6,20}$/;
	var status = memberIdField.attr("status");
	if (memberId == "" || memberId == null) {
		alert("아이디 입력 후 버튼을 눌러주세요");
	}else if(!engCheck.test(memberId)){
		alert("아이디는 영문으로 6자 이상 20자 이하로 작성하세요");
		memberIdField.val("");
		memberIdField.focus();
	}
	else{
		$.ajax({
			url : "/members/idChk",
			type : 'POST',
			data: JSON.stringify({ memberId: memberId }),
			contentType : "application/json; charset=utf-8",
			beforeSend: function(xhr) {
							xhr.setRequestHeader(header, token); // CSRF 토큰 추가
					    },
			success : function(result){
				if(result != 0){
					memberIdField.val("");
					memberIdField.attr('status', 'no');
					alert("이미 존재하는 아이디입니다.");
				}else{
					var isConfirmed = confirm("사용할 수 있는 아이디 입니다. 사용하겠습니까?");
					if(!isConfirmed){
						memberIdField.val("");
					}else{
						memberIdField.attr("status", "yes");
					}
					
				}
			},error : function(e) {
				alert("아이디 중복체크 에러 발생하였습니다. 관리자에게 문의하세요.");
			}
		}) 
	}
})
//회원가입
$('#registerModal').on('hidden.bs.modal', function () {
    // 입력 필드 초기화
    $('#joinMember')[0].reset();
});
$(document).on("click", ".joinMemberButton", function () {
		
	var RSAModulus = $("#RSAModulus").val();
	var RSAExponent = $("#RSAExponent").val();
	
	var memberIdField = $("#registerMemberId");
	var status = memberIdField.attr("status");
	
	var memberId = $("#registerMemberId").val();
	var memberPw = $("#registerMemberPw").val();
	var memberPw2 = $("#registerMemberPw2").val();
	var email = $("#registerEmail").val();
	
	//이메일 형식 체크
	var regexp = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
	//비밀번호 숫자/특수문자 포함
	var reg = /^(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/;
	
	if(memberId == "" || memberId == null){
		alert("아이디가 공란입니다");
		$("#registerMemberId").focus();
		return;
	}
	if(status != "yes"){
		alert("아이디 중복체크를 하세요");
		return;
	}
	if (memberPw == "" || memberPw == null) {
	    alert("비밀번호를 입력하세요");
	    $("#registerMemberPw").focus();
	    return;
	}
	
	if(!reg.test(memberPw)){
		alert("비밀번호는 영문,숫자,특수문자 포함해야합니다.");
		$("#registerMemberPw").focus();
		$("#registerMemberPw").val('');
		$("#registerMemberPw2").val('');
		return;		
	}
	
	if (memberPw !== memberPw2) {
	    alert("비밀번호가 일치하지 않습니다");
	    $("#registerMemberPw2").focus();
		$("#registerMemberPw2").val('');
	    return;
	}

	if (email == "" || email == null) {
	    alert("이메일을 입력하세요");
	    $("#registerEmail").focus();
	    return;
	}	

	if(!regexp.test(email)){
		alert("이메일 형식이 아닙니다.");
		$("#registerEmail").focus();
		$("#registerEmail").val('');
		return;   
	}

	var userConfirm = confirm("가입하시겠습니까?");
	
	if(userConfirm){
		
		var rsa = new RSAKey();
		rsa.setPublic(RSAModulus,RSAExponent);
		
		var memberPw = rsa.encrypt(memberPw);
		$("#registerMemberPw").val(memberPw);
		
		var formData = new FormData();
		formData.append('memberId', memberId);
		formData.append('memberPw', memberPw); // 암호화된 비밀번호
		formData.append('email', email);
		formData.append('memberName', $("#registerName").val());
		formData.append('memberCompany', $("#registerCompany").val());
		
		$.ajax({
			url : "/members/join",
			type: 'POST',
			data: formData,
			contentType: false,
			processData: false,
			beforeSend: function(xhr) {
			 	xhr.setRequestHeader(header, token); // CSRF 토큰 추가
			},
			success: function(data) {
				alert("가입이 되었습니다. 로그인 시 승인번호를 입력하세요");
				// 모달 닫기
				$("#registerModal").modal("hide");
				// 모달 백드롭 제거
				$(".modal-backdrop").remove();
				// body에서 overflow 속성 초기화
				$("body").removeClass("modal-open");
			},
			error: function(e) {
			    alert("에러가 발생하였습니다. 관리자에게 문의하세요.");			           
			}			
		})		
	}else{
		alert("가입이 취소되었습니다.");
	}
})
//아이디 및 비밀번호 찾기
$(document).on('click', '.findMember', function () {
	var activeTabId = $('.tab-pane.active').attr('id');
	var email = $("#findIdEmail").val();
	//이메일 형식 체크
	var regexp = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
	if (activeTabId === 'find-id') {
		console.log("아이디찾기");
		
		if (email == "" || email == null) {
		    alert("이메일을 입력하세요");
		    $("#findIdEmail").focus();
		    return;
		}
		if(!regexp.test(email)){
			alert("이메일 형식이 아닙니다.");
			$("#findIdEmail").focus();
			$("#findIdEmail").val('');
			return;   
		}
		
		
	}else if(activeTabId === 'find-pw'){
		console.log("비밀번호찾기");
		alert(activeTabId);
	}
})
//로그인
$(document).on("click",".login", function(){
	
	var RSAModulus = $("#RSAModulus").val();
	var RSAExponent = $("#RSAExponent").val();
	
	var memberId = $("#memberId").val();
	var memberPw = $("#memberPw").val();
	
	if(memberId == "" || memberId == null){
		alert("아이디가 공란입니다.");
		$("#memberId").focus();
		return;
	}
	if(memberPw == "" || memberPw == null){
		alert("비밀번호를 입력하세요");
		$("#memberPw").focus();
		return;
	}
	
	var rsa = new RSAKey();
	rsa.setPublic(RSAModulus,RSAExponent);
			
	var memberId = rsa.encrypt(memberId);
	var memberPw = rsa.encrypt(memberPw);	

	$.ajax({
		url : "/members/login",
		type: 'POST',
		dataType : "json",
		data : {
			"memberId" : memberId, "memberPw" : memberPw
		},
		beforeSend: function(xhr) {
			xhr.setRequestHeader(header, token); // CSRF 토큰 추가
		},
		success: function(data) {
			if(data.state == true){
				document.location.href="/main";
			}else if(data.state == 'nonCertify'){
				document.location.href="/members/certify";
			}else if(data.state == 'nonApprove'){
				alert("회원승인이 안되어있습니다. 관리자에게 문의하세요. \n연락처 : 042-1111-1111");
			}else if(data.state == 'block'){
				alert("해당 회원은 차단되었습니다.");
			}else if(data.state == false){
				alert("로그인 실패하였습니다. 관리자에게 문의하세요")
			}else if(data.state === 'nonId' || data.state === 'pwNon'){
			    alert("아이디와 비밀번호를 확인하세요");
			}
		},
		error: function(e) {
			alert("에러가 발생하였습니다. 관리자에게 문의하세요.");			           
		}			
	})	
})