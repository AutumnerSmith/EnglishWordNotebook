package com.example.englishwordnotebook.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.englishwordnotebook.data.entity.WordMeaningCache;

@Dao
public interface IWordMeaningCacheDao {

    @Insert
    void insert(WordMeaningCache wordMeaningCache);

    @Query("SELECT * FROM word_meaning_cache WHERE word = :word")
    WordMeaningCache getByWord(String word);

    @Query("DELETE FROM word_meaning_cache WHERE timestamp < :time")
    void deleteOldCache(long time);
}
