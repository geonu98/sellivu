# Sellivu

서로 다른 형식의 정산 파일을 내부 표준 구조로 정규화한 뒤, 주문 정산·수수료 상세·일별 정산 데이터를 교차 검증하고 차이를 분석할 수 있도록 만든 정산 분석 서비스입니다.

## Overview

Sellivu는 스마트스토어 판매자가 업로드한 정산 파일을 분석해 정산금액, 수수료, 실수령액 차이를 확인할 수 있도록 만든 서비스입니다.

실무에서는 주문 정산, 수수료 상세, 일별 정산 파일을 직접 맞춰 보며 정산 오류를 확인하는 경우가 많지만, 파일 형식과 컬럼 구성이 제각각이라 비교 자체가 어렵고 수작업 부담도 큽니다. Sellivu는 파일을 그대로 보여주는 대신, 내부 표준 구조로 정규화한 뒤 비교 가능한 단위로 재구성해 정산 결과를 검증하도록 설계했습니다.

## Tech Stack

- Backend: Spring Boot, Spring Security, JPA, PostgreSQL
- Frontend: React, TypeScript, Vite
- Infra / Tools: Docker, GitHub Actions, Vercel, Render

## Key Features

- CSV / Excel 정산 파일 업로드
- 헤더 정규화 기반 파일 유형 판별
- 주문 정산 / 수수료 상세 / 일별 정산 표준 구조 변환
- `productOrderNo` 우선 / `orderNo` 보조 규칙의 joinKey 기반 병합
- snapshot 단위 재구성 후 정산금액, 수수료, 실수령액 비교
- 일별 정산 cross-check
- `active-run` 기반 현재 분석 결과 관리
- saved analysis / workspace 분리

## Core Design

### 1. saved analysis / workspace 분리
초기에는 저장본과 현재 작업 상태가 섞여 restore만 해도 dirty 상태가 되거나 불필요한 저장 경고가 발생하는 문제가 있었습니다. 이를 해결하기 위해 saved analysis와 workspace의 역할을 분리하고, restore 직후는 clean, 실제 수정 시에만 dirty가 되도록 상태 모델을 재설계했습니다.

### 2. active-run 기반 분석 구조
현재 화면이 어떤 분석 결과를 보고 있는지 추적하기 어려운 문제를 해결하기 위해 `settlement_analysis_run`과 `active_run_id` 기반 구조로 전환했습니다. raw / snapshot / summary를 run 단위로 분리해 현재 활성 분석 결과를 명시적으로 관리하도록 했습니다.

### 3. 헤더 정규화와 타입 기반 검증
외부 파일 형식 차이 때문에 분석이 깨지지 않도록 헤더 정규화 레이어를 두고 파일 유형 판별과 필수 필드 검증을 분리했습니다. 이후 숫자와 날짜를 명시적으로 파싱해 문자열 비교가 아닌 타입 기반 분석이 가능하도록 만들었습니다.

### 4. joinKey 기반 병합
주문 정산과 수수료 상세를 같은 거래 단위로 비교하기 위해 joinKey를 도입했습니다.

- `productOrderNo`가 있으면 `P:` + productOrderNo
- 없으면 `O:` + orderNo

이를 통해 플랫폼 데이터가 완벽하지 않은 상황에서도 비교를 최대한 이어가고, 식별자가 없는 데이터는 별도 문제로 분리할 수 있도록 했습니다.

## Performance Optimization

성능 최적화는 더미 CSV와 `[PERF]` 로그를 기반으로 단계별 병목을 추적하는 방식으로 진행했습니다.

- 업로드와 분석 실행 분리
- JPA 기반 적재에서 PostgreSQL COPY 기반 적재로 전환
- parser / mapper 최적화
- snapshot writer 내부 병목을 payload build / encode / copyIn으로 분해
- PostgreSQL write path 설정 조정
- snapshot 보조 인덱스 정리

### Results
- 100k 기준 snapshot batch insert `13.3s → 8.5s`
- snapshot `copyIn` 기준 `7.7s → 1.5~1.8s`
- 전체 `orchestrator.total` 기준 약 `18s → 9~11s`
- 500k 데이터도 현재 구조에서 안정적으로 분석 가능

## Testing and CI

- 파일 유형 판별 테스트
- 필수 헤더 검증 테스트
- 숫자/날짜 파싱 테스트
- joinKey 생성 규칙 테스트
- GitHub Actions 기반 프론트/백엔드 빌드 검증

## Getting Started

### Backend
```bash
cd backend
./gradlew bootRun

Deploy: https://sellivu.vercel.app/
Project Document: https://www.notion.so/Sellivu-33294741d9a680048f7afae5069653ae?pvs=21