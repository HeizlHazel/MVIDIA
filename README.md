<img width="300" height="300" alt="b32b62ffe11d899d" src="https://github.com/user-attachments/assets/b799cfb7-6780-485c-8ed0-5841d39c9d62" />

## 📌 프로젝트 소개
### ERP 시스템 - MVIDIA 구축
_외부 공용협업툴과 실시간 연동되는 사내 ERP 시스템 만들기_   
_(인적자원관리, 재무관리, 품질보증관리)_   
   
   

<br>

기업의 핵심 부문인 인사, 품질, 재무 관리 업무는 부서별로 개별 시스템이나 수작업으로 처리되는 경우가 많아, 정보의 단절과 중복 관리로 인해 업무 효율성이 저하되는 문제가 발생함.

본 프로젝트는 이러한 문제를 해소하기 위해 인사·품질·재무 관리 기능을 하나의 ERP 시스템 안에 통합하고, 부서별 필수 기능을 신속하게 확인하고 처리할 수 있는 환경을 마련하고자 함.


ERP 내에서는 각 부서가 요구하는 기능과 메뉴를 맞춤형으로 제공하여 업무 접근성을 높이며, 자료의 통합 관리와 검색 편의성을 강화하게 함.

사용자 화면은 부서별로 차별화된 메뉴 구성을 적용하여 실무 효율성을 극대화하고, 관리자 화면에서는 계정 및 권한 관리, 부서 정보 관리 등의 기능을 제공함으로써 시스템 운영을 보다 체계적이고 안정적으로 가능하게 함.


이를 통해 기업은 업무 처리 속도의 향상, 부서 간 협업 강화, 데이터 신뢰성 제고, 관리 비용 절감 등 다양한 효과를 기대할 수 있음.


<br>

## 📆 개발 기간
- 2025-07-29(화) ~ 2025-08-04(월) : 주제, 프로젝트명, 팀장 선정
- 2025-08-04(월) ~ 2025-08-11(월) : 기획의도, 유사사이트 분석, 클라이언트 요구사항분석, UseCase Diagram 작성
- 2025-08-11(월) ~ 2025-08-18(월) : 사이트맵, 화면설계 회의, 메인페이지 화면설계
- 2025-08-18(월) ~ 2025-08-22(금) : DB 테이블 설계, 샘플데이터, ERD 작성
- 2025-08-22(금) ~ 2025-08-28(목) : 화면 JSP 생성, 기능구현, JDBC 오라클연동 후 CRUD 최소 1개
- 2025-08-28(목) ~ 2025-09-21(일) : 프로젝트 구현 및 디버깅
- 2025-09-22(월) ~ 2025-09-24(수) : 베타 테스트, 최종 발표자료 준비
- 2025-09-25(금) : 프로젝트 발표
<img width="1096" height="511" alt="image" src="https://github.com/user-attachments/assets/ecd48314-35fc-44d0-bee8-2f4c59b95217" />

<br>
<br>

## 👥 구성원 및 역할

### 💬 조장 어수용
+ 로그인/로그아웃 - CRUD 사용하여 login한 사용자 정보 session에 저장
+ 출근/퇴근/출퇴근 기록 조회/ 근태 관리 - ajax, CRUD 사용하여 근태 기록 조회 및 변경
+ 마이페이지 - CRUD 사용하여 session에 저장된 사용자의 정보 조회 및 수정 요청
+ 휴가 신청/ 사용내역 조회/ 휴가 관리 - ajax, CRUD 사용하여 휴가 신청, 사용 내역 조회 및 휴가 승인,반려 처리
+ 일정 조회 - google calendar API 사용하여 일정 확인
+ 사원 계정 생성/수정/삭제 - CRUD 사용하여 사원 계정 생성 수정 및 삭제
+ 인사 통합 관리 : ajax, CRUD 사용하여 각 부서별 인사 정보 확인 가능
+ 증명서 발급 - Notion API 사용하여 재직증명서, 경력증명서 발급 및 인쇄 가능
+ 조직도 - ajax, CRUD 사용하여 조직도 조회 및 해당 사원 정보 조회 가능


### 💭 조원 김혜지
- 실시간 날씨 정보 - OpenWeatherMap API 연동으로 근무지 날씨 표시
- 부서별 맞춤 대시보드 - 권한에 따른 차별화된 정보 제공
- 빠른 작업 바로가기 - 주요 업무 기능 원클릭 접근
- 전자 결재 신청 - Notion API 연동을 통한 문서 작성 및 제출
- 내 문서함 - 신청한 결재 문서 CRUD(문서 삭제 시 Notion 아카이브 기능 활용)
- 승인함 - 결재 문서 승인/반려 처리
- 계정 권한 관리 - 사용자별 권한 부여 및 회수
- 권한 변경 로그 - 권한 변경 이력 추적 및 감사
- 전자결재 처리 로그 - 전자결재 처리 이력 추적 및 관리



### 🗯 조원 박현아



### 🗨 조원 홍세민  

<br>

