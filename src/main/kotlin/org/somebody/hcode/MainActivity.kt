package org.somebody.hcode

import android.os.*
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var input by remember { mutableStateOf("") }
                    var result by remember { mutableStateOf("") }
                    val salt by remember { mutableStateOf(Algorithm.getSalt()) }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { if (it.all(Char::isDigit) && it.length <= 6) input = it },
                            label = { Text("请求码") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Text("盐值：$salt", modifier = Modifier.padding(8.dp))
                        Button(onClick = {
                            Thread {
                                val response = Algorithm.generateResponseCode(input, salt)
                                mainHandler.post { result = response }
                            }.start()
                        }) { Text("生成响应码") }
                        Text(
                            text = result,
                            modifier = Modifier.padding(top = 16.dp),
                            color = if (result.contains("失败") || result.contains("错误")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

object Algorithm {
    private var soLoaded = false
    private val MD5: MessageDigest? by lazy { try { MessageDigest.getInstance("MD5") } catch (e: Exception) { null } }
    init { try { System.loadLibrary("p4bu"); soLoaded = true } catch (e: Exception) {} }
    fun getSalt(): String = if (soLoaded) try { jni_getSalt().trim() } catch (e: Exception) { "获取盐值失败" } else "SO未加载"
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
    } catch (e: Exception) { "处理失败：${e.message}" }
    private external fun jni_getSalt(): String
}
