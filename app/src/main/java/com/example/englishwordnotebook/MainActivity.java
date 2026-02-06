package com.example.englishwordnotebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.englishwordnotebook.base.enums.WordOperateStatus;
import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;
import com.example.englishwordnotebook.data.vo.WordWithPosAndMeaning;
import com.example.englishwordnotebook.viewModel.WordViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页面：整合单词新增、数据观察、状态回调
 * 解决LiveData泛型不匹配、ViewModel方法调用所有报错
 */
public class MainActivity extends AppCompatActivity {
    // ViewModel实例
    private WordViewModel wordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 布局可根据你的实际布局修改，建议包含新增按钮（fab_add_word）
        setContentView(R.layout.activity_main);

        // 1. 初始化ViewModel（官方推荐方式，避免内存泄漏）
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 2. 绑定新增单词按钮（悬浮按钮/普通按钮，id与布局匹配）
        initAddWordButton();

        // 3. 观察单词列表数据（核心修正：泛型匹配WordWithPosAndMeaning）
        observeWordListData();

        // 4. 观察操作状态（新增成功/失败/重复等，吐司提示）
        observeOperateStatus();
    }

    /**
     * 绑定新增单词按钮点击事件
     * 布局中建议添加FloatingActionButton，id：fab_add_word
     * 若用普通Button，替换为findViewById(R.id.xxx)即可
     */
    private void initAddWordButton() {
        FloatingActionButton fabAddWord = findViewById(R.id.fab_add_word);
        fabAddWord.setOnClickListener(v -> showAddWordDialog());
    }

    /**
     * 优化版新增单词对话框
     * 包含：英文输入+词性选择+释义输入+例句输入，基础非空校验
     */
    private void showAddWordDialog() {
        // 加载对话框布局（已修正无报错的dialog_add_word.xml）
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        // 获取布局控件
        EditText etEnglish = dialogView.findViewById(R.id.et_english);
        Spinner spPos = dialogView.findViewById(R.id.sp_pos);
        EditText etMeaning = dialogView.findViewById(R.id.et_meaning);
        EditText etExample = dialogView.findViewById(R.id.et_example);

        // 构建对话框
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(dialogView)
                .setPositiveButton("确认新增", (dialog, which) -> {
                    // 获取输入内容并去空格
                    String english = etEnglish.getText().toString().trim();
                    String pos = spPos.getSelectedItem().toString().trim();
                    String meaning = etMeaning.getText().toString().trim();
                    String example = etExample.getText().toString().trim();

                    // 基础非空校验（UI层轻量提示）
                    if (english.isEmpty()) {
                        Toast.makeText(MainActivity.this, "英文单词不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (meaning.isEmpty()) {
                        Toast.makeText(MainActivity.this, "单词释义不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 构造参数列表（匹配ViewModel方法，单词性+单释义，适配后续拓展）
                    List<String> posList = new ArrayList<>();
                    posList.add(pos);
                    List<String> meaningList = new ArrayList<>();
                    meaningList.add(meaning);

                    // 调用ViewModel新增方法（无参数不匹配报错）
                    wordViewModel.addCompleteWord(english, example, posList, meaningList);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * 观察单词列表数据（核心修正：泛型为List<WordWithPosAndMeaning>）
     * 可在此处更新RecyclerView/ListView数据，兼容原有Word实体操作
     */
    private void observeWordListData() {
        wordViewModel.wordListLiveData.observe(this, new Observer<List<WordWithPosAndMeaning>>() {
            @Override
            public void onChanged(List<WordWithPosAndMeaning> wordWithPosAndMeanings) {
                // 空数据判断，避免空指针
                if (wordWithPosAndMeanings == null || wordWithPosAndMeanings.isEmpty()) {
                    Toast.makeText(MainActivity.this, "暂无单词数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 遍历VO列表，获取【Word实体】+【词性+释义】关联数据
                for (WordWithPosAndMeaning wwpm : wordWithPosAndMeanings) {
                    // 1. 获取原有Word实体，所有原有操作完全兼容，无需修改
                    Word word = wwpm.getWord();
                    long wordId = word.getId();
                    String english = word.getEnglishWord();
                    String example = word.getExampleSentence();
                    boolean isMastered = word.isMastered();
                    long createTime = word.getCreateTime();

                    // 2. 获取关联词性（单词性场景取第一个，多词性可循环遍历）
                    String posType = "";
                    List<WordPartOfSpeech> posList = wwpm.getPartOfSpeechList();
                    if (posList != null && !posList.isEmpty()) {
                        posType = posList.get(0).getPosType();
                    }

                    // 3. 获取关联释义（单释义场景取第一个，多释义可循环遍历）
                    String meaningContent = "";
                    List<WordMeaning> meaningList = wwpm.getMeaningList();
                    if (meaningList != null && !meaningList.isEmpty()) {
                        meaningContent = meaningList.get(0).getMeaningContent();
                    }

                    // 测试打印（可删除，替换为列表展示逻辑）
                    System.out.println("单词：" + english + "，词性：" + posType + "，释义：" + meaningContent);
                }

                // 【关键扩展位】：更新RecyclerView/ListView数据源
                // 示例：mWordAdapter.setData(wordWithPosAndMeanings);
                // 示例：mWordAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 观察操作状态，统一吐司提示
     * 匹配WordOperateStatus枚举的所有状态
     */
    private void observeOperateStatus() {
        wordViewModel.operateStatusLiveData.observe(this, new Observer<WordOperateStatus>() {
            @Override
            public void onChanged(WordOperateStatus status) {
                if (status == null) return;
                switch (status) {
                    case SUCCESS:
                        Toast.makeText(MainActivity.this, "新增单词成功", Toast.LENGTH_SHORT).show();
                        break;
                    case WORD_DUPLICATE:
                        Toast.makeText(MainActivity.this, "该单词已存在，请勿重复添加", Toast.LENGTH_SHORT).show();
                        break;
                    case PARAM_INVALID:
                        Toast.makeText(MainActivity.this, "参数不能为空，词性与释义需一一对应", Toast.LENGTH_SHORT).show();
                        break;
                    case UNKNOWN_ERROR:
                        Toast.makeText(MainActivity.this, "新增失败，未知错误", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
    }
}