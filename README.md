# 가계부 애플리케이션

객체지향프로그래밍 수업 프로젝트로 개발한 안드로이드 가계부 애플리케이션입니다.

[디자인 시안 보기 (Figma)](https://www.figma.com/design/aUV7OddROBLlGzt34qUs1J/Travel-APP?node-id=0-1&t=cGw0TTfn2niEiWNf-1](https://www.figma.com/design/1JphfW6dsQFx15wwlPcMT1/OOP-AccountBook?node-id=0-1&t=4smghJCnOZxVWV8X-1)

## 🌟 주요 기능

- **사용자 인증**: Firebase를 이용한 회원가입 및 로그인 기능을 제공합니다.
- **수입/지출 관리**: 날짜와 함께 수입과 지출 내역을 등록하고 목록으로 확인할 수 있습니다.
- **통계 확인**: 등록된 내역을 바탕으로 총 수입, 총 지출, 잔액을 계산하여 보여줍니다.
- **시각화**: MPAndroidChart 라이브러리를 사용하여 수입과 지출의 비율을 원형 차트로 시각화하여 보여줍니다.

## 🛠️ 사용 기술

- **언어**: Kotlin
- **데이터베이스**: Firebase Realtime Database
- **인증**: Firebase Authentication
- **차트 라이브러리**: MPAndroidChart
- **UI**: Android Jetpack (View Binding, Navigation Component)

## ⚙️ 설정 방법

1.  이 저장소를 클론합니다.
    ```bash
    git clone https://github.com/psw204/OOP-AccountBook.git
    ```
2.  Android Studio에서 프로젝트를 엽니다.
3.  Firebase 프로젝트를 생성하고 `google-services.json` 파일을 `app/` 디렉토리에 추가합니다.
4.  프로젝트를 빌드하고 실행합니다.
