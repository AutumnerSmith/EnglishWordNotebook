package com.example.englishwordnotebook.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;

import java.util.List;

/**
 * 单词词性 Dao 接口
 * 支持批量插入、按单词 id 查询/删除，配合 Word 表的级联删除
 */
@Dao
public interface IWordPartOfSpeechDao {

    /**
     * 批量插入词性（新增单词时，同时插入多个词性）
     * @param posList 词性列表
     * @return 插入后的词性主键 id 列表（-1 表示插入失败）
     */
    @Insert
    List<Long> insertWordPartOfSpeechList(List<WordPartOfSpeech> posList);

    /**
     * 按单词 id 查询对应的所有词性
     * @param wordId 单词主键 id
     * @return 词性列表
     */
    @Query("SELECT * FROM word_part_of_speech WHERE word_id = :wordId")
    List<WordPartOfSpeech> getPosListByWordId(long wordId);

    /**
     * 按单词 id 删除对应的所有词性（配合 Word 表级联删除，冗余保障）
     * @param wordId 单词主键 id
     * @return 受影响的行数
     */
    @Query("DELETE FROM word_part_of_speech WHERE word_id = :wordId")
    int deletePosListByWordId(long wordId);

    /**
     * 删除单个词性
     * @param wordPartOfSpeech 词性实体
     * @return 受影响的行数
     */
    @Delete
    int deleteWordPartOfSpeech(WordPartOfSpeech wordPartOfSpeech);
}