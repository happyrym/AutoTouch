package com.rymin.autotouch.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AutoTouchService : AccessibilityService() {

    companion object {
        private const val TAG = "[AutoTouchService]"
        private var instance: AutoTouchService? = null

        fun getInstance(): AutoTouchService? = instance
    }

    private var targetText: String = ""
    private var isSearching: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "접근성 서비스 연결됨")
    }

    fun setTargetText(text: String) {
        this.targetText = text
    }

    fun startSearching() {
        isSearching = true
        startPeriodicSearch()
        Log.d(TAG, "검색 시작: $targetText")
    }

    fun stopSearching() {
        isSearching = false
        searchRunnable?.let { handler.removeCallbacks(it) }
        Log.d(TAG, "검색 중지")
    }

    private fun startPeriodicSearch() {
        searchRunnable = object : Runnable {
            override fun run() {
                if (isSearching) {
                    searchAndClickText()
                    handler.postDelayed(this, 1000) // 1초마다 검색
                }
            }
        }
        searchRunnable?.let { handler.post(it) }
    }

    private fun searchAndClickText() {
        val rootNode = rootInActiveWindow
        rootNode?.let { node ->
            if (searchNodeForText(node, targetText)) {
                Log.d(TAG, "텍스트 발견하여 클릭: $targetText")
            }
            node.recycle()
        }
    }

    private fun searchNodeForText(node: AccessibilityNodeInfo?, text: String): Boolean {
        if (node == null) return false

        try {
            // 현재 노드의 텍스트 확인
            val nodeText = node.text?.toString()
            val contentDesc = node.contentDescription?.toString()
            val viewIdResourceName = node.viewIdResourceName

            val hasTargetText = (nodeText?.contains(text, ignoreCase = true) == true) ||
                    (contentDesc?.contains(text, ignoreCase = true) == true) ||
                    (viewIdResourceName?.contains(text, ignoreCase = true) == true)

            if (hasTargetText) {
                // 클릭 가능한 노드인지 확인
                if (node.isClickable && node.isFocusable) {
                    clickNode(node)
                    return true
                } else {
                    // 클릭 가능한 부모 노드 찾기
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            clickNode(parent)
                            parent.recycle()
                            return true
                        }
                        val temp = parent
                        parent = parent.parent
                        temp.recycle()
                    }
                }
            }

            // 자식 노드들 검색
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                child?.let {
                    if (searchNodeForText(it, text)) {
                        it.recycle()
                        return true
                    }
                    it.recycle()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "노드 검색 중 오류: ${e.message}")
        }

        return false
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        try {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            // 노드의 중심점 계산
            val centerX = bounds.centerX().toFloat()
            val centerY = bounds.centerY().toFloat()

            // 화면 경계 체크
            if (centerX <= 0 || centerY <= 0) {
                Log.w(TAG, "잘못된 좌표: ($centerX, $centerY)")
                return
            }

            // 터치 제스처 생성
            val clickPath = Path().apply {
                moveTo(centerX, centerY)
            }

            val clickStroke = GestureDescription.StrokeDescription(
                clickPath,
                0,
                100
            )

            val gestureBuilder = GestureDescription.Builder().apply {
                addStroke(clickStroke)
            }

            // 제스처 실행
            dispatchGesture(
                gestureBuilder.build(),
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription) {
                        Log.d(TAG, "터치 제스처 완료: ($centerX, $centerY)")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription) {
                        Log.w(TAG, "터치 제스처 취소됨")
                    }
                },
                null
            )

        } catch (e: Exception) {
            Log.e(TAG, "클릭 실행 중 오류: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 화면 변경 시 자동으로 검색
        event?.let {
            if (isSearching && (it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                        it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                // 너무 빈번한 호출을 방지하기 위해 기존 실행 취소 후 딜레이 추가
                searchRunnable?.let { runnable ->
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 500)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "접근성 서비스 중단됨")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopSearching()
        Log.d(TAG, "접근성 서비스 종료됨")
    }
}