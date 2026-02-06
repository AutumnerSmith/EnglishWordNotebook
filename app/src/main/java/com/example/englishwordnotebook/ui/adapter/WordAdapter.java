package com.example.englishwordnotebook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;
import com.example.englishwordnotebook.data.vo.WordWithPosAndMeaning;

import java.util.List;

/**
 * 适配现有item_word.xml（CardView布局），控件ID完全匹配
 * 展示：英文单词 + 词性+释义 + 是否掌握状态
 */
public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private final Context mContext;
    private List<WordWithPosAndMeaning> mWordList;

    public WordAdapter(Context context) {
        this.mContext = context;
    }

    // 设置数据源，刷新列表（MainActivity中直接调用，不变）
    public void setWordList(List<WordWithPosAndMeaning> wordList) {
        this.mWordList = wordList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载你现有的item_word.xml布局
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_word, parent, false);
        return new WordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        if (mWordList == null || mWordList.isEmpty()) return;
        WordWithPosAndMeaning wwpm = mWordList.get(position);

        // 1. 绑定【英文单词】：匹配ID tv_word_english
        holder.tvWordEnglish.setText(wwpm.getWord().getEnglishWord());

        // 2. 拼接【词性+释义+是否掌握】，绑定到tv_word_mastered
        StringBuilder statusText = new StringBuilder();
        // 拼接词性
        List<WordPartOfSpeech> posList = wwpm.getPartOfSpeechList();
        if (posList != null && !posList.isEmpty()) {
            statusText.append(posList.get(0).getPosType()).append(" · ");
        } else {
            statusText.append("未知词性 · ");
        }
        // 拼接释义
        List<WordMeaning> meaningList = wwpm.getMeaningList();
        if (meaningList != null && !meaningList.isEmpty()) {
            statusText.append(meaningList.get(0).getMeaningContent()).append(" · ");
        } else {
            statusText.append("无释义 · ");
        }
        // 拼接是否掌握状态
        statusText.append(wwpm.getWord().isMastered() ? "已掌握" : "未掌握");

        // 绑定拼接后的文本到状态控件
        holder.tvWordMastered.setText(statusText.toString());
    }

    @Override
    public int getItemCount() {
        return mWordList == null ? 0 : mWordList.size();
    }

    // 视图持有者：完全匹配你item_word.xml的控件ID
    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView tvWordEnglish, tvWordMastered;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定现有布局的两个控件ID，无任何新增
            tvWordEnglish = itemView.findViewById(R.id.tv_word_english);
            tvWordMastered = itemView.findViewById(R.id.tv_word_mastered);
        }
    }
}