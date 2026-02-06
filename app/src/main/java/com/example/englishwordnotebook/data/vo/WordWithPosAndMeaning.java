package com.example.englishwordnotebook.data.vo;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;

import java.util.List;

// Room关联查询VO类：正确嵌入父实体+关联子实体
public class WordWithPosAndMeaning {
    @Embedded // 标记父实体Word，Room会自动读取其所有字段
    private Word word;

    // 关联词性：父实体列是Word的id，子实体列是word_id
    @Relation(parentColumn = "id", entityColumn = "word_id")
    private List<WordPartOfSpeech> partOfSpeechList;

    // 关联释义：父实体列是Word的id，子实体列是word_id
    @Relation(parentColumn = "id", entityColumn = "word_id")
    private List<WordMeaning> meaningList;

    // Getter & Setter
    public Word getWord() { return word; }
    public void setWord(Word word) { this.word = word; }
    public List<WordPartOfSpeech> getPartOfSpeechList() { return partOfSpeechList; }
    public void setPartOfSpeechList(List<WordPartOfSpeech> partOfSpeechList) { this.partOfSpeechList = partOfSpeechList; }
    public List<WordMeaning> getMeaningList() { return meaningList; }
    public void setMeaningList(List<WordMeaning> meaningList) { this.meaningList = meaningList; }
}