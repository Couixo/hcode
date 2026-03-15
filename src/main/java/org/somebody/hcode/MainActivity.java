package org.somebody.hcode;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextRequestCode;
    private Button buttonGenerate;
    private TextView textViewChallengeCode;
    private Button buttonClear;
    private android.widget.Switch switchScreenshot;
    private boolean hasResult = false;
    private boolean isScreenshotDisabled = false;
    private String waitingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        editTextRequestCode = findViewById(R.id.editTextRequestCode);
        buttonGenerate = findViewById(R.id.buttonGenerate);
        textViewChallengeCode = findViewById(R.id.textViewChallengeCode);
        buttonClear = findViewById(R.id.buttonCopy);
        switchScreenshot = findViewById(R.id.switchScreenshot);
        
        // 初始化等待文本
        waitingText = getString(R.string.waiting_input);
        
        // 检查是否为平板设备
        boolean isTablet = isTabletDevice();
        
        if (isTablet) {
            // 平板设备：初始化截屏开关
            setupScreenshotSwitch();
        } else {
            // 手机和Wear OS设备：隐藏截屏开关
            if (switchScreenshot != null) {
                switchScreenshot.setVisibility(android.view.View.GONE);
            }
        }
        // 设置输入框文本变化监听器
        editTextRequestCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // 根据输入框内容启用或禁用生成按钮
                buttonGenerate.setEnabled(s.length() > 0);
            }
        });

        // 设置生成按钮点击事件
        buttonGenerate.setOnClickListener(v -> {
            String requestCode = editTextRequestCode.getText().toString().trim();
            
            if (requestCode.isEmpty()) {
                Toast.makeText(this, R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Bug修复：将requestCode作为盐值传入generateChallenge
                String challengeCode = ChallengeValidator.generateChallenge(requestCode);
                textViewChallengeCode.setText(challengeCode);
                hasResult = true;
                
                // Bug修复：使用字符串资源而非硬编码
                Toast.makeText(this, R.string.generate_success, Toast.LENGTH_SHORT).show();
                
                // Wear OS 适配：显示清除按钮
                buttonClear.setVisibility(android.view.View.VISIBLE);
                
            } catch (Exception e) {
                // Bug修复：getString() 获取实际字符串，而非 int 资源ID拼接
                String errorMsg = getString(R.string.error_generate_failed) + ": " + e.getMessage();
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        // 设置清除按钮点击事件
        buttonClear.setOnClickListener(v -> {
            textViewChallengeCode.setText(waitingText);
            hasResult = false;
            Toast.makeText(this, getString(R.string.clear_button), Toast.LENGTH_SHORT).show();
            
            // Wear OS 适配：隐藏清除按钮
            buttonClear.setVisibility(android.view.View.GONE);
        });
    }
    
    /**
     * 检查是否为平板设备
     * @return true 如果是平板，false 如果是手机或Wear OS
     */
    private boolean isTabletDevice() {
        // 检查屏幕尺寸是否为平板尺寸
        android.content.res.Configuration config = getResources().getConfiguration();
        int screenSize = config.screenLayout & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK;
        
        return screenSize == android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE || 
               screenSize == android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
    
    /**
     * 设置截屏开关功能
     */
    private void setupScreenshotSwitch() {
        if (switchScreenshot == null) {
            return;
        }
        
        // 设置截屏开关状态变化监听器
        switchScreenshot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isScreenshotDisabled = isChecked;
            
            try {
                if (isScreenshotDisabled) {
                    // 禁用截屏
                    getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, 
                                       android.view.WindowManager.LayoutParams.FLAG_SECURE);
                    Toast.makeText(this, R.string.screenshot_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    // 启用截屏
                    getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
                    Toast.makeText(this, R.string.screenshot_disabled, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // 如果设置失败，回退开关状态
                switchScreenshot.setChecked(!isChecked);
                Toast.makeText(this, "设置截屏权限失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