## ⚙ 개발 환경
- OS : Windows10
- Developer Tools : IntelliJ / VS Code / SqlDeveloper
- Server : Apache Tomcat 9.0
- DBMS : Oracle
- Management : Git, GitHub

<br>

## 🔧 사용 기술
- Backend : JAVA, Spring Boot, MyBatis 
- Frontend : HTML5, CSS3, JavaScript, jQuery, Bootstrap, Thymeleaf

<br>

## 💡 라이브러리
- alertify.js
- chart.js
  
<br>

## 🔌 외부 API 연동
- Notion API
- google calendar API
- OpenWeatherMap API

<br>

## 🧩 설계
ERD 설계: [ERD Cloud](https://www.erdcloud.com/d/63X6sALm9KCksFYod)

<br>

## 💻 프로젝트 구현
### 💬 조장 어수용

+ 로그인 로그아웃
![로그인로그아웃](https://github.com/lthorl/mvidia/blob/main/%EB%A1%9C%EA%B7%B8%EC%9D%B8%EB%A1%9C%EA%B7%B8%EC%95%84%EC%9B%83.gif)


+ 출근,퇴근,출퇴근 기록조회, 사원 근태 정보 변경
![출근퇴근](https://github.com/lthorl/mvidia/blob/main/%EC%B6%9C%EA%B7%BC%ED%87%B4%EA%B7%BC.gif)


+ 마이페이지 개인정보 조회, 개인정보 수정 요청
![마이페이지](https://github.com/lthorl/mvidia/blob/main/%EB%A7%88%EC%9D%B4%ED%8E%98%EC%9D%B4%EC%A7%80.gif)

+사원 계정 수정



+ 휴가 신청 및 휴가 신청 목록, 휴가 사용 내역
![휴가 신청 및 승인](https://github.com/lthorl/mvidia/blob/main/%ED%9C%B4%EA%B0%80%20%EC%8B%A0%EC%B2%AD%20%EB%B0%8F%20%EC%8A%B9%EC%9D%B8.gif)

+ 인사 통합 관리


+ 일정 조회(google calendar API)
![캘린더](https://github.com/lthorl/mvidia/blob/main/%EC%BA%98%EB%A6%B0%EB%8D%94.gif)


+ 사원 계정 생성
![사원 계정 생성](https://github.com/lthorl/mvidia/blob/main/%EC%82%AC%EC%9B%90%20%EA%B3%84%EC%A0%95%20%EC%83%9D%EC%84%B1.gif)


+ 증명서 발급
![증명서 조회발급](https://github.com/lthorl/mvidia/blob/main/%EC%A6%9D%EB%AA%85%EC%84%9C%20%EC%A1%B0%ED%9A%8C%EB%B0%9C%EA%B8%89.gif)


+ 조직도 정보 조회
![조직도 정보 조회](https://github.com/lthorl/mvidia/blob/main/%EC%A1%B0%EC%A7%81%EB%8F%84%20%EC%A0%95%EB%B3%B4%20%EC%A1%B0%ED%9A%8C.gif)


### 💭 조원 김혜지

- 부서별 맞춤 대시보드, 날씨 API, 빠른 작업 바로가기
![메인 날씨 API, 빠른 작업](https://github.com/user-attachments/assets/7dca5d26-dd23-4141-aa85-fc07deaef7b3)

- 전자결재 신청
![전자결재 신청-문서함 이동](https://github.com/user-attachments/assets/1fb79b66-3a8b-4eb7-bc58-96a4639e6602)

- 내 문서함
![내 문서함](https://github.com/user-attachments/assets/eda585be-ae51-430b-93e8-92bd99426870)

- 노션 전자결재 문서함
![노션 전자결재 문서함](https://github.com/user-attachments/assets/f350ea9a-58b0-4661-aed9-e1a305ab4fed)

- 노션 전자결재 아카이빙
![노션 전자결재 아카이빙](https://github.com/user-attachments/assets/1ff03720-f37b-4f73-ab39-ad39f71c3053)

- 결재 승인함
![결재 승인함 탭](https://github.com/user-attachments/assets/53918952-4beb-446a-9e59-122c456c779d)

- 결재 승인
![전자결재 승인](https://github.com/user-attachments/assets/1b29b95a-89e8-4803-91ef-bd87c268fd43)

- 전자결재 처리 로그
![전자결재 처리 로그](https://github.com/user-attachments/assets/ab878dc4-d261-420e-8b30-c1f26ba13828)

- 계정 권한 관리
![계정 권한 관리](https://github.com/user-attachments/assets/43174ecc-859b-4e86-8db0-c85b06409d85)

- 권한 변경 로그
![권한 변경 로그](https://github.com/user-attachments/assets/f40f67ca-f309-43d7-a375-446d83044c4d)


### 🗯 조원 박현아


### 🗨 조원 홍세민


<br>

## 📑 최종 보고서
[2조(codescape)_최종보고서.pdf](https://github.com/user-attachments/files/22552199/2.codescape._.pdf)
