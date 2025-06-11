# 🤖 자동 터치 앱 (Kotlin + Compose)

화면에 표시된 특정 텍스트를 자동으로 감지하고 터치해주는 안드로이드 앱입니다.  
"확인", "다음", "시작하기" 같은 버튼을 자동으로 눌러주는 데 유용합니다.

---

## ✨ 주요 기능

- **텍스트 자동 인식**  
  화면에 보이는 모든 UI 요소에서 지정한 텍스트를 찾아냅니다.

- **자동 터치 실행**  
  찾은 텍스트 위치를 터치 제스처로 자동 클릭합니다.

- **실시간 감지**  
  화면이 바뀌면 자동으로 다시 탐색을 시작합니다.

- **현대적인 UI**  
  Jetpack Compose로 구성된 간결하고 직관적인 인터페이스.

- **상태 관리**  
  ViewModel과 StateFlow를 이용한 MVVM 패턴 적용.

---

## 🏗️ 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose
- **아키텍처**: MVVM (ViewModel + StateFlow)
- **접근성**: Android Accessibility Service
- **터치 제스처**: GestureDescription API 사용

---

## 📱 사용 방법

### 1. 설치 및 접근성 설정

- 앱 설치 후  
  `설정 → 접근성 → 다운로드한 앱 → "자동 터치"`  
  접근성 서비스를 **직접 활성화**해주세요.

### 2. 앱 사용

1. 앱을 실행하고 찾고 싶은 텍스트를 입력합니다.  
   (예: "확인", "다음", "시작하기")

2. **▶ 자동 터치 시작** 버튼을 누릅니다.

3. 다른 앱으로 이동하면 지정한 텍스트를 찾아 자동으로 터치합니다.

4. 중지하려면 **🛑 자동 터치 중지** 버튼을 누르면 됩니다.

---

## 🔧 주요 구현

### `AutoTouchService.kt`

- `AccessibilityService`를 상속한 핵심 서비스
- 노드 트리를 재귀적으로 탐색하여 텍스트를 찾고,
- `GestureDescription`으로 터치 이벤트 실행

```kotlin
private fun searchNodeForText(node: AccessibilityNodeInfo?, text: String): Boolean {
    // 텍스트, contentDescription, viewId 검사
    // 자식 노드 재귀 탐색
}
