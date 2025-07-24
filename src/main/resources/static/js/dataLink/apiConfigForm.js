$(document).ready(function() {
    initializeForm();
	
	// 기존 이벤트 핸들러 제거 후 새로 바인딩
	$('#apiConfigTest').off('click').on('click', function(e) {
	    e.preventDefault();
	    e.stopPropagation();
	    
	    // 중복 클릭 방지
	    if (isApiTesting) {
	        console.log('이미 테스트가 진행 중입니다.');
	        return false;
	    }
	    
	    testApiConnection();
	    return false;
	});

	// form submit 이벤트도 방지 (만약 form 안에 버튼이 있다면)
	$('#apiConfigTest').closest('form').on('submit', function(e) {
	    e.preventDefault();
	    return false;
	});
});

// 페이지 언로드 시 진행 중인 요청 정리
$(window).on('beforeunload', function() {
    if (isApiTesting && apiTestAbortController) {
        apiTestAbortController.abort();
    }
});

function fn_menuLink(_page,seq){
	url = '/admin';
	if(_page=='apiConfigList'){
		url += '/datalink_mng/link_mng';
	}
	window.location.href = url.toLowerCase();
}

function initializeForm() {
    // 스케줄 설정 토글
    $('#scheduleYn').change(function() {
        if ($(this).is(':checked')) {
            $('#scheduleOptions').show();
        } else {
            $('#scheduleOptions').hide();
        }
    });
    
    // 초기 상태 설정
    if ($('#scheduleYn').is(':checked')) {
        $('#scheduleOptions').show();
    }
    
    // 스케줄 타입에 따른 설정 표시
    $('#scheduleType').change(function() {
        $('#cronSettings, #intervalSettings').hide();
        
        if ($(this).val() === 'CRON') {
            $('#cronSettings').show();
        } else if ($(this).val() === 'INTERVAL') {
            $('#intervalSettings').show();
        }
    });
    
    // 초기 스케줄 타입 설정
    if ($('#scheduleType').val()) {
        $('#scheduleType').trigger('change');
    }
    
    // 매핑 추가 버튼
    $('#addMappingBtn').click(function() {
        addMappingRow();
    });
    
    // 파라미터 추가 버튼
    $('#addParameterBtn').click(function() {
        addParameterRow();
    });
    
    // 기존 매핑/파라미터 데이터 로드 (수정 모드인 경우)
    loadExistingMappings();
    loadExistingParameters();
    
    // 연결 테스트 버튼 이벤트
    $('#apiConfigTest').click(function() {
        testApiConnection();
    });

	$('#apiConfigCreate').click(function() {
	    const token = $("meta[name='_csrf']").attr("content");
	    const header = $("meta[name='_csrf_header']").attr("content");
	    
	    if (validateForm()) {
	        const formData = {
	            apiName: $('#apiName').val(),
	            baseUrl: $('#baseUrl').val(),
	            serviceKey: $('#serviceKey').val(),
	            responseFormat: $('#responseFormat').val(),
	            targetTable: $('#targetTable').val(),
	            useYn: $('#useYn').val(),
	            scheduleYn: $('#scheduleYn').is(':checked') ? 'Y' : 'N',
	            scheduleType: $('#scheduleType').val(),
	            scheduleDesc: $('#scheduleDesc').val(),
	            cronExpression: $('#cronExpression').val(),
	            intervalMinutes: $('#intervalMinutes').val(),
	            mappingConfigJson: JSON.stringify(getMappingData()),
	            parameterConfigJson: JSON.stringify(getParameterData())
	        };
	        
	        console.log('=== 생성 - 전송할 데이터 ===');
	        console.log(JSON.stringify(formData, null, 2));
	        
	        $.ajax({
	            url: '/admin/apiConfigCreate',
	            type: 'POST',
	            contentType: 'application/json',
	            data: JSON.stringify(formData),
	            beforeSend: function (xhr) {
	                xhr.setRequestHeader(header, token);
	            },
	            success: function (data) {
	                console.log('=== 생성 - 서버 응답 ===');
	                console.log(data);
	                alert('생성 성공!');
	                var seq = data.configId;
	                window.location.href = '/admin/link_create?seq=' + seq;
	            },
	            error: function (xhr) {
	                console.log('=== 생성 - 에러 응답 ===');
	                console.log('Status:', xhr.status);
	                console.log('Response Text:', xhr.responseText);
	                console.log('Response JSON:', xhr.responseJSON);
	                alert('생성 중 에러 발생: ' + xhr.responseText);
	            }
	        });
	    }
	});

	// 수정 버튼 이벤트
	$('#apiConfigUpdate').click(function() {
	    const token = $("meta[name='_csrf']").attr("content");
	    const header = $("meta[name='_csrf_header']").attr("content");
		const seq = $('#configId').val();
	    if (validateForm()) {
	        const formData = {
				configId : $('#configId').val(),
	            apiName: $('#apiName').val(),
	            baseUrl: $('#baseUrl').val(),
	            serviceKey: $('#serviceKey').val(),
	            responseFormat: $('#responseFormat').val(),
	            targetTable: $('#targetTable').val(),
	            useYn: $('#useYn').val(),
	            scheduleYn: $('#scheduleYn').is(':checked') ? 'Y' : 'N',
	            scheduleType: $('#scheduleType').val(),
	            scheduleDesc: $('#scheduleDesc').val(),
	            cronExpression: $('#cronExpression').val(),
	            intervalMinutes: $('#intervalMinutes').val(),
	            mappingConfigJson: JSON.stringify(getMappingData()),
	            parameterConfigJson: JSON.stringify(getParameterData())
	        };
	        
	        console.log('=== 수정 - 전송할 데이터 ===');
	        console.log(JSON.stringify(formData, null, 2));
	        
	        $.ajax({
	            url: '/admin/apiConfigUpdate',
	            type: 'POST',
	            contentType: 'application/json',
	            data: JSON.stringify(formData),
	            beforeSend: function (xhr) {
	                xhr.setRequestHeader(header, token);
	            },
	            success: function (data) {
	                console.log('=== 수정 - 서버 응답 ===');
	                console.log(data);
	                alert('수정 성공!');
	                window.location.href = '/admin/datalink_update?config_id=' + seq;
	            },
	            error: function (xhr) {
	                console.log('=== 수정 - 에러 응답 ===');
	                console.log('Status:', xhr.status);
	                console.log('Response Text:', xhr.responseText);
	                console.log('Response JSON:', xhr.responseJSON);
	                alert('수정 중 에러 발생: ' + xhr.responseText);
	            }
	        });
	    }
	});
}

