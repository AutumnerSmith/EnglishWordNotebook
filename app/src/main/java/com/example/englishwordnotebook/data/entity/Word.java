package com.example.englishwordnotebook.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(
        tableName = "word",
        indices = {@Index(value = "englishWord")}
)
public class Word {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String englishWord;
    private String exampleSentence;
    private boolean isMastered;
    private long createTime;

    // Room默认无参构造（保留）
    public Word() {}

    // 带参构造：用@Ignore标记，解决多构造函数冲突
    @Ignore
    public Word(String englishWord) {
        this.englishWord = englishWord.trim().toLowerCase();
        this.isMastered = false;
        this.createTime = System.currentTimeMillis();
    }

    // Getter & Setter（保留）
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEnglishWord() { return englishWord; }
    public void setEnglishWord(String englishWord) { this.englishWord = englishWord.trim().toLowerCase(); }
    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { isMastered = mastered; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}