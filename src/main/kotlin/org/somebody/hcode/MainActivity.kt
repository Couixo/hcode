package org.somebody.hcode

import android.os.*
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*  // 新增：导入软键盘相关类
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RoundScreenContent()
                }
            }
        }
    }
}

@Composable
fun RoundScreenContent() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    val salt by remember { mutableStateOf(Algorithm.getSalt()) }

    BoxWithConstraints {
        val isRound = constraints.isRound
        val paddingValue = if (isRound) 32.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.all(Char::isDigit) && it.length <= 6) input = it },
                label = { Text("请求码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "盐值：$salt",
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Button(
                onClick = {
                    Thread {
                        val response = Algorithm.generateResponseCode(input, salt)
                        mainHandler.post { result = response }
                    }.start()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("生成响应码")
            }

            Text(
                text = result,
                modifier = Modifier.padding(top = 16.dp),
                color = if (result.contains("失败") || result.contains("错误")) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

object Algorithm {
    private var soLoaded = false
    private val MD5: MessageDigest? by lazy { 
        try { MessageDigest.getInstance("MD5") } 
        catch (e: Exception) { null } 
    }

    init { 
        try { 
            System.loadLibrary("p4bu")
            soLoaded = true 
        } catch (e: Exception) {} 
    }

    fun getSalt(): String = if (soLoaded) {
        try { jni_getSalt().trim() } 
        catch (e: Exception) { "获取盐值失败" }
    } else {
        "SO未加载"
    }

    fun generateResponseCode(input: String, salt: String): String = try {
        when {
            input.isBlank() || salt.isBlank() -> "输入不能为空"
            !input.all(Char::isDigit) || input.length != 6 -> "请求码格式错误（需6位数字）"
            !soLoaded -> "SO库未加载"
            MD5 == null -> "MD5初始化失败"
            else -> {
                val responseCode = MD5!!.digest((salt + input).toByteArray())
                    .joinToString("") { "%02x".format(it) }
                    .substring(0, 6)
                    .replace(Regex("[a-fA-F]")) { (it.value[0].lowercaseChar() - 'a' + 1).toString() }
                "响应码：$responseCode"
            }
        }
    } catch (e: Exception) { 
        "处理失败：${e.message}" 
    }

    private external fun jni_getSalt(): String
}
