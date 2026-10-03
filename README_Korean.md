# Smarter Moving

Smarter Moving 1.0.0은 Minecraft 1.12.2와 Forge 14.23.5.2854용 모드입니다. 오르기, 기어가기, 슬라이딩, 수영, 다이빙, 점프 및 이동 애니메이션을 추가합니다.

## 설치

Minecraft 1.12.2용 Forge 14.23.5.2854를 설치한 뒤 `SmarterMoving-1.12.2-1.0.0.jar`를 `mods` 폴더에 넣으세요. 전용 서버에도 같은 JAR를 설치하세요. PlayerAPI, RenderPlayerAPI, SmartRender는 필요하지 않습니다.

## 빌드

Java 8에서 `./gradlew clean build` 명령을 실행하세요. Windows에서는 `gradlew.bat clean build`를 사용하세요. 완성된 JAR는 `build/libs/`에 생성됩니다.

## 설정

처음 실행하면 Minecraft의 `options.txt` 옆에 `smart_moving_options.txt`가 생성됩니다. 이 파일에서 이동 동작을 설정할 수 있습니다.

## 출처

이 프로젝트는 [Smart Moving Reboot](https://github.com/doch2/SmartMovingReboot)를 계승합니다. 원작자 Divisor와 이후 포팅 및 기여를 진행한 JonnyNova, elveskevtar, doch13_ 및 다른 기여자들에게 감사를 표합니다. 현재 검증 상태와 마이그레이션 설명은 [MIGRATION.md](MIGRATION.md)를 참조하세요.
