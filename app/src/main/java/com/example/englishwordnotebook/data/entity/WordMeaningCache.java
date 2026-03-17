package com.example.englishwordnotebook.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "word_meaning_cache")
public class WordMeaningCache {

    @PrimaryKey
    @NonNull
    private String word;
    private String meaning;
    private long timestamp;

    public WordMeaningCache(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
        this.timestamp = System.currentTimeMillis();
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
