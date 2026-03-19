package org.somebody.hcode
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "HCode"
        private var soLoaded = false
        init {
            try { System.loadLibrary("p4bu"); soLoaded = true } 
            catch (e: Exception) { Log.e(TAG, "SO加载失败: ${e.message}") }
        }
        private external fun jni_getSalt(): String
        fun getSalt() = if (soLoaded) try { jni_getSalt().trim() } catch (e: Exception) { "获取盐值失败" } else "SO未加载"
        private val REGEX = Regex("[a-f]")
        private val MD5 by lazy { try { MessageDigest.getInstance("MD5") } catch (e: Exception) { null } }
    }
    data class S(val i: TextFieldValue = TextFieldValue(""), val r: String = "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var s by remember { mutableStateOf(S()) }
                    val salt by remember { mutableStateOf(getSalt()) }
                    Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.Center, Alignment.CenterHorizontally) {
                        OutlinedTextField(s.i, { if (it.text.all(Char::isDigit)) s = s.copy(i = it) }, 
                            label = { Text("请求码") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), 
                            modifier = Modifier.padding(bottom = 8.dp))
                        Text("盐值：$salt", Modifier.padding(8.dp))
                        Button({
                            when {
                                s.i.text.length != 6 -> s = s.copy(r = "格式错误（需6位）")
                                salt in listOf("SO未加载", "获取盐值失败") -> s = s.copy(r = "SO加载失败")
                                MD5 == null -> s = s.copy(r = "MD5初始化失败")
                                else -> LaunchedEffect(Unit) {
                                    try {
                                        val b = MD5!!.digest((salt + s.i.text).toByteArray())
                                        val sb = StringBuilder()
                                        b.forEach { sb.append(String.format("%02x", it)) }
                                        val res = sb.toString().replace(REGEX) { (it[0]-'a'+1).toString() }.substring(0,6)
                                        s = s.copy(r = "响应码：$res")
                                    } catch (e: Exception) { s = s.copy(r = "生成失败") }
                                }
                            }
                        }, Modifier.padding(8.dp)) { Text("生成") }
                        Text(s.r, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
