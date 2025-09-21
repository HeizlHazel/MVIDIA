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







