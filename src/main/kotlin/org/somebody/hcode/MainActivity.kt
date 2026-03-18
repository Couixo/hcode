package org.somebody.hcode

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    companion object {
        init { System.loadLibrary("p4bu") }
        private external fun jni_getSalt(): String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputCode = findViewById<EditText>(R.id.inputCode)
        val resultText = findViewById<TextView>(R.id.resultText)
        val generateButton = findViewById<Button>(R.id.generateButton)

        generateButton.setOnClickListener {
            val code = inputCode.text.toString().trim()
            if (code.length != 6) {
                resultText.text = "请输入6位请求码"
                return@setOnClickListener
            }
            val salt = jni_getSalt().trim()
            val md5 = MessageDigest.getInstance("MD5")
                .digest((salt + code).toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
                .replace(Regex("[a-f]")) { (it.value[0] - 'a' + 1).toString() }
                .substring(0, 6)
            resultText.text = "挑战码：$md5"
        }
    }
}