function addMappingRow() {
    var $container = $('#mappingContainer');
    var $template = $('#mappingTemplate');
    var $newRow = $template.find('.mapping-row').clone();
    var index = $container.children().length;
    
    $newRow.attr('data-index', index);
    $newRow.show();
    
    // 삭제 버튼 이벤트 추가
    $newRow.find('.btn-remove-mapping').click(function() {
        $(this).closest('.mapping-row').remove();
        updateMappingIndexes();
    });
    
    $container.append($newRow);
}

function addParameterRow() {
    var $container = $('#parameterContainer');
    var $template = $('#parameterTemplate');
    var $newRow = $template.find('.parameter-row').clone();
    var index = $container.children().length;
    
    $newRow.attr('data-index', index);
    $newRow.show();
    
    // 삭제 버튼 이벤트 추가
    $newRow.find('.btn-remove-mapping').click(function() {
        $(this).closest('.parameter-row').remove();
        updateParameterIndexes();
    });
    
    $container.append($newRow);
}

function updateParameterIndexes() {
    $('.parameter-row').each(function(index) {
        $(this).attr('data-index', index);
    });
}

// 기존 파라미터 데이터 로드 함수 개선
function loadExistingParameters() {
    console.log('=== 파라미터 데이터 로드 시작 ===');

    let existingParameterConfig = $('#apiConfigForm').data('parameter-config') || '';
    console.log('existingParameterConfig:', existingParameterConfig);

    $('#parameterContainer').empty();

    try {
        let parameters = [];

        if (typeof existingParameterConfig === 'string') {
            const trimmed = existingParameterConfig.trim();
            if (trimmed === '' || trimmed === '[]') {
                console.log('빈 파라미터 데이터 - 아무 행도 추가하지 않음');
                return;
            }
            parameters = JSON.parse(trimmed);
        } else {
            parameters = existingParameterConfig;
        }

        if (Array.isArray(parameters) && parameters.length > 0) {
            $.each(parameters, function(index, parameter) {
                addParameterRowWithData(parameter.paramName, parameter.paramValue);
            });
        } else {
            console.log('파라미터 배열이 비어있음 - 행 추가하지 않음');
        }
    } catch (e) {
        console.error('파라미터 데이터 파싱 오류:', e);
    }
}

function addParameterRowWithData(paramName, paramValue) {
    const $template = $('#parameterTemplate').html();
    const $row = $($.parseHTML($template));
    const index = $('#parameterContainer').children().length;

    $row.attr('data-index', index);
    $row.find('.param-name').val(paramName || '');
    $row.find('.param-value').val(paramValue || '');

    $row.find('.btn-remove-mapping').click(function() {
        $(this).closest('.parameter-row').remove();
        updateParameterIndexes();
    });

    $('#parameterContainer').append($row);
}


function updateMappingIndexes() {
    $('.mapping-row').each(function(index) {
        $(this).attr('data-index', index);
    });
}

// 기존 매핑 데이터 로드 함수 수정
function loadExistingMappings() {
    console.log('=== 매핑 데이터 로드 시작 ===');

    let existingMappingConfig = $('#apiConfigForm').data('mapping-config') || '';
    console.log('existingMappingConfig:', existingMappingConfig);

    $('#mappingContainer').empty();

    try {
        let mappings = [];

        if (typeof existingMappingConfig === 'string') {
            const trimmed = existingMappingConfig.trim();
            if (trimmed === '' || trimmed === '[]') {
                console.log('빈 매핑 데이터 - 아무 행도 추가하지 않음');
                return;
            }
            mappings = JSON.parse(trimmed);
        } else {
            mappings = existingMappingConfig;
        }

        if (Array.isArray(mappings) && mappings.length > 0) {
            $.each(mappings, function (index, mapping) {
                addMappingRowWithData(mapping.apiField, mapping.dbColumn);
            });
        } else {
            console.log('매핑 배열이 비어있음 - 행 추가하지 않음');
        }
    } catch (e) {
        console.error('매핑 데이터 파싱 오류:', e);
    }
}

	
	function addMappingRowWithData(apiField, dbColumn) {
	    const $template = $('#mappingTemplate').html();
	    const $row = $($.parseHTML($template));
	    const index = $('#mappingContainer').children().length;

	    $row.attr('data-index', index);
	    $row.find('.api-field').val(apiField || '');
	    $row.find('.db-column').val(dbColumn || '');

	    $row.find('.btn-remove-mapping').click(function () {
	        $(this).closest('.mapping-row').remove();
	        updateMappingIndexes();
	    });

	    $('#mappingContainer').append($row);
	}

function getMappingData() {
	var mappings = [];

	$('.mapping-row').each(function(index) {
	    var $row = $(this);
	    var apiField = $row.find('.api-field').val();
	    var dbColumn = $row.find('.db-column').val();
	    
	    console.log(`매핑 ${index + 1} - API 필드: "${apiField}", DB 컬럼: "${dbColumn}"`);
	    
	    // trim() 적용하고 빈 값 체크
	    apiField = apiField ? apiField.trim() : '';
	    dbColumn = dbColumn ? dbColumn.trim() : '';
	    
	    if (apiField && dbColumn) {
	        mappings.push({
	            apiField: apiField,
	            dbColumn: dbColumn
	        });
	    } else {
	        console.warn(`매핑 ${index + 1}이 비어있어서 제외됨`);
	    }
	});

	return mappings;
}

function getParameterData() {
	var parameters = [];

	$('.parameter-row').each(function(index) {
	    var $row = $(this);
	    var paramName = $row.find('.param-name').val();
	    var paramValue = $row.find('.param-value').val();
	    
	    console.log(`파라미터 ${index + 1} - 이름: "${paramName}", 값: "${paramValue}"`);
	    
	    // trim() 적용
	    paramName = paramName ? paramName.trim() : '';
	    paramValue = paramValue ? paramValue.trim() : '';
	    
		// 날짜 패턴이 있는 경우에만 치환
		if (paramValue) {
		    var processedValue = replaceDatePatterns(paramValue);
		    if (processedValue !== paramValue) {
		        console.log(`날짜 패턴 치환: "${paramValue}" → "${processedValue}"`);
		    }
		    paramValue = processedValue;
		}
		
	    if (paramName) {
	        parameters.push({
	            paramName: paramName,
	            paramValue: paramValue || '' // 기본값이 없어도 빈 문자열로 저장
	        });
	    } else {
	        console.warn(`파라미터 ${index + 1}의 이름이 비어있어서 제외됨`);
	    }
	});
    return parameters;
}

// 날짜 포맷팅 함수
function formatDate(date, format) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    switch(format) {
        case 'yyyyMMdd':
            return `${year}${month}${day}`;
        case 'yyyy-MM-dd':
            return `${year}-${month}-${day}`;
        case 'yyyy/MM/dd':
            return `${year}/${month}/${day}`;
        default:
            return `${year}${month}${day}`;
    }
}

// 파라미터 값에서 날짜 패턴을 실제 날짜로 치환하는 함수
function replaceDatePatterns(value, baseDate = new Date()) {
    if (!value || typeof value !== 'string') {
        return value;
    }
    
    // 날짜 패턴이 포함되어 있는지 먼저 확인
    const hasDatePattern = /\{(yyyyMMdd|yyyy-MM-dd|yyyy\/MM\/dd|today|yesterday|tomorrow)\}/i.test(value);
    
    if (!hasDatePattern) {
        return value; // 날짜 패턴이 없으면 원본 그대로 반환
    }
    
    // 날짜 패턴이 있을 때만 치환 작업 수행
    const patterns = {
        '{yyyyMMdd}': () => formatDate(baseDate, 'yyyyMMdd'),
        '{yyyy-MM-dd}': () => formatDate(baseDate, 'yyyy-MM-dd'),
        '{yyyy/MM/dd}': () => formatDate(baseDate, 'yyyy/MM/dd'),
        '{today}': () => formatDate(baseDate, 'yyyyMMdd'),
        '{yesterday}': () => {
            const yesterday = new Date(baseDate);
            yesterday.setDate(yesterday.getDate() - 1);
            return formatDate(yesterday, 'yyyyMMdd');
        },
        '{tomorrow}': () => {
            const tomorrow = new Date(baseDate);
            tomorrow.setDate(tomorrow.getDate() + 1);
            return formatDate(tomorrow, 'yyyyMMdd');
        }
    };
    
    let result = value;
    Object.keys(patterns).forEach(pattern => {
        if (result.includes(pattern)) {
            result = result.replace(new RegExp(pattern.replace(/[{}]/g, '\\$&'), 'g'), patterns[pattern]());
        }
    });
    
    return result;
}
// 중복 호출 방지를 위한 전역 변수
let isApiTesting = false;
let apiTestAbortController = null;

