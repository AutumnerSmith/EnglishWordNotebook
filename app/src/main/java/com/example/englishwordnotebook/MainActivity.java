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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    // 排序状态
    private boolean isSortByTime = true; // true: 按时间倒序，false: 按字母正序

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

        // 4. 绑定排序按钮
        initSortButton();

        // 5. 绑定筛选按钮
        initFilterButton();

        // 6. 绑定搜索按钮
        initSearchButton();

        // 7. 绑定背诵按钮
        initReciteButton();

        // 8. 绑定文章阅读按钮
        initArticleReadingButton();

        // 9. 观察单词列表数据（补全数据绑定到列表的逻辑）
        observeWordListData();

        // 10. 观察操作状态
        observeOperateStatus();
    }

    /**
     * 绑定排序按钮点击事件
     */
    private void initSortButton() {
        Button btnSort = findViewById(R.id.btn_sort);
        btnSort.setOnClickListener(v -> {
            isSortByTime = !isSortByTime;
            if (isSortByTime) {
                btnSort.setText("时间");
                wordViewModel.loadAllWordsByTimeDesc();
            } else {
                btnSort.setText("字母");
                wordViewModel.loadAllWordsByLetterAsc();
            }
        });
    }

    /**
     * 绑定筛选按钮点击事件
     */
    private void initFilterButton() {
        Button btnFilter = findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    /**
     * 显示词性筛选对话框
     */
    private void showFilterDialog() {
        // 获取词性数组
        String[] posTypes = getResources().getStringArray(R.array.part_of_speech);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("按词性筛选")
                .setItems(posTypes, (dialog, which) -> {
                    String selectedPos = posTypes[which];
                    wordViewModel.loadWordsByPosType(selectedPos);
                    Toast.makeText(MainActivity.this, "已筛选：" + selectedPos, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    /**
     * 绑定搜索按钮点击事件
     */
    private void initSearchButton() {
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            showSearchDialog();
        });
    }

    /**
     * 绑定背诵按钮点击事件
     */
    private void initReciteButton() {
        Button btnRecite = findViewById(R.id.btn_recite);
        btnRecite.setOnClickListener(v -> {
            showReciteDialog();
        });
    }

    /**
     * 绑定文章阅读按钮点击事件
     */
    private void initArticleReadingButton() {
        Button btnArticleReading = findViewById(R.id.btn_article_reading);
        btnArticleReading.setOnClickListener(v -> {
            startActivity(new android.content.Intent(MainActivity.this, ArticleReadingActivity.class));
        });
    }

    // 背诵相关变量
    private List<WordWithPosAndMeaning> allWords; // 所有单词列表
    private int currentReciteIndex = 0; // 当前背诵索引
    private boolean isShowingMeaning = false; // 是否显示释义

    /**
     * 显示背诵对话框
     */
    private void showReciteDialog() {
        // 加载所有单词
        if (allWords == null || allWords.isEmpty()) {
            wordViewModel.wordListLiveData.observe(this, words -> {
                allWords = words;
                if (allWords == null || allWords.isEmpty()) {
                    Toast.makeText(MainActivity.this, "暂无单词数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 随机打乱单词顺序
                java.util.Collections.shuffle(allWords);
                currentReciteIndex = 0;
                isShowingMeaning = false;
                showReciteWordDialog();
            });
        } else {
            // 随机打乱单词顺序
            java.util.Collections.shuffle(allWords);
            currentReciteIndex = 0;
            isShowingMeaning = false;
            showReciteWordDialog();
        }
    }

    /**
     * 显示背诵单词对话框
     */
    private void showReciteWordDialog() {
        if (allWords == null || allWords.isEmpty() || currentReciteIndex >= allWords.size()) {
            Toast.makeText(MainActivity.this, "背诵完成", Toast.LENGTH_SHORT).show();
            return;
        }

        WordWithPosAndMeaning currentWord = allWords.get(currentReciteIndex);
        Word word = currentWord.getWord();

        // 加载背诵对话框布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recite, null);
        TextView tvEnglish = dialogView.findViewById(R.id.tv_recite_english);
        TextView tvMeaning = dialogView.findViewById(R.id.tv_recite_meaning);
        Button btnShowMeaning = dialogView.findViewById(R.id.btn_show_meaning);
        Button btnMarkMastered = dialogView.findViewById(R.id.btn_mark_mastered);

        // 设置单词
        tvEnglish.setText(word.getEnglishWord());
        // 初始隐藏释义
        tvMeaning.setVisibility(View.GONE);
        isShowingMeaning = false;

        // 显示/隐藏释义按钮
        btnShowMeaning.setOnClickListener(v -> {
            if (isShowingMeaning) {
                tvMeaning.setVisibility(View.GONE);
                btnShowMeaning.setText("显示释义");
                isShowingMeaning = false;
            } else {
                // 显示释义
                StringBuilder meaningText = new StringBuilder();
                List<WordPartOfSpeech> posList = currentWord.getPartOfSpeechList();
                List<WordMeaning> meaningList = currentWord.getMeaningList();
                if (posList != null && !posList.isEmpty() && meaningList != null && !meaningList.isEmpty()) {
                    for (int i = 0; i < Math.min(posList.size(), meaningList.size()); i++) {
                        meaningText.append(posList.get(i).getPosType()).append(": ").append(meaningList.get(i).getMeaningContent()).append("\n");
                    }
                } else {
                    meaningText.append("无释义");
                }
                tvMeaning.setText(meaningText.toString());
                tvMeaning.setVisibility(View.VISIBLE);
                btnShowMeaning.setText("隐藏释义");
                isShowingMeaning = true;
            }
        });

        // 标记为已掌握按钮
        btnMarkMastered.setOnClickListener(v -> {
            wordViewModel.markWordAsMastered(word);
            Toast.makeText(MainActivity.this, "已标记为已掌握", Toast.LENGTH_SHORT).show();
        });

        // 构建弹窗
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("单词背诵")
                .setView(dialogView)
                .setPositiveButton("下一个", (dialog1, which) -> {
                    currentReciteIndex++;
                    showReciteWordDialog();
                })
                .setNegativeButton("结束", null)
                .create();
        dialog.show();
    }

    /**
     * 显示搜索对话框
     */
    private void showSearchDialog() {
        // 加载搜索对话框布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null);
        EditText etSearch = dialogView.findViewById(R.id.et_search);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("搜索单词")
                .setView(dialogView)
                .setPositiveButton("搜索", (dialog, which) -> {
                    String keyword = etSearch.getText().toString().trim();
                    if (keyword.isEmpty()) {
                        Toast.makeText(MainActivity.this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    wordViewModel.searchWords(keyword);
                    Toast.makeText(MainActivity.this, "搜索结果", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
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
    /**
     * 新增单词对话框（完全适配多词性多释义新布局）
     */
    private void showAddWordDialog() {
        // 加载新的dialog布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        // 绑定固定控件
        EditText etEnglish = dialogView.findViewById(R.id.et_english);
        EditText etExample = dialogView.findViewById(R.id.et_example);
        LinearLayout llPosContainer = dialogView.findViewById(R.id.ll_pos_container);
        Button btnAddPos = dialogView.findViewById(R.id.btn_add_pos);

        // 1. 英文自动转小写逻辑（和布局hint对应）
        etEnglish.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString();
                String lowerInput = input.toLowerCase();
                if (!input.equals(lowerInput)) {
                    etEnglish.setText(lowerInput);
                    etEnglish.setSelection(lowerInput.length());
                }
            }
        });

        // 2. 初始化动态控件的核心方法：添加新词性组
        Runnable addNewPosGroup = () -> {
            // 加载词性组子布局
            View posGroupView = LayoutInflater.from(this).inflate(R.layout.item_word_pos_group, llPosContainer, false);
            android.widget.Spinner spPos = posGroupView.findViewById(R.id.sp_pos);
            android.widget.Button btnAddMeaning = posGroupView.findViewById(R.id.btn_add_meaning);
            android.widget.Button btnRemovePos = posGroupView.findViewById(R.id.btn_remove_pos);
            android.widget.LinearLayout llMeaningContainer = posGroupView.findViewById(R.id.ll_meaning_container);

            // 给当前词性默认添加1个释义输入框
            Runnable addNewMeaningItem = () -> {
                View meaningItemView = LayoutInflater.from(this).inflate(R.layout.item_word_meaning_item, llMeaningContainer, false);
                android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                android.widget.Button btnRemoveMeaning = meaningItemView.findViewById(R.id.btn_remove_meaning);

                // 删除释义：至少保留1个
                btnRemoveMeaning.setOnClickListener(v -> {
                    if (llMeaningContainer.getChildCount() > 1) {
                        llMeaningContainer.removeView(meaningItemView);
                    }
                });
                llMeaningContainer.addView(meaningItemView);
            };

            // 初始化1个释义
            addNewMeaningItem.run();

            // 按钮事件
            btnAddMeaning.setOnClickListener(v -> addNewMeaningItem.run());
            btnRemovePos.setOnClickListener(v -> {
                if (llPosContainer.getChildCount() > 1) {
                    llPosContainer.removeView(posGroupView);
                }
            });

            // 添加到总容器
            llPosContainer.addView(posGroupView);
        };

        // 3. 初始化：默认加载1个词性组
        addNewPosGroup.run();
        // 4. 绑定添加新词性按钮
        btnAddPos.setOnClickListener(v -> addNewPosGroup.run());

        // 5. 构建弹窗
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(dialogView)
                .setPositiveButton("确认新增", (dialog, which) -> {
                    // 核心：收集所有输入数据
                    String english = etEnglish.getText().toString().trim();
                    String example = etExample.getText().toString().trim();

                    // 嵌套结构：词性 -> 该词性对应的多个释义
                    java.util.Map<String, java.util.List<String>> posWithMeaningMap = new java.util.HashMap<>();

                    // 遍历所有词性组
                    for (int i = 0; i < llPosContainer.getChildCount(); i++) {
                        View posGroupView = llPosContainer.getChildAt(i);
                        android.widget.Spinner spPos = posGroupView.findViewById(R.id.sp_pos);
                        android.widget.LinearLayout llMeaningContainer = posGroupView.findViewById(R.id.ll_meaning_container);

                        String pos = spPos.getSelectedItem().toString().trim();
                        java.util.List<String> meaningList = new java.util.ArrayList<>();

                        // 遍历当前词性的所有释义
                        for (int j = 0; j < llMeaningContainer.getChildCount(); j++) {
                            View meaningItemView = llMeaningContainer.getChildAt(j);
                            android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                            String meaning = etMeaning.getText().toString().trim();
                            if (!meaning.isEmpty()) {
                                meaningList.add(meaning);
                            }
                        }

                        // 只保留有有效释义的词性
                        if (!meaningList.isEmpty()) {
                            posWithMeaningMap.put(pos, meaningList);
                        }
                    }

                    // 输入校验
                    if (english.isEmpty()) {
                        Toast.makeText(MainActivity.this, "英文单词不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (posWithMeaningMap.isEmpty()) {
                        Toast.makeText(MainActivity.this, "至少填写一个有效词性和释义", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 对接你的ViewModel，传入结构化数据
                    wordViewModel.addCompleteWord(english, example, posWithMeaningMap);
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

            // 设置长按点击事件
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showWordOptionsDialog(wwpm);
                    return true;
                }
            });
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
                tvEnglish = itemView.findViewById(R.id.tv_word_english);
                tvPos = itemView.findViewById(R.id.tv_word_pos);
                tvMeaning = itemView.findViewById(R.id.tv_word_meaning);
                tvExample = itemView.findViewById(R.id.tv_word_example);
            }
        }
    }

    /**
     * 显示单词操作选项对话框（编辑/删除）
     */
    private void showWordOptionsDialog(WordWithPosAndMeaning wwpm) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(wwpm.getWord().getEnglishWord())
                .setItems(new String[]{"编辑", "删除"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // 编辑
                            showEditWordDialog(wwpm);
                            break;
                        case 1: // 删除
                            showDeleteConfirmDialog(wwpm);
                            break;
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(WordWithPosAndMeaning wwpm) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("删除单词")
                .setMessage("确定要删除单词 " + wwpm.getWord().getEnglishWord() + " 吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    wordViewModel.deleteWord(wwpm.getWord());
                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    /**
     * 显示编辑单词对话框
     */
    private void showEditWordDialog(WordWithPosAndMeaning wwpm) {
        // 加载对话框布局
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_word, null);
        // 绑定固定控件
        EditText etEnglish = dialogView.findViewById(R.id.et_english);
        EditText etExample = dialogView.findViewById(R.id.et_example);
        LinearLayout llPosContainer = dialogView.findViewById(R.id.ll_pos_container);
        Button btnAddPos = dialogView.findViewById(R.id.btn_add_pos);

        // 填充现有数据
        etEnglish.setText(wwpm.getWord().getEnglishWord());
        if (wwpm.getWord().getExampleSentence() != null) {
            etExample.setText(wwpm.getWord().getExampleSentence());
        }

        // 英文自动转小写逻辑
        etEnglish.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String input = s.toString();
                String lowerInput = input.toLowerCase();
                if (!input.equals(lowerInput)) {
                    etEnglish.setText(lowerInput);
                    etEnglish.setSelection(lowerInput.length());
                }
            }
        });

        // 初始化动态控件的核心方法：添加新词性组
        Runnable addNewPosGroup = () -> {
            // 加载词性组子布局
            View posGroupView = LayoutInflater.from(this).inflate(R.layout.item_word_pos_group, llPosContainer, false);
            android.widget.Spinner spPos = posGroupView.findViewById(R.id.sp_pos);
            android.widget.Button btnAddMeaning = posGroupView.findViewById(R.id.btn_add_meaning);
            android.widget.Button btnRemovePos = posGroupView.findViewById(R.id.btn_remove_pos);
            android.widget.LinearLayout llMeaningContainer = posGroupView.findViewById(R.id.ll_meaning_container);

            // 给当前词性默认添加1个释义输入框
            Runnable addNewMeaningItem = () -> {
                View meaningItemView = LayoutInflater.from(this).inflate(R.layout.item_word_meaning_item, llMeaningContainer, false);
                android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                android.widget.Button btnRemoveMeaning = meaningItemView.findViewById(R.id.btn_remove_meaning);

                // 删除释义：至少保留1个
                btnRemoveMeaning.setOnClickListener(v -> {
                    if (llMeaningContainer.getChildCount() > 1) {
                        llMeaningContainer.removeView(meaningItemView);
                    }
                });
                llMeaningContainer.addView(meaningItemView);
            };

            // 初始化1个释义
            addNewMeaningItem.run();

            // 按钮事件
            btnAddMeaning.setOnClickListener(v -> addNewMeaningItem.run());
            btnRemovePos.setOnClickListener(v -> {
                if (llPosContainer.getChildCount() > 1) {
                    llPosContainer.removeView(posGroupView);
                }
            });

            // 添加到总容器
            llPosContainer.addView(posGroupView);
        };

        // 清空容器，添加现有词性和释义
        llPosContainer.removeAllViews();
        List<WordPartOfSpeech> posList = wwpm.getPartOfSpeechList();
        List<WordMeaning> meaningList = wwpm.getMeaningList();
        if (posList != null && !posList.isEmpty()) {
            for (WordPartOfSpeech pos : posList) {
                // 添加词性组
                View posGroupView = LayoutInflater.from(this).inflate(R.layout.item_word_pos_group, llPosContainer, false);
                android.widget.Spinner spPos = posGroupView.findViewById(R.id.sp_pos);
                android.widget.Button btnAddMeaning = posGroupView.findViewById(R.id.btn_add_meaning);
                android.widget.Button btnRemovePos = posGroupView.findViewById(R.id.btn_remove_pos);
                android.widget.LinearLayout llMeaningContainer = posGroupView.findViewById(R.id.ll_meaning_container);

                // 设置词性
                for (int i = 0; i < spPos.getAdapter().getCount(); i++) {
                    if (spPos.getAdapter().getItem(i).equals(pos.getPosType())) {
                        spPos.setSelection(i);
                        break;
                    }
                }

                // 给当前词性添加释义输入框
                Runnable addNewMeaningItem = () -> {
                    View meaningItemView = LayoutInflater.from(this).inflate(R.layout.item_word_meaning_item, llMeaningContainer, false);
                    android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                    android.widget.Button btnRemoveMeaning = meaningItemView.findViewById(R.id.btn_remove_meaning);

                    // 删除释义：至少保留1个
                    btnRemoveMeaning.setOnClickListener(v -> {
                        if (llMeaningContainer.getChildCount() > 1) {
                            llMeaningContainer.removeView(meaningItemView);
                        }
                    });
                    llMeaningContainer.addView(meaningItemView);
                };

                // 添加现有释义
                if (meaningList != null && !meaningList.isEmpty()) {
                    for (WordMeaning meaning : meaningList) {
                        View meaningItemView = LayoutInflater.from(this).inflate(R.layout.item_word_meaning_item, llMeaningContainer, false);
                        android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                        android.widget.Button btnRemoveMeaning = meaningItemView.findViewById(R.id.btn_remove_meaning);

                        etMeaning.setText(meaning.getMeaningContent());

                        // 删除释义：至少保留1个
                        btnRemoveMeaning.setOnClickListener(v -> {
                            if (llMeaningContainer.getChildCount() > 1) {
                                llMeaningContainer.removeView(meaningItemView);
                            }
                        });
                        llMeaningContainer.addView(meaningItemView);
                    }
                } else {
                    // 至少添加一个释义
                    addNewMeaningItem.run();
                }

                // 按钮事件
                btnAddMeaning.setOnClickListener(v -> addNewMeaningItem.run());
                btnRemovePos.setOnClickListener(v -> {
                    if (llPosContainer.getChildCount() > 1) {
                        llPosContainer.removeView(posGroupView);
                    }
                });

                // 添加到总容器
                llPosContainer.addView(posGroupView);
            }
        } else {
            // 至少添加一个词性组
            addNewPosGroup.run();
        }

        // 绑定添加新词性按钮
        btnAddPos.setOnClickListener(v -> addNewPosGroup.run());

        // 构建弹窗
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("编辑单词")
                .setView(dialogView)
                .setPositiveButton("保存修改", (dialog, which) -> {
                    // 核心：收集所有输入数据
                    String english = etEnglish.getText().toString().trim();
                    String example = etExample.getText().toString().trim();

                    // 嵌套结构：词性 -> 该词性对应的多个释义
                    java.util.Map<String, java.util.List<String>> posWithMeaningMap = new java.util.HashMap<>();

                    // 遍历所有词性组
                    for (int i = 0; i < llPosContainer.getChildCount(); i++) {
                        View posGroupView = llPosContainer.getChildAt(i);
                        android.widget.Spinner spPos = posGroupView.findViewById(R.id.sp_pos);
                        android.widget.LinearLayout llMeaningContainer = posGroupView.findViewById(R.id.ll_meaning_container);

                        String pos = spPos.getSelectedItem().toString().trim();
                        java.util.List<String> meaningList1 = new java.util.ArrayList<>();

                        // 遍历当前词性的所有释义
                        for (int j = 0; j < llMeaningContainer.getChildCount(); j++) {
                            View meaningItemView = llMeaningContainer.getChildAt(j);
                            android.widget.EditText etMeaning = meaningItemView.findViewById(R.id.et_meaning);
                            String meaning = etMeaning.getText().toString().trim();
                            if (!meaning.isEmpty()) {
                                meaningList1.add(meaning);
                            }
                        }

                        // 只保留有有效释义的词性
                        if (!meaningList1.isEmpty()) {
                            posWithMeaningMap.put(pos, meaningList1);
                        }
                    }

                    // 输入校验
                    if (english.isEmpty()) {
                        Toast.makeText(MainActivity.this, "英文单词不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (posWithMeaningMap.isEmpty()) {
                        Toast.makeText(MainActivity.this, "至少填写一个有效词性和释义", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 对接你的ViewModel，传入结构化数据
                    wordViewModel.updateWord(wwpm.getWord(), english, example, posWithMeaningMap);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}