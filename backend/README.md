<p align="center">
   <img width="300" alt="2025-03-08_20-37-02" src="https://github.com/user-attachments/assets/bc22d291-ec07-44cd-8328-91818870bf06" />
</p>

<p align="center">
   중고 물품 경매 서비스, Palgona
</p>

## 프로젝트 소개
중고로 물품을 판매할 때 가격 측정에 어려움도 있고 한 사람이 여러 사람에게 동시에 구매 약속을 잡는 문제가 있었습니다.
저희 Palgona 서비스를 사용하면 중고 물품을 적정한 가격에 쉽게 경매를 통해 판매가 가능합니다.

## 시스템 아키텍처

<img width="1023" alt="2025-03-08_21-00-32" src="https://github.com/user-attachments/assets/2edf7c51-5a74-4d45-8f7e-d407d5e6e77c" />

## 기여한 부분

## 입찰 시스템 구현

### 입찰 플로우
<img width="1153" alt="2025-03-08_21-31-53" src="https://github.com/user-attachments/assets/50b88662-2edf-40cb-bba6-dcdb621bd614" />
동시 입찰을 막기 위해 Redis의 분산락을 활용했습니다. (부하 분산) <br>
입찰 시도가 많이 발생할텐데 DB 레코드 수준(SELECT FOR UPDATE)에서 락을 걸면 DB 부하가 크다고 생각했습니다.

### 유저 마일리지 갱신
<img width="691" alt="2025-03-08_22-09-26" src="https://github.com/user-attachments/assets/12e650b5-217f-461f-be60-c3149cf51294" />

유저 잔액을 갱신이 발생하는 트랜잭션은 총 6개. (입찰 포함) <br>
-> Pessimistic Lock을 활용하여 동시성 제어

### 현재 입찰가 반정규화 후 발생한 문제

1. 입찰 시간이 종료되기 직전에 입찰 시도 요청이 들어온다. <br>
2. 입찰 트랜잭션이 끝나기 전에 입찰 만료 시간이 지나고 입찰 만료 확인 트랜잭션(cron Job)이 시작된다. <br>
3. 마지막 입찰자의 입찰가 갱신이 분실된다. <br>
<p align="center">
<img width="752" alt="2025-03-08_21-36-10" src="https://github.com/user-attachments/assets/c2279968-93c3-4011-b02a-b40861813323" />
</p>

조회 성능 때문에 입찰가를 반정규화한 위와 같은 문제가 발생했습니다. <br>

이를 해결하기 위해 입찰 기간 만료 TX를 시작하기 전에도 분산락을 잡음으로써 Lost Update 문제를 해결할 수 있었습니다.
<p align="center">
<img width="728" alt="2025-03-08_21-38-28" src="https://github.com/user-attachments/assets/11a5cfe2-6938-4eae-927a-4152420c2194" />
</p>

### 기타
* 입찰가 반정규화 및 쿼리, 인덱스 튜닝으로 상품 조회 성능 개선
* Kakao Oauth 로그인 기능 구현
* Docker & GitHub Actions를 활용하여 CICD 파이프라인 구축

# 기술 스택
<div align="center">
  <h3> 기술 스택 </h3>
  <img src="https://img.shields.io/badge/Java17-000000?style=flat-square&logo=java&color=F40D12">
  <img src="https://img.shields.io/badge/Spring_Boot_3-0?style=flat-square&logo=spring-boot&logoColor=white&color=%236DB33F">
  <img src="https://img.shields.io/badge/MySQL_8-0?style=flat-square&logo=mysql&logoColor=white&color=4479A1">
  <img src="https://img.shields.io/badge/Hibernate-0?style=flat-square&logo=hibernate&logoColor=white&color=%2359666C">
  <br/>
  <img src="https://img.shields.io/badge/Amazon_EC2-0?style=flat-square&logo=amazon-ec2&logoColor=white&color=%23FF9900">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white">
  <br/>
  <img src="https://img.shields.io/badge/OAuth2-0?style=flat-square&logo=oauth2&logoColor=white&color=%23000000">
  <img src="https://img.shields.io/badge/Gradle-0?style=flat-square&logo=gradle&logoColor=white&color=%2302303A">
  <img src="https://img.shields.io/badge/JUnit5-0?style=JUnit5-square&logo=junit5&logoColor=white&color=%2325A162">
  <br/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white">
</div>
<br/>
<br/>

