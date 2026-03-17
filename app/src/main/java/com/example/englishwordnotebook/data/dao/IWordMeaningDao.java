package com.example.englishwordnotebook.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.englishwordnotebook.data.entity.WordMeaning;

import java.util.List;

/**
 * 单词释义 Dao 接口
 * 支持批量插入、按单词 id 查询/删除，配合 Word 表的级联删除
 */
@Dao
public interface IWordMeaningDao {

    /**
     * 批量插入释义（新增单词时，同时插入多个释义）
     * @param meaningList 释义列表
     * @return 插入后的释义主键 id 列表（-1 表示插入失败）
     */
    @Insert
    List<Long> insertWordMeaningList(List<WordMeaning> meaningList);

    /**
     * 按单词 id 查询对应的所有释义
     * @param wordId 单词主键 id
     * @return 释义列表
     */
    @Query("SELECT * FROM word_meaning WHERE word_id = :wordId")
    List<WordMeaning> getMeaningListByWordId(long wordId);

    /**
     * 按单词 id 删除对应的所有释义（配合 Word 表级联删除，冗余保障）
     * @param wordId 单词主键 id
     * @return 受影响的行数
     */
    @Query("DELETE FROM word_meaning WHERE word_id = :wordId")
    int deleteByWordId(long wordId);

    /**
     * 删除单个释义
     * @param wordMeaning 释义实体
     * @return 受影响的行数
     */
    @Delete
    int deleteWordMeaning(WordMeaning wordMeaning);
}