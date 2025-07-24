const token = $("meta[name='_csrf']").attr("content");
const header = $("meta[name='_csrf_header']").attr("content");

$(".certifyBtn").on("click",function(){
	$.ajax({
		type : "POST",
		url : "/members/certifyMember",
		dataType: "json",
		data: { userEmail: $("#email").val() },
		beforeSend: function(xhr) {
			xhr.setRequestHeader(header, token);
		},
		success : function(data){
			alert("메일전송");			
		},
		error: function(xhr, status, error) {
			alert("에러가 발생했습니다. 관리자에게 문의하세요");
			console.log("에러 발생:", status, error);
		  	console.log("응답 본문:", xhr.responseText);
		}
	})
})
$(".certifyChk").on("click",function(){
	var number = $("#memberApprove").val();
	
	if(number == '' || number == null){
		alert("인증번호가 빈칸 입니다.")
		$("#memberApprove").focus();
		 return;
	}
	$.ajax({
		type : "POST",
		url : "/members/verify",
		data : {number : number},
		contentType: "application/x-www-form-urlencoded; charset=UTF-8",
		beforeSend: function(xhr) {
			xhr.setRequestHeader(header, token);
		},
		success : function(response){
			alert(response.message);
			if (response.redirectUrl) {
				document.location.href = response.redirectUrl;
			}			
		},
		error: function(xhr) {
			var errorResponse = JSON.parse(xhr.responseText);
			alert(errorResponse.message);
		}
	})	
})
