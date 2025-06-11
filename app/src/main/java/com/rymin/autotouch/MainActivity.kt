package com.rymin.autotouch

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rymin.autotouch.ui.theme.AutoTouchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoTouchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoTouchScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTouchScreen(viewModel: AutoTouchViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // 접근성 서비스 상태 확인
    val isAccessibilityEnabled = remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 타이틀
        Text(
            text = "🤖 자동 터치",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 접근성 서비스 상태
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAccessibilityEnabled.value)
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                else
                    Color(0xFFF44336).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isAccessibilityEnabled.value) "✅ 접근성 서비스 활성화됨" else "❌ 접근성 서비스 비활성화",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAccessibilityEnabled.value) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }

        // 타겟 텍스트 입력
        OutlinedTextField(
            value = uiState.targetText,
            onValueChange = viewModel::updateTargetText,
            label = { Text("찾을 텍스트") },
            placeholder = { Text("예: 확인, 다음, 시작하기") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // 상태 표시
        if (uiState.isSearching) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "\"${uiState.targetText}\" 검색 중...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 시작/중지 버튼
        Button(
            onClick = {
                if (uiState.isSearching) {
                    viewModel.stopSearching()
                } else {
                    if (uiState.targetText.isBlank()) {
                        Toast.makeText(context, "찾을 텍스트를 입력하세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!isAccessibilityEnabled.value) {
                        Toast.makeText(context, "접근성 서비스를 먼저 활성화하세요", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    viewModel.startSearching()
                    Toast.makeText(context, "자동 터치 시작: ${uiState.targetText}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isSearching) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (uiState.isSearching) "🛑 자동 터치 중지" else "▶️ 자동 터치 시작",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 접근성 설정 버튼
        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
                // 설정에서 돌아왔을 때 상태 업데이트를 위해 약간의 딜레이 후 체크
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
                }, 1000)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "⚙️ 접근성 설정 열기",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 주의사항
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFC107).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⚠️ 사용 방법",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "1. 접근성 설정에서 '자동 터치' 서비스를 활성화하세요\n" +
                            "2. 찾을 텍스트를 입력하고 시작 버튼을 누르세요\n" +
                            "3. 다른 앱으로 이동하면 자동으로 해당 텍스트를 찾아 터치합니다",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

    return enabledServices.any { service ->
        service.id.contains(context.packageName)
    }
}

@Preview(showBackground = true)
@Composable
fun AutoTouchScreenPreview() {
    AutoTouchTheme {
        AutoTouchScreen()
    }
}