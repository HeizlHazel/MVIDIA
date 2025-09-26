// 전역 변수 설정
const csrfToken = $('input[name="_csrf"]').val();
let currentMsgId = null;


// 근무 상태에 따라 버튼을 업데이트하는 함수
function checkWorkStatus() {
    $.get('/work/status', function(data) {
        const sessionBox = $('.session-box');
        const extendForm = sessionBox.find('#extendForm');

        // 기존의 출근/퇴근 버튼 제거
        sessionBox.find('.work-btn').remove();

        if (data.hasCheckedIn) {
            // 출근 상태인 경우, 퇴근 버튼을 추가
            $(`<form class="work-btn" id="checkoutForm"><button class="mini-btn checkout-btn" type="button">퇴근</button></form>`).insertAfter(extendForm);
        } else {
            // 출근하지 않은 상태인 경우, 출근 버튼을 추가
            $(`<form class="work-btn" id="checkinForm"><button class="mini-btn checkin-btn" type="button">출근</button></form>`).insertAfter(extendForm);
        }
    }).fail(function() {
        console.error('근무 상태를 불러오는 데 실패했습니다.');
    });
}


// 문서 로드 후 실행
$(document).ready(function() {
    checkWorkStatus();
    fetchUnreadCounts();
    setInterval(fetchUnreadCounts, 30000); // 30초마다 갱신

    // 이벤트 리스너


    // 출근 버튼 클릭 (동적으로 추가된 요소)
    $(document).on('click', '.checkin-btn', function() {
        $.post('/work/checkin', { _csrf: csrfToken })
            .done(function(response) {
                if (response.isLate) {
                    alertify.alert('지각 처리되었습니다.');
                } else {
                    alertify.alert('정상적으로 출근 처리되었습니다.');
                }
                checkWorkStatus(); // 출근 버튼을 퇴근 버튼으로 변경
            })
            .fail(function() {
                alertify.error('출근 처리에 실패했습니다.');
            });
    });

    // 퇴근 버튼 클릭 (동적으로 추가된 요소)
    $(document).on('click', '.checkout-btn', function() {
        $.post('/work/checkout', { _csrf: csrfToken })
            .done(function(response) {
                if (response.isWorkHourInsufficient) {
                    alertify.confirm('총 근무시간이 8시간을 넘어야 정상 출근 처리됩니다. 지금 퇴근하시겠습니까?<br>현재 근무시간: ' + response.workDuration,
                        function() { // 확인(OK)을 누르면
                            $.post('/work/checkout/confirm', { _csrf: csrfToken })
                                .done(function() {
                                    alertify.alert('퇴근 처리되었습니다.');
                                    checkWorkStatus(); // 퇴근 버튼을 출근 버튼으로 변경
                                });
                        },
                        function() { // 취소(Cancel)를 누르면
                            alertify.error('퇴근을 취소했습니다.');
                        }
                    );
                } else {
                    alertify.alert('정상적으로 퇴근 처리되었습니다.');
                    checkWorkStatus(); // 퇴근 버튼을 출근 버튼으로 변경
                }
            })
            .fail(function() {
                alertify.error('퇴근 처리에 실패했습니다.');
            });
    });
});



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

    $('#loadingModal').modal('show');

    fetch(`/payroll/export-notion?empNo=${empNo}&payDate=${payDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("서버 응답 오류: " + response.status);
            }
            return response.json();

        })
        .then(result => {
            $('#loadingModal').modal('hide');

            if (result.status === "success") {
                // alert("노션 업로드가 완료되었습니다.");
                $("#printModal").modal("hide");
                $(".modal-backdrop").remove();

                window.location.href = `/finance/salary-pdf-notion?empNo=${empNo}&yearMonth=${payDate}`;
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

// 재고 입출고하기
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

// 중요도 토글 함수
function toggleImportance(event, button) {
    event.stopPropagation(); // 행 클릭 이벤트 방지

    const msgId = button.getAttribute('data-msgid');
    const currentStatus = button.getAttribute('data-current-status');
    const newStatus = (currentStatus === 'Z') ? 'J' : 'Z';

    // 서버에 중요도 업데이트 요청
    $.ajax({
        url: '/message/update-importance',
        method: 'POST',
        data: {
            msgId: msgId,
            receiverNo: CURRENT_USER_NO,
            importYn: newStatus
        },
        success: function(response) {
            if (response.success) {
                // UI 업데이트
                button.setAttribute('data-current-status', newStatus);

                if (newStatus === 'Z') {
                    button.classList.remove('star-normal');
                    button.classList.add('star-important');
                } else {
                    button.classList.remove('star-important');
                    button.classList.add('star-normal');
                }

                // 성공 메시지는 표시하지 않음 (사용자 경험을 위해)
            } else {
                alert('중요도 변경에 실패했습니다: ' + (response.message || '알 수 없는 오류'));
            }
        },
        error: function(xhr, status, error) {
            console.error('중요도 업데이트 오류:', {xhr, status, error});
            alert('중요도 변경 중 오류가 발생했습니다.');
        }
    });
}
// 날짜 포맷팅 함수
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

// 메시지 상세 보기 모달이 열릴 때
$('#inboxMessageModal').on('show.bs.modal', function(event) {
    const button = $(event.relatedTarget);
    currentMsgId = button.data('msgid'); // 전역 변수에 msgId 저장

    // 삭제 버튼 클릭 이벤트 설정
    $('#deleteMessageBtn').off('click').on('click', function() {
        deleteMessage(currentMsgId);
    });

    // 모달 열기 전에 내용 초기화
    $('#inboxMessageModal').on('show.bs.modal', function(event){
        const modal = $(this);
        modal.find('.modal-body').html('<div class="text-center text-muted">로딩 중...</div>');
        $('#deleteMessageBtn').hide();
    });
});


// 버튼 클릭 시 모달 열기
$(document).on('click', '.view-btn', function(e){
    e.preventDefault();
    e.stopPropagation();

    const msgId = $(this).data('msgid');
    currentMsgId = msgId;
    console.log("현재 선택된 msgId:", currentMsgId);

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

            $(`tr[data-msgid="${msgId}"] td:nth-child(4)`).text('읽음');
            $(`tr[data-msgid="${msgId}"]`).removeClass('unread-row');
            fetchUnreadCounts(); // 상단 안읽은 쪽지 개수도 새로고침

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

// 모달 완전히 닫힐 때 정리 작업
$('#inboxMessageModal').on('hidden.bs.modal', function () {
    currentMsgId = null;
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

//  쪽지보내기 - 사원 찾기
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
    minLength: 1, // 1글자 이상 입력 시 실행
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

// 초기화
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

    if (!content || content.trim() === '') {
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
                location.href = '/message/outbox';
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

// 쪽지 삭제
function deleteMessage(msgId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: '/message/delete/inbox',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            msgId: msgId,
            receiverNo: CURRENT_USER_NO   // 세션에서 로그인한 사번
        }),
        success: function(response) {
            if (response.success) {
                alert('쪽지가 삭제되었습니다.');
                $('#inboxMessageModal').modal('hide');
                // 목록 갱신
                location.reload();
            } else {
                alert('삭제 실패: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error("삭제 요청 오류:", {xhr, status, error});
            alert('서버 오류로 삭제에 실패했습니다.');
        }
    });
}

// 발신 쪽지 삭제
function deleteSentMessage(msgId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    $.ajax({
        url: '/message/delete/outbox',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ msgId: msgId }),
        success: function(response) {
            if (response.success) {
                alert(response.message);
                $('#sentMessageModal').modal('hide');
                location.reload();
            } else {
                alert('삭제 실패: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error("발신 삭제 요청 오류:", {xhr, status, error});
            alert('서버 오류로 발신 삭제에 실패했습니다.');
        }
    });
}

function checkParams(btn) {
    const empNo = btn.getAttribute("data-empno");
    const payDate = btn.getAttribute("data-paydate");
    console.log("empNo:", empNo, "payDate:", payDate);

    // yearMonth 잘라서 확인
    const yearMonth = payDate ? payDate.substring(0,7) : "";
    console.log("yearMonth:", yearMonth);

    // 실제 이동할 URL
    const url = `/finance/salary-pdf-notion?empNo=${empNo}&yearMonth=${yearMonth}`;
    console.log("Request URL:", url);
}


$(document).on("click", ".upload-btn", function() {
    const empNo = $(this).data("empno");
    const payDate = $(this).data("paydate");

    uploadToNotion(empNo, payDate);
});

function uploadToNotion(button) {
    const empNo = button.getAttribute("data-empno");
    const payDate = button.getAttribute("data-paydate");

    // 로딩 모달 표시
    $('#loadingModal').modal('show');

    $.ajax({
        url: "/payroll/export-notion",
        method: "GET",
        data: { empNo, payDate },
        success: function (res) {
            // 로딩 모달 닫기
            $('#loadingModal').modal('hide');

            if (res.status === "success") {
                // ✅ 부트스트랩 Alert (알림창)
                $('<div class="alert alert-success alert-dismissible fade show" role="alert">' +
                    '<strong>✅ 완료!</strong> 급여명세서가 노션에 업로드되었습니다.' +
                    '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span></button></div>')
                    .appendTo(".innerOuter").delay(3000).fadeOut(500, function () { $(this).remove(); });
            } else {
                $('<div class="alert alert-warning alert-dismissible fade show" role="alert">' +
                    '⚠️ ' + (res.message || "업로드 실패") +
                    '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
                    '<span aria-hidden="true">&times;</span></button></div>')
                    .appendTo(".innerOuter").delay(3000).fadeOut(500, function () { $(this).remove(); });
            }
        },
        error: function () {
            $('#loadingModal').modal('hide');
            $('<div class="alert alert-danger alert-dismissible fade show" role="alert">' +
                '❌ 업로드 중 오류가 발생했습니다.' +
                '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
                '<span aria-hidden="true">&times;</span></button></div>')
                .appendTo(".innerOuter").delay(3000).fadeOut(500, function () { $(this).remove(); });
        }
    });
}