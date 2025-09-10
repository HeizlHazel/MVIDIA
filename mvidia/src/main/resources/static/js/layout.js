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

    const payDate = payMonth; // yyyy-MM 그대로 전달

    fetch(`/payroll/export-notion?empNo=${empNo}&payDate=${payDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("서버 응답 오류: " + response.status);
            }
            return response.text();
        })
        .then(result => {
            if (result === "success") {
                alert("출력이 완료되었습니다.");
                $("#printModal").modal("hide");
            } else if (result === "fail") {
                alert("급여자료가 없습니다.");
            } else {
                alert("알 수 없는 응답: " + result);
            }
        })
        .catch(error => {
            console.error(error);
            alert("서버 오류가 발생했습니다.");
        });
}
