package com.example.englishwordnotebook.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "word_meaning",
        indices = {@Index(value = "word_id")}, // 外键索引
        foreignKeys = @ForeignKey(
                entity = Word.class,
                parentColumns = "id",
                childColumns = "word_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class WordMeaning {
    @PrimaryKey(autoGenerate = true) // 新增：标记主键
    private long id;
    private long word_id;
    private String meaningContent;

    public WordMeaning() {}

    @Ignore
    public WordMeaning(long word_id, String meaningContent) {
        this.word_id = word_id;
        this.meaningContent = meaningContent;
    }

    // Getter & Setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getWord_id() { return word_id; }
    public void setWord_id(long word_id) { this.word_id = word_id; }
    public String getMeaningContent() { return meaningContent; }
    public void setMeaningContent(String meaningContent) { this.meaningContent = meaningContent; }
}