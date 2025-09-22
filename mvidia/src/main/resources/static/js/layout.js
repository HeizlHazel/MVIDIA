// 전역 변수 설정
const csrfToken = $('input[name="_csrf"]').val();
let countdownInterval;

// 세션 카운트다운 타이머 시작 함수
function startSessionCountdown() {
    const sessionCountdownElement = $('#sessionCountdown');
    let sessionSeconds = parseInt(sessionCountdownElement.data('seconds'));

    clearInterval(countdownInterval); // 기존 타이머가 있으면 초기화
    countdownInterval = setInterval(() => {
        if (sessionSeconds > 0) {
            sessionSeconds--;
            const minutes = Math.floor(sessionSeconds / 60);
            const seconds = sessionSeconds % 60;
            const formattedTime = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
            sessionCountdownElement.text(formattedTime);
        } else {
            clearInterval(countdownInterval);
            alertify.alert('세션이 만료되어 로그아웃됩니다.', function(){
                $('#logoutForm').submit();
            });
        }
    }, 1000);
}

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

// 메시지 및 알림 개수를 주기적으로 가져와 업데이트하는 함수
function fetchUnreadCounts() {
    $.get('/messages/unread/count', function(data) {
        $('#messageCount').text(data.messageCount);
    }).fail(function() {
        console.error('메시지 개수를 불러오는 데 실패했습니다.');
    });

    $.get('/notifications/unread/count', function(data) {
        $('#notificationCount').text(data.notificationCount);
    }).fail(function() {
        console.error('알림 개수를 불러오는 데 실패했습니다.');
    });
}

// 문서 로드 후 실행
$(document).ready(function() {
    startSessionCountdown();
    checkWorkStatus();
    fetchUnreadCounts();
    setInterval(fetchUnreadCounts, 30000); // 30초마다 갱신

    // 이벤트 리스너

    // 세션 연장 버튼 클릭
    $('.extend-btn').on('click', function(e) {
        e.preventDefault();
        $.post('/session/extend', { _csrf: csrfToken })
            .done(function() {
                const newSeconds = 3600; // 60분으로 재설정
                $('#sessionCountdown').data('seconds', newSeconds);
                startSessionCountdown();
                alertify.success('세션이 60분 연장되었습니다.');
            })
            .fail(function() {
                alertify.error('세션 연장에 실패했습니다. 다시 로그인해주세요.');
            });
    });

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

    // 메시지 아이콘 클릭 시 모달창 열기
    $('#messageIcon').on('click', function() {
        $.get('/messages/unread/list', function(messages) {
            let modalBody = '<h5>새로운 메시지</h5><ul>';
            if (messages && messages.length > 0) {
                messages.forEach(msg => {
                    modalBody += `<li><b>${msg.senderName}</b>: ${msg.subject}</li>`;
                });
            } else {
                modalBody += '<li>새로운 메시지가 없습니다.</li>';
            }
            modalBody += '</ul>';
            alertify.alert('MVIDIA ALERT', modalBody);
        }).fail(function() {
            alertify.error('메시지 목록을 불러오는 데 실패했습니다.');
        });
    });

    // 알림 아이콘 클릭 시 모달창 열기
    $('#notificationIcon').on('click', function() {
        $.get('/notifications/unread/list', function(notifications) {
            let modalBody = '<h5>새로운 알림</h5><ul>';
            if (notifications && notifications.length > 0) {
                notifications.forEach(notif => {
                    modalBody += `<li>${notif.content}</li>`;
                });
            } else {
                modalBody += '<li>새로운 알림이 없습니다.</li>';
            }
            modalBody += '</ul>';
            alertify.alert('MVIDIA ALERT', modalBody);
        }).fail(function() {
            alertify.error('알림 목록을 불러오는 데 실패했습니다.');
        });
    });
});