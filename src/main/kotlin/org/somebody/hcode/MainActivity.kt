import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import org.somebody.hcode.ChallengeValidator
import org.somebody.hcode.databinding.ActivityMainBinding // 自动生成的Binding类

class MainActivity : AppCompatActivity() {
    // 使用ViewBinding替代findViewById
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 按钮点击事件（直接通过binding访问控件）
        binding.generateButton.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            
            val requestCode = binding.inputCode.text.toString().trim()
            if (requestCode.length != 6) {
                binding.resultText.text = "请输入6位请求码"
                binding.resultText.setTextColor(getColor(android.R.color.holo_red_light))
                return@setOnClickListener
            }

            try {
                val challengeCode = ChallengeValidator.generateChallenge(requestCode)
                binding.resultText.text = "挑战码：$challengeCode"
                binding.resultText.setTextColor(getColor(android.R.color.black))
            } catch (e: Exception) {
                binding.resultText.text = "生成失败：${e.message}"
                binding.resultText.setTextColor(getColor(android.R.color.holo_red_light))
            }
        }
    }
}
