$(document).ready(function() {
    // configId 변수 확인
    let configId = getConfigId();
	console.log("configId " +configId);
    // historyId 변수 확인
	let historyId = getHistoryId();
	console.log("historyId " +historyId);
    if (!configId || !historyId) {
        console.error('configId 또는 historyId가 정의되지 않았습니다.');
        return;
    }
    
	historyStatistics(configId);
	selectHistory(historyId);
});

// configId를 가져오는 함수 (URL 파라미터에서)
function getConfigId() {
	const urlParams = new URLSearchParams(window.location.search);
	const configIdFromUrl = urlParams.get('config_id');
	return configIdFromUrl;
}
// historyId를 가져오는 함수 (URL 파라미터에서)
function getHistoryId() {
	const urlParams = new URLSearchParams(window.location.search);
	const historyIdFromUrl  = urlParams.get('history_id');
	return historyIdFromUrl;
}

function historyStatistics(configId){
	// CSRF 토큰 설정
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
	
	$.ajax({
	    url: '/admin/historyStatistics',
	    type: 'POST',
	    data: {configId: configId},
	    beforeSend: function (xhr) {
			xhr.setRequestHeader(header, token);
	    },
	    success: function (data) {
	        console.log("이력통계", data);            
	        topUpdateUI(data);
	    },
	    error: function (xhr) {
	        console.log('=== 이력통계 조회 에러 ===');
	        console.log('Status:', xhr.status);
	        console.log('Response Text:', xhr.responseText);
	        console.log('Response JSON:', xhr.responseJSON);
	        alert('이력통계 조회 중 오류가 발생했습니다.');
	    }
	});	   
}
function selectHistory(historyId){
	// CSRF 토큰 설정
	const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");

	$.ajax({
	    url: '/admin/selectHistory',
	    type: 'POST',
	    data: {historyId: historyId},
	    beforeSend: function (xhr) {
			xhr.setRequestHeader(header, token);
	    },
	    success: function (data) {
	        console.log("연계 정보 ", data);            
	        underUpdateUI(data);
	    },
	    error: function (xhr) {
	        console.log('=== 이력통계 조회 에러 ===');
	        console.log('Status:', xhr.status);
	        console.log('Response Text:', xhr.responseText);
	        console.log('Response JSON:', xhr.responseJSON);
	        alert('연계정보 조회 중 오류가 발생했습니다.');
	    }
	});	   
	
}

// UI 업데이트 함수
function topUpdateUI(data) {
    if (data) {
        // 통계 카드 업데이트
		$('#processedCount').text(data.processedCount || 0);
		$('#errorCount').text(data.errorCount || 0);
		$('#executeDuration').text(data.executeDurationInSeconds || 0);
		$('#successRate').text(data.successRate + '%' || '0%'); 
    }
}

function underUpdateUI(data) {
    if (data) {         
        // 기본 정보 업데이트
        $('#configId').text(data.configId || '-');
        $('#apiName').text(data.apiName || '-');
        $('#executeTime').text(data.executeTime || '-');
        $('#status').text(data.status || '-');
        $('#message').text(data.message || '-');
        
        // 실행 시간 정보 업데이트
        $('#executeStartTime').text(data.executeStartTime || '-');
        $('#executeEndTime').text(data.executeEndTime || '-');
        $('#executeDurationDetail').text(data.executeDuration  || '-');
        $('#processedCountDetail').text(data.processedCount || '-');
        $('#errorCountDetail').text(data.errorCount || '-');
        $('#processingSpeed').text(data.processingSpeed || '-');
    }
}

function fn_apiLink(){
	let configId = getConfigId();
	var option = "width = 1500, height = 1500, top = 100, left = 200, location = no"
	url="/admin/api_pop_up?config_id="+configId;
	window.open(url, "_blank", option);
}