// layout.js (급여명세서 관련 스크립트만 포함)

// 부서별 직급 옵션
const jobOptions = {
    D1: [
        {code: "J11", name: "부장"},
        {code: "J12", name: "차장"}
    ],
    D2: [
        {code: "J21", name: "부장"},
        {code: "J22", name: "차장"}
    ],
    D3: [
        {code: "J31", name: "부장"},
        {code: "J32", name: "차장"}
    ],
    D4: [
        {code: "J41", name: "부장"},
        {code: "J42", name: "차장"}
    ]
};

// 직급 select 갱신
function updateJobSelect(selectedJobCode = "") {
    const deptSelect = document.getElementById("deptCode");
    const jobSelect = document.getElementById("jobCode");
    if (!deptSelect || !jobSelect) return; // 다른 페이지에서는 그냥 무시

    const dept = deptSelect.value;
    jobSelect.innerHTML = "<option value=''>전체</option>";

    if (jobOptions[dept]) {
        jobOptions[dept].forEach(job => {
            let opt = document.createElement("option");
            opt.value = job.code;
            opt.text = job.name;
            jobSelect.appendChild(opt);
        });
    }

    if (window.selectedJobCode) {
        jobSelect.value = window.selectedJobCode;
    }
}

// 급여명세서 노션 전송
function sendPayroll() {
    const empNo = document.getElementById("empNo")?.value;
    const payMonth = document.getElementById("payMonth")?.value;

    if (!empNo || !payMonth) {
        alert("사원번호와 지급월을 모두 입력해주세요.");
        return;
    }

    const payDate = payMonth.substring(0, 7); // yyyy-MM 그대로 전달

    fetch(`/payroll/export-notion?empNo=${empNo}&payDate=${payDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("서버 응답 오류: " + response.status);
            }
            return response.json();

        })
        .then(result => {
            if (result.status === "success") {
                alert("노션 업로드가 완료되었습니다.");
                $("#printModal").modal("hide");
                $(".modal-backdrop").remove();
            } else {
                alert(result.message || "급여데이터가 없습니다.");
            }
        })
        .catch(error => {
            console.error(error);
            alert("서버 오류가 발생했습니다.");
        });
}

// 사원들 급여 검색
function searchPayroll() {
    $.ajax({
        url: "/finance/payroll/search",
        type: "get",
        data: {
            yearMonth: $("#yearMonth").val(),
            deptCode: $("#deptCode").val(),
            jobCode: $("#jobCode").val(),
            empName:$("#empName").val()
        },
        success: function (data) {
            var newContent = $(data).filter(".table-responsive");
            $(".table-responsive").replaceWith(newContent);
        }
    });
}

$(document).ready(function () {
    $("#empName").on("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();  // 폼 submit 막고
            searchPayroll();     // 검색 실행
        }
    });
});

    // Ajax 검색
function searchComponent() {
    $.ajax({
        url: "/finance/inventory/search",
        type: "get",
        data: {
            keyword: $("#keyword").val(),
            status: $("#status").val()
        },
        success: function (data) {
            $("#componentTable").html(data);
        }
    });
}

    // 엔터로 검색 가능
    $(document).ready(function () {
    $("#searchForm").on("submit", function(e) {
        e.preventDefault();
        searchComponent();
    });
});

    $(function () {
    // 서버에서 compList 가져오기 (AJAX)
    $.ajax({
        url: "/finance/getComponents",
        method: "GET",
        success: function(data) {
            let items = data.map(c => ({
                label: c.cpCode + " - " + c.cpName,
                value: c.cpCode
            }));

            $("#cpCode").autocomplete({
                source: items,
                minLength: 1,
                appendTo: "#printModal",
                select: function(event, ui) {
                    $("#cpCode").val(ui.item.value); // 선택 시 cpCode 값만 입력됨
                    return false;
                }
            });
        }
    });
});

function saveStock() {
    let formData = $("#stockForm").serialize();

    $.post("/finance/updateStock", formData, function(res) {
        if (res === "success") {
            alert("변경이 완료되었습니다!");
            location.reload();
        } else if (res === "not_enough") {
            alert("더이상 출고할 수 없습니다! (재고 부족)");
        } else {
            alert("DB 업데이트 실패! (응답: " + res + ")");
        }
    }).fail(function(xhr, status, error) {
        console.error("서버 오류:", error);
        alert("서버 오류가 발생했습니다.");
    });
}
    // 안전한 날짜 포맷팅 함수
    function formatSafeDate(dateValue) {
    if (!dateValue) return '날짜 정보 없음';

    try {
    let date;
    if (typeof dateValue === 'string') {
    dateValue = dateValue.replace(/\.0$/, ''); // .0 제거
    date = new Date(dateValue);
} else if (typeof dateValue === 'number') {
    date = new Date(dateValue);
} else {
    date = new Date(dateValue);
}

    // Invalid Date 체크
    if (isNaN(date.getTime())) {
    console.warn('Invalid date:', dateValue);
    return '날짜 형식 오류';
}

    return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    hour12: true
});
} catch (error) {
    console.error('Date formatting error:', error);
    return '날짜 처리 오류';
}
}

    // 안전한 값 체크 함수
    function safeValue(value, defaultValue = '정보 없음') {
    return (value !== null && value !== undefined && value !== '') ? value : defaultValue;
}

    // 모달 열기 전에 내용 초기화
    $('#inboxMessageModal').on('show.bs.modal', function(event){
    const modal = $(this);
    modal.find('.modal-body').html('<div class="text-center text-muted">로딩 중...</div>');
});


    // 버튼 클릭 시 모달 열기
    $(document).on('click', '.view-btn', function(e){
    e.stopPropagation();
    const msgId = $(this).data('msgid');

    if (!msgId) {
    alert('메시지 ID가 없습니다.');
    return;
}

    $('#inboxMessageModal').modal('show');

    $.ajax({
    url: '/message/read-ajax',
    method: 'GET',
    data: { msgId: msgId,
    receiverNo: CURRENT_USER_NO
},

    success: function(response) {
    console.log('AJAX Response:', response); // 디버깅용

    if (!response) {
    $('#inboxMessageContent').html(`
                        <div class="alert alert-danger">
                            <strong>오류!</strong> 서버에서 응답을 받지 못했습니다.
                        </div>
                    `);
    return;
}

    // 안전한 데이터 추출
    const title = safeValue(response.title, '제목 없음');
    const senderName = safeValue(response.senderName, '알 수 없는 발신자');
    const sendDate = formatSafeDate(response.sendDate);
    const content = safeValue(response.content, '내용이 없습니다.');

    let contentHtml = `
                    <div class="mb-3">
                        <strong>제목:</strong>
                        <span class="ml-2">${title}</span>
                    </div>
                    <div class="mb-3">
                        <strong>보낸 사람:</strong>
                        <span class="ml-2">${senderName}</span>
                    </div>
                    <div class="mb-3">
                        <strong>발송일시:</strong>
                        <span class="ml-2">${sendDate}</span>
                    </div>
                    <div class="mb-3">
                        <strong>내용:</strong>
                        <div class="mt-2 p-3" style="background-color: #f8f9fa; border-radius: 5px; min-height: 100px; white-space: pre-wrap;">${content.replace(/\n/g, '<br>')}</div>
                    </div>
                `;

    $('#inboxMessageContent').html(contentHtml);
},
    error: function(xhr, status, error) {
    console.error('AJAX Error:', {xhr, status, error});

    let errorMessage = '쪽지를 불러오는 중 오류가 발생했습니다.';

    if (status === 'timeout') {
    errorMessage = '서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.';
} else if (xhr.status === 404) {
    errorMessage = '요청한 쪽지를 찾을 수 없습니다.';
} else if (xhr.status === 500) {
    errorMessage = '서버 내부 오류가 발생했습니다.';
}

    $('#inboxMessageContent').html(`
                    <div class="alert alert-danger">
                        <strong>오류!</strong> ${errorMessage}
                        <br><small>오류 코드: ${xhr.status} ${status}</small>
                    </div>
                `);
}
});
});

function formatKoreanDate(dateStr) {
    dateStr = dateStr.replace(/\.0$/, '');

    const date = new Date(dateStr);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'long',   // "9월"
        day: 'numeric',  // "7일"
        hour: 'numeric',
        minute: 'numeric',
        hour12: true     // 오전/오후 표시
    });
}

// 발송 메시지 상세보기
function viewSentMessage(msgId) {
    $.ajax({
        url: '/message/sent-detail',
        method: 'GET',
        data: { msgId: msgId },
        success: function(response) {
            console.log(response);
            let content = `
                <div class="mb-3">
                    <strong>제목:</strong> ${response.title}
                </div>
                <div class="mb-3">
                    <strong>발송일시:</strong> ${formatKoreanDate(response.sendDate)}
                </div>
                <div class="mb-3">
                    <strong>수신자:</strong>
                    <div class="mt-2">
            `;

            response.receivers.forEach(function(receiver) {
                content += `
                        ${receiver.empName} (${receiver.empNo})
                    </span>
                `;
            });

            content += `
                    </div>
                </div>
                <div class="mb-3">
                    <strong>내용:</strong>
                    <div class="mt-2 p-3" style="background-color: #f8f9fa; border-radius: 5px;">
                        ${response.content.replace(/\n/g, '<br>')}
                    </div>
                </div>
            `;

            $('#sentMessageContent').html(content);
            $('#sentMessageModal').modal('show');
        },
        error: function() {
            alert('메시지를 불러오는 중 오류가 발생했습니다.');
        }
    });
}

let selectedReceivers = []; // 선택된 수신자 목록
let searchTimeout; // 검색 지연을 위한 타이머

$(document).ready(function() {
    // 제목 글자 수 카운트
    $('#title').on('input', function() {
        const length = $(this).val().length;
        $('#titleCount').text(length + '/100');
    });

    // 내용 글자 수 카운트
    $('#content').on('input', function() {
        const length = $(this).val().length;
        $('#contentCount').text(length + '/1000');
    });

    // 수신자 검색
    $('#receiverSearch').on('input', function() {
        const keyword = $(this).val().trim();

        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        if (keyword.length >= 2) {
            searchTimeout = setTimeout(() => {
                searchEmployees(keyword);
            }, 300);
        } else {
            $('#employeeList').hide().empty();
        }
    });

    // 검색창 외부 클릭 시 목록 숨기기
    $(document).on('click', function(e) {
        if (!$(e.target).closest('#receiverSearch, #employeeList').length) {
            $('#employeeList').hide();
        }
    });
});

$("#receiverSearch").autocomplete({
    source: function(request, response) {
        $.ajax({
            url: "/message/search-employees",
            method: "GET",
            data: { keyword: request.term },
            success: function(data) {
                console.log("검색 결과:", data);
                response($.map(data, function(emp) {
                    return {
                        label: emp.empName + " (" + emp.empNo + ") - " + (emp.deptName || "") + " " + (emp.jobName || ""),
                        value: emp.empName, // 입력창에 채워질 값
                        empNo: emp.empNo,
                        empName: emp.empName,
                        deptName: emp.deptName,
                        jobName: emp.jobName
                    };
                }));
            },
            error: function() {
                console.error("자동완성 검색 실패");
            }
        });
    },
    minLength: 2, // 2글자 이상 입력 시 실행
    select: function(event, ui) {
        // 수신자 추가
        addReceiver(ui.item.empNo, ui.item.empName, ui.item.deptName, ui.item.jobName);
        $(this).val(""); // 선택 후 입력창 비우기
        return false;
    }
});

// 사원 검색 함수
function searchEmployees(keyword) {
    $.ajax({
        url: '/message/search-employees',
        method: 'GET',
        data: { keyword: keyword },
        success: function(employees) {
            displayEmployeeList(employees);
        },
        error: function() {
            console.error('사원 검색 중 오류 발생');
        }
    });
}

// 검색 결과 표시
function displayEmployeeList(employees) {
    const $list = $('#employeeList');
    $list.empty();

    if (employees.length === 0) {
        $list.append('<div class="employee-item text-muted">검색 결과가 없습니다.</div>');
    } else {
        employees.forEach(function(emp) {
            // 이미 선택된 사원은 제외
            if (!selectedReceivers.some(r => r.empNo === emp.empNo)) {
                $list.append(`
                      <div class="employee-item"
                           onclick='addReceiver(${JSON.stringify(emp.empNo)}, ${JSON.stringify(emp.empName)}, ${JSON.stringify(emp.deptName || "")}, ${JSON.stringify(emp.jobName || "")})'>
                          <strong>${emp.empName}</strong> (${emp.empNo})
                          <br><small class="text-muted">${emp.deptName || ''} ${emp.jobName || ''}</small>
                      </div>
                    `);
            }
        });
    }

    $list.show();
}

// 수신자 추가
function addReceiver(empNo, empName, deptName, jobName) {
    const receiver = {
        empNo: empNo,
        empName: empName,
        deptName: deptName,
        jobName: jobName
    };

    selectedReceivers.push(receiver);
    updateReceiverDisplay();

    $('#receiverSearch').val('');
    $('#employeeList').hide();
}

// 수신자 표시 업데이트
function updateReceiverDisplay() {
    const $container = $('#selectedReceivers');
    $container.empty();

    selectedReceivers.forEach(function(receiver, index) {
        $container.append(`
            <span class="receiver-tag">
                ${receiver.empName} (${receiver.empNo})
                <span class="remove-btn" onclick="removeReceiver(${index})">&times;</span>
            </span>
        `);
    });
}

// 수신자 제거
function removeReceiver(index) {
    selectedReceivers.splice(index, 1);
    updateReceiverDisplay();
}

// 폼 초기화
function resetForm() {
    if (confirm('작성 중인 내용이 모두 삭제됩니다. 계속하시겠습니까?')) {
        $('#composeForm')[0].reset();
        selectedReceivers = [];
        updateReceiverDisplay();
        $('#titleCount').text('0/100');
        $('#contentCount').text('0/1000');
    }
}

// 메시지 전송
function sendMessage() {
    const title = $('#title').val().trim();
    const content = $('#content').val().trim();

    // 유효성 검사
    if (selectedReceivers.length === 0) {
        alert('수신자를 선택해주세요.');
        return;
    }

    if (!title) {
        alert('제목을 입력해주세요.');
        $('#title').focus();
        return;
    }

    if (!content) {
        alert('내용을 입력해주세요.');
        $('#content').focus();
        return;
    }

    const messageData = {
        senderNo: $('#senderNo').val(), // 임시로 설정된 값
        receivers: selectedReceivers.map(r => r.empNo),
        title: title,
        content: content
    };

    // 전송 버튼 비활성화
    const $sendBtn = $('button[onclick="sendMessage()"]');
    $sendBtn.prop('disabled', true).text('전송 중...');

    $.ajax({
        url: '/message/send',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(messageData),
        success: function(response) {
            if (response.success) {
                alert(response.message);
                location.href = '/message/inbox';
            } else {
                alert('전송 실패: ' + response.message);
            }
        },
        error: function() {
            alert('시스템 오류가 발생했습니다. 다시 시도해주세요.');
        },
        complete: function() {
            $sendBtn.prop('disabled', false).html('<span class="material-symbols-outlined" style="font-size: 16px;">send</span> 전송');
        }
    });
}



