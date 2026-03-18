import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import org.somebody.hcode.ChallengeValidator;
import org.somebody.hcode.R;


/**
 * 主界面Activity：处理用户输入并生成挑战码
 */
public class MainActivity extends AppCompatActivity {
    // 控件声明（4空格缩进）
    private TextInputEditText inputCode;
    private TextView resultText;
    private Button generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件（4空格缩进）
        inputCode = findViewById(R.id.input_code);
        resultText = findViewById(R.id.result_text);
        generateButton = findViewById(R.id.generate_button);

        // 按钮点击事件（4空格缩进）
        generateButton.setOnClickListener(v -> {
            // 触觉反馈（8空格缩进）
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            // 获取输入（8空格缩进）
            String requestCode = inputCode.getText().toString().trim();
            if (requestCode.length() != 6) {
                resultText.setText("请输入6位请求码");
                resultText.setTextColor(getColor(R.color.md_theme_error));
                return;
            }

            // 生成挑战码（8空格缩进）
            try {
                String challengeCode = ChallengeValidator.generateChallenge(requestCode);
                resultText.setText("挑战码：" + challengeCode);
                resultText.setTextColor(getColor(R.color.md_theme_onSurface));
            } catch (Exception e) {
                resultText.setText("生成失败：" + e.getMessage());
                resultText.setTextColor(getColor(R.color.md_theme_error));
            }
        });
    }
}
