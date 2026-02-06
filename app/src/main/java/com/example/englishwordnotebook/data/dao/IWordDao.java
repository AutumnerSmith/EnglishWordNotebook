package com.example.englishwordnotebook.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.vo.WordWithPosAndMeaning;

import java.util.List;

@Dao
public interface IWordDao {
    @Insert
    long insertWord(Word word);

    @Update
    int updateWord(Word word);

    @Delete
    int deleteWord(Word word);

    @Query("SELECT * FROM word WHERE englishWord = :englishWord LIMIT 1")
    Word getWordByEnglish(String englishWord);

    // 关联查询：查询word表，Room自动关联子表，返回VO类
    @Transaction
    @Query("SELECT * FROM word ORDER BY createTime DESC")
    LiveData<List<WordWithPosAndMeaning>> getAllWordsWithPosAndMeaningOrderByCreateTime();

    @Transaction
    @Query("SELECT * FROM word ORDER BY englishWord ASC")
    LiveData<List<WordWithPosAndMeaning>> getAllWordsWithPosAndMeaningOrderByEnglishWord();
}