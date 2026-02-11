package com.example.englishwordnotebook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
 * 主页面：整合单词新增、数据观察、RecyclerView列表展示
 * 已补全所有列表展示逻辑，解决“数据有但不显示”问题
 */
public class MainActivity extends AppCompatActivity {
    // ViewModel实例
    private WordViewModel wordViewModel;
    // RecyclerView和适配器（核心新增）
    private RecyclerView rvWordList;
    private WordListAdapter wordListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化ViewModel
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 2. 初始化RecyclerView（核心新增：列表展示的基础）
        initRecyclerView();

        // 3. 绑定新增单词按钮
        initAddWordButton();

        // 4. 观察单词列表数据（补全数据绑定到列表的逻辑）
        observeWordListData();

        // 5. 观察操作状态
        observeOperateStatus();
    }

    /**
     * 核心新增：初始化RecyclerView
     * 包含布局管理器、适配器、空数据适配
     */
    private void initRecyclerView() {
        // 1. 绑定布局中的RecyclerView（需确保activity_main.xml中有此id）
        rvWordList = findViewById(R.id.rv_word_list);

        // 2. 设置布局管理器（必须有，否则列表无法展示）
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWordList.setLayoutManager(layoutManager);

        // 3. 初始化适配器并绑定到RecyclerView
        wordListAdapter = new WordListAdapter();
        rvWordList.setAdapter(wordListAdapter);
    }

    /**
     * 绑定新增单词按钮点击事件
     */
    private void initAddWordButton() {
        FloatingActionButton fabAddWord = findViewById(R.id.fab_add_word);
        fabAddWord.setOnClickListener(v -> showAddWordDialog());
    }

    /**
     * 新增单词对话框（原有逻辑不变）
     */
    private void showAddWordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        EditText etEnglish = dialogView.findViewById(R.id.et_english);
        Spinner spPos = dialogView.findViewById(R.id.sp_pos);
        EditText etMeaning = dialogView.findViewById(R.id.et_meaning);
        EditText etExample = dialogView.findViewById(R.id.et_example);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(dialogView)
                .setPositiveButton("确认新增", (dialog, which) -> {
                    String english = etEnglish.getText().toString().trim();
                    String pos = spPos.getSelectedItem().toString().trim();
                    String meaning = etMeaning.getText().toString().trim();
                    String example = etExample.getText().toString().trim();

                    if (english.isEmpty()) {
                        Toast.makeText(MainActivity.this, "英文单词不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (meaning.isEmpty()) {
                        Toast.makeText(MainActivity.this, "单词释义不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> posList = new ArrayList<>();
                    posList.add(pos);
                    List<String> meaningList = new ArrayList<>();
                    meaningList.add(meaning);

                    wordViewModel.addCompleteWord(english, example, posList, meaningList);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * 观察单词列表数据（补全：把数据传给适配器，刷新列表）
     */
    private void observeWordListData() {
        wordViewModel.wordListLiveData.observe(this, new Observer<List<WordWithPosAndMeaning>>() {
            @Override
            public void onChanged(List<WordWithPosAndMeaning> wordWithPosAndMeanings) {
                if (wordWithPosAndMeanings == null || wordWithPosAndMeanings.isEmpty()) {
                    Toast.makeText(MainActivity.this, "暂无单词数据", Toast.LENGTH_SHORT).show();
                    wordListAdapter.setData(new ArrayList<>()); // 空数据适配
                    return;
                }

                // 核心修改：把数据传给适配器，刷新列表
                wordListAdapter.setData(wordWithPosAndMeanings);
                wordListAdapter.notifyDataSetChanged();

                // 原有打印逻辑（可保留用于调试）
                for (WordWithPosAndMeaning wwpm : wordWithPosAndMeanings) {
                    Word word = wwpm.getWord();
                    String english = word.getEnglishWord();
                    String posType = "";
                    List<WordPartOfSpeech> posList = wwpm.getPartOfSpeechList();
                    if (posList != null && !posList.isEmpty()) {
                        posType = posList.get(0).getPosType();
                    }
                    String meaningContent = "";
                    List<WordMeaning> meaningList = wwpm.getMeaningList();
                    if (meaningList != null && !meaningList.isEmpty()) {
                        meaningContent = meaningList.get(0).getMeaningContent();
                    }
                    System.out.println("单词：" + english + "，词性：" + posType + "，释义：" + meaningContent);
                }
            }
        });
    }

    /**
     * 观察操作状态（原有逻辑不变）
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

    /**
     * 核心新增：RecyclerView适配器
     * 适配WordWithPosAndMeaning数据，展示单词、词性、释义
     */
    private class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
        // 适配器数据集
        private List<WordWithPosAndMeaning> mData = new ArrayList<>();

        // 设置数据的方法
        public void setData(List<WordWithPosAndMeaning> data) {
            this.mData.clear();
            this.mData.addAll(data);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // 加载列表项布局（需创建item_word.xml，见下方）
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_word, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            WordWithPosAndMeaning wwpm = mData.get(position);
            Word word = wwpm.getWord();

            // 绑定单词内容
            holder.tvEnglish.setText(word.getEnglishWord());

            // 绑定词性（取第一个）
            List<WordPartOfSpeech> posList = wwpm.getPartOfSpeechList();
            String pos = posList != null && !posList.isEmpty() ? posList.get(0).getPosType() : "无词性";
            holder.tvPos.setText(pos);

            // 绑定释义（取第一个）
            List<WordMeaning> meaningList = wwpm.getMeaningList();
            String meaning = meaningList != null && !meaningList.isEmpty() ? meaningList.get(0).getMeaningContent() : "无释义";
            holder.tvMeaning.setText(meaning);

            // 绑定例句（可选）
            String example = word.getExampleSentence() != null ? word.getExampleSentence() : "无例句";
            holder.tvExample.setText(example);
        }

        @Override
        public int getItemCount() {
            return mData.size(); // 返回真实数据数量
        }

        // 列表项ViewHolder
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEnglish; // 单词
            TextView tvPos;     // 词性
            TextView tvMeaning; // 释义
            TextView tvExample; // 例句

            public ViewHolder(View itemView) {
                super(itemView);
                // 绑定列表项布局中的控件（需与item_word.xml的id一致）
                tvEnglish = itemView.findViewById(R.id.tv_english);
                tvPos = itemView.findViewById(R.id.tv_pos);
                tvMeaning = itemView.findViewById(R.id.tv_meaning);
                tvExample = itemView.findViewById(R.id.tv_example);
            }
        }
    }
}