package com.example.englishwordnotebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.englishwordnotebook.viewModel.WordViewModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleReadingActivity extends AppCompatActivity {

    private EditText etArticleInput;
    private Button btnShowArticle;
    private ScrollView scrollView;
    private TextView tvArticleDisplay;
    private WordViewModel wordViewModel;
    private PopupWindow meaningPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_reading);

        initViews();
        initViewModel();
        setupListeners();
    }

    private void initViews() {
        etArticleInput = findViewById(R.id.et_article_input);
        btnShowArticle = findViewById(R.id.btn_show_article);
        scrollView = findViewById(R.id.scroll_view);
        tvArticleDisplay = findViewById(R.id.tv_article_display);
    }

    private void initViewModel() {
        wordViewModel = new androidx.lifecycle.ViewModelProvider(this).get(WordViewModel.class);
    }

    private void setupListeners() {
        btnShowArticle.setOnClickListener(v -> {
            String articleText = etArticleInput.getText().toString();
            if (!articleText.isEmpty()) {
                displayArticleWithClickableWords(articleText);
            } else {
                Toast.makeText(this, "请输入文章内容", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayArticleWithClickableWords(String text) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        
        // 使用正则表达式识别英文单词
        Pattern pattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = pattern.matcher(text);
        
        int lastIndex = 0;
        while (matcher.find()) {
            // 添加非单词部分
            spannableStringBuilder.append(text.substring(lastIndex, matcher.start()));
            
            // 添加可点击的单词
            String word = matcher.group();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showWordMeaning(word);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    // 设置单词样式，轻微变色，无下划线
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(false);
                }
            };
            
            spannableStringBuilder.append(word);
            spannableStringBuilder.setSpan(clickableSpan, 
                    spannableStringBuilder.length() - word.length(), 
                    spannableStringBuilder.length(), 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            lastIndex = matcher.end();
        }
        
        // 添加剩余部分
        spannableStringBuilder.append(text.substring(lastIndex));
        
        tvArticleDisplay.setText(spannableStringBuilder);
        tvArticleDisplay.setMovementMethod(LinkMovementMethod.getInstance());
        tvArticleDisplay.setHighlightColor(Color.TRANSPARENT); // 去除点击时的高亮
    }

    private void showWordMeaning(String word) {
        // 检查悬浮窗权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要悬浮窗权限来显示单词释义", Toast.LENGTH_SHORT).show();
            return;
        }

        // 加载悬浮窗布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_word_meaning, null);
        TextView tvWord = popupView.findViewById(R.id.tv_word);
        TextView tvPhonetic = popupView.findViewById(R.id.tv_phonetic);
        TextView tvMeaning = popupView.findViewById(R.id.tv_meaning);
        Button btnAddToNotebook = popupView.findViewById(R.id.btn_add_to_notebook);
        Button btnClose = popupView.findViewById(R.id.btn_close);

        // 设置单词
        tvWord.setText(word);
        
        // 这里应该调用词典API获取释义，暂时使用模拟数据
        tvPhonetic.setText("[fəˈnetɪk]");
        tvMeaning.setText("释义：单词的含义\n详细解释：这是一个示例释义");

        // 创建悬浮窗
        meaningPopup = new PopupWindow(popupView, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                true);
        
        // 设置悬浮窗样式
        meaningPopup.setElevation(10);
        meaningPopup.setFocusable(true);
        
        // 显示悬浮窗
        meaningPopup.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        // 关闭按钮点击事件
        btnClose.setOnClickListener(v -> {
            if (meaningPopup != null && meaningPopup.isShowing()) {
                meaningPopup.dismiss();
            }
        });

        // 添加到笔记本按钮点击事件
        btnAddToNotebook.setOnClickListener(v -> {
            // 这里应该实现添加到单词本的逻辑
            Toast.makeText(this, "已添加到单词本", Toast.LENGTH_SHORT).show();
        });
    }
}