// 연결 테스트 함수 - URL 로깅 추가 버전
function testApiConnection() {
    // 이미 테스트 중인 경우 중단
    if (isApiTesting) {
        console.log('이미 API 테스트가 진행 중입니다.');
        return;
    }

    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    var baseUrl = $('#baseUrl').val().trim();
    var serviceKey = $('#serviceKey').val().trim();

    if (!baseUrl || !serviceKey) {
        alert('기본 URL과 서비스 키를 입력해주세요.');
        return;
    }

    // URL 형식 검증
    var urlPattern = /^https?:\/\/.+/;
    if (!urlPattern.test(baseUrl)) {
        alert('올바른 URL 형식을 입력해주세요. (http:// 또는 https://로 시작)');
        return;
    }

    // 테스트용 파라미터 수집
    var parameters = getParameterData();

    // 로딩 표시 및 중복 호출 방지 플래그 설정
    var $testBtn = $('#apiConfigTest');
    var originalText = $testBtn.text();
    $testBtn.prop('disabled', true).text('테스트 중...');
    
    // 중복 호출 방지 플래그 설정
    isApiTesting = true;
    
    // AbortController 생성 (필요시 요청 취소용)
    apiTestAbortController = new AbortController();

    console.log('=== API 테스트 시작 ===');
    console.log('baseUrl:', baseUrl);
    console.log('serviceKey 길이:', serviceKey.length);
    console.log('serviceKey (원본):', serviceKey);
    console.log('serviceKey (디코딩 시도):', decodeURIComponent(serviceKey));
    console.log('parameters:', parameters);
    
    // 브라우저에서 직접 테스트할 수 있는 URL도 생성
    let directTestUrl = baseUrl;
    if (parameters && parameters.length > 0) {
        const urlParams = [];
        urlParams.push('serviceKey=' + serviceKey);
        
        parameters.forEach(param => {
            if (param.paramName && param.paramValue !== undefined) {
                urlParams.push(encodeURIComponent(param.paramName) + '=' + encodeURIComponent(param.paramValue));
            }
        });
        
        directTestUrl += '?' + urlParams.join('&');
    } else {
        directTestUrl += '?serviceKey=' + serviceKey;
    }
    
    console.log('=== 브라우저 직접 테스트용 URL (클릭 가능) ===');
    console.log('%c' + directTestUrl, 'color: blue; text-decoration: underline; cursor: pointer;');
    
    // 서버 응답과 비교를 위한 URL 차이점 분석
    console.log('=== URL 문자 분석 ===');
    console.log('serviceKey 첫 10글자:', serviceKey.substring(0, 10));
    console.log('serviceKey 마지막 10글자:', serviceKey.substring(serviceKey.length - 10));
    console.log('serviceKey에 포함된 특수문자:', serviceKey.match(/[^a-zA-Z0-9]/g));
    
    // 서버로 전송되는 전체 데이터 로깅
    const requestData = {
        baseUrl: baseUrl,
        serviceKey: serviceKey,
        parameters: parameters
    };
    console.log('=== 서버로 전송되는 요청 데이터 ===');
    console.log(JSON.stringify(requestData, null, 2));
    
    console.log('=== 권장 HTTP 헤더 설정 ===');
    console.log('User-Agent: Mozilla/5.0 (compatible; API-Client/1.0)');
    console.log('Accept: application/json, application/xml, */*');
    console.log('Content-Type: application/json (POST 요청시)');
    
    // 실제 API 호출 URL 구성 시뮬레이션 (서버에서 어떻게 구성될 것인지 예상)
    let simulatedApiUrl = baseUrl;
    if (parameters && parameters.length > 0) {
        const urlParams = [];
        // serviceKey는 이미 인코딩되어 있을 수 있으므로 추가 인코딩하지 않음
        urlParams.push('serviceKey=' + serviceKey);
        
        parameters.forEach(param => {
            if (param.paramName && param.paramValue !== undefined) {
                // 파라미터 값만 인코딩 (이름과 값이 일반 텍스트인 경우)
                urlParams.push(encodeURIComponent(param.paramName) + '=' + encodeURIComponent(param.paramValue));
            }
        });
        
        simulatedApiUrl += '?' + urlParams.join('&');
    } else {
        // serviceKey가 이미 인코딩되어 있다면 추가 인코딩하지 않음
        simulatedApiUrl += '?serviceKey=' + serviceKey;
    }
    
    console.log('=== 예상되는 실제 API 호출 URL ===');
    console.log(simulatedApiUrl);

    // AJAX로 API 연결 테스트 요청
    $.ajax({
        url: '/admin/test-connection',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
            console.log('=== AJAX 요청 전송 ===');
            console.log('요청 URL:', '/admin/test-connection');
            console.log('요청 메소드:', 'POST');
            console.log('Content-Type:', 'application/json');
        },
        timeout: 45000, // 45초 타임아웃
        success: function(response) {
            console.log('=== API 응답 성공 ===');
            console.log('응답 데이터:', response);
            
            // 서버에서 실제 호출한 URL이 응답에 포함되어 있다면 출력
            if (response.actualUrl) {
                console.log('=== 서버에서 실제 호출한 URL ===');
                console.log(response.actualUrl);
                
                // URL 비교 분석
                console.log('=== URL 비교 분석 ===');
                console.log('클라이언트 예상 URL:', directTestUrl);
                console.log('서버 실제 URL:', response.actualUrl);
                console.log('URL 일치:', directTestUrl === response.actualUrl);
            }
            
            // 요청/응답 헤더 정보가 있다면 출력
            if (response.requestHeaders) {
                console.log('=== 서버에서 보낸 요청 헤더 ===');
                console.log(response.requestHeaders);
            }
            
            if (response.responseHeaders) {
                console.log('=== API 서버 응답 헤더 ===');
                console.log(response.responseHeaders);
            }
            
            // HTTP 상태 코드별 상세 분석
            if (response.statusCode) {
                console.log('=== HTTP 상태 분석 ===');
                console.log('상태 코드:', response.statusCode);
                if (response.statusCode === 200 && !response.success) {
                    console.log('⚠️ HTTP 200이지만 API 레벨에서 에러 - 서비스키나 파라미터 문제일 가능성');
                }
            }
            
            if (response.success) {
                var message = 'API 연결 테스트가 성공했습니다.';
                if (response.statusCode) {
                    message += '\n상태 코드: ' + response.statusCode;
                }
                if (response.response) {
                    message += '\n\n응답 내용 (일부):\n' + response.response.substring(0, 300);
                }
                alert(message);
            } else {
                var errorMessage = 'API 연결 테스트에 실패했습니다.\n\n' + response.message;
                if (response.statusCode) {
                    errorMessage += '\n상태 코드: ' + response.statusCode;
                }
                if (response.errorCode) {
                    errorMessage += '\n에러 코드: ' + response.errorCode;
                }
                if (response.errorDetails) {
                    errorMessage += '\n\n상세 오류:\n' + response.errorDetails;
                }
                
                // 서비스 키 오류인 경우 추가 디버깅 정보
                if (response.errorCode === 'SERVICE_KEY_ERROR') {
                    console.log('=== 서비스 키 오류 디버깅 ===');
                    console.log('1. 브라우저에서 직접 접근해보세요:', directTestUrl);
                    console.log('2. 서비스 키 길이:', serviceKey.length);
                    console.log('3. 서비스 키 인코딩 상태 확인 필요');
                    console.log('4. 공공데이터포털에서 API 사용승인 상태 확인 필요');
                    errorMessage += '\n\n디버깅: 브라우저 콘솔에서 직접 테스트 URL을 확인하세요.';
                }
                
                alert(errorMessage);
            }
        },
        error: function(xhr, status, error) {
            console.log('=== API 응답 에러 ===');
            console.log('xhr:', xhr);
            console.log('status:', status);
            console.log('error:', error);
            console.log('xhr.responseText:', xhr.responseText);
            
            var errorMessage = 'API 연결 테스트 중 오류가 발생했습니다.\n\n';

            if (xhr.status === 0) {
                if (status === 'abort') {
                    errorMessage += '요청이 취소되었습니다.';
                } else {
                    errorMessage += '네트워크 연결을 확인해주세요.';
                }
            } else if (xhr.status === 404) {
                errorMessage += '테스트 엔드포인트를 찾을 수 없습니다.';
            } else if (xhr.status === 500) {
                errorMessage += '서버 내부 오류가 발생했습니다.';
            } else if (status === 'timeout') {
                errorMessage += '요청 시간이 초과되었습니다. API 응답이 느리거나 URL이 잘못되었을 수 있습니다.';
            } else {
                errorMessage += 'HTTP ' + xhr.status + ': ' + error;
            }

            // 서버에서 보낸 에러 메시지가 있으면 추가
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage += '\n\n서버 메시지: ' + xhr.responseJSON.message;
            }

            alert(errorMessage);
        },
        complete: function() {
            console.log('=== API 테스트 완료 ===');
            
            // 로딩 해제 및 중복 호출 방지 플래그 해제
            $testBtn.prop('disabled', false).text(originalText);
            isApiTesting = false;
            apiTestAbortController = null;
        }
    });
}
// 폼 유효성 검사
function validateForm() {
    var isValid = true;
    
    // 필수 필드 검사
    var requiredFields = [
        { id: 'apiName', name: 'API 명' },
        { id: 'baseUrl', name: '기본 URL' },
        { id: 'serviceKey', name: '서비스 키' },
        { id: 'targetTable', name: '대상 테이블명' }
    ];
    
    $.each(requiredFields, function(index, field) {
        var $field = $('#' + field.id);
        var $errorDiv = $('#' + field.id + 'Div');
        
        if (!$field.val().trim()) {
            $errorDiv.text(field.name + '을(를) 입력해주세요.');
            isValid = false;
        } else {
            $errorDiv.text('');
        }
    });
    
    // URL 형식 검사
    var urlPattern = /^https?:\/\/.+/;
    if ($('#baseUrl').val() && !urlPattern.test($('#baseUrl').val())) {
        $('#baseUrlDiv').text('올바른 URL 형식을 입력해주세요.');
        isValid = false;
    }
    
    // 스케줄 설정 검사
    if ($('#scheduleYn').is(':checked')) {
        if (!$('#scheduleType').val()) {
            alert('스케줄 타입을 선택해주세요.');
            isValid = false;
        } else if ($('#scheduleType').val() === 'CRON' && !$('#cronExpression').val().trim()) {
            alert('Cron 표현식을 입력해주세요.');
            isValid = false;
        } else if ($('#scheduleType').val() === 'INTERVAL' && !$('#intervalMinutes').val()) {
            alert('실행 간격을 입력해주세요.');
            isValid = false;
        }
    }
    
    // 매핑 데이터 검사
    var mappings = getMappingData();
    if (mappings.length === 0) {
        alert('최소 1개 이상의 컬럼 매핑을 설정해주세요.');
        isValid = false;
    }
    
    return isValid;
}