package com.example.englishwordnotebook.data.repository;

import android.content.Context;

import com.example.englishwordnotebook.data.dao.IWordDao;
import com.example.englishwordnotebook.data.dao.IWordMeaningDao;
import com.example.englishwordnotebook.data.dao.IWordPartOfSpeechDao;
import com.example.englishwordnotebook.data.db.WordDatabase;
import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;
import com.example.englishwordnotebook.data.vo.WordWithPosAndMeaning;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库层：统一封装数据操作，对外暴露简洁方法给ViewModel
 * 适配Room最新规范、VO类关联查询、词性/释义批量插入
 */
public class WordRepository {
    // 三个表的Dao实例（Room数据库单例，仅初始化一次）
    private final IWordDao wordDao;
    private final IWordPartOfSpeechDao posDao;
    private final IWordMeaningDao meaningDao;

    // 关联查询LiveData（承载Word+词性+释义，供ViewModel观察）
    private final LiveData<List<WordWithPosAndMeaning>> wordsByCreateTimeDesc;
    private final LiveData<List<WordWithPosAndMeaning>> wordsByEnglishWordAsc;

    /**
     * 构造方法：初始化数据库和Dao
     * @param context 上下文（建议传Application上下文，避免内存泄漏）
     */
    public WordRepository(Context context) {
        WordDatabase db = WordDatabase.getInstance(context);
        this.wordDao = db.getWordDao();
        this.posDao = db.getWordPartOfSpeechDao();
        this.meaningDao = db.getWordMeaningDao();

        // 初始化关联查询LiveData（与Dao层修正后的方法匹配）
        this.wordsByCreateTimeDesc = wordDao.getAllWordsWithPosAndMeaningOrderByCreateTime();
        this.wordsByEnglishWordAsc = wordDao.getAllWordsWithPosAndMeaningOrderByEnglishWord();
    }

    /**
     * 核心方法：新增完整单词（同步插入Word主表+词性表+释义表）
     * @param englishWord 英文单词（ViewModel/UI层无需处理大小写，Word实体内部已统一小写去重）
     * @param exampleSentence 例句（可选，可为null/空字符串）
     * @param posStrList 词性字符串列表（如["名词", "动词"]，与arrays.xml中选项一致）
     * @param meaningStrList 释义字符串列表（与词性列表一一对应，长度需相同）
     * @return 结果码：>0 表示成功，返回单词主键ID；<0 表示失败，对应具体异常
     *         -1：参数无效（空值/长度不一致）  -2：单词重复  -3：词性插入失败  -4：释义插入失败
     */
    public long addCompleteWord(String englishWord, String exampleSentence,
                                List<String> posStrList, List<String> meaningStrList) {
        // 1. 基础非空校验（避免无效数据库操作）
        if (englishWord == null || englishWord.trim().isEmpty()
                || posStrList == null || posStrList.isEmpty()
                || meaningStrList == null || meaningStrList.isEmpty()) {
            return -1;
        }
        // 词性和释义列表长度一致性校验（一一对应，避免数据错乱）
        if (posStrList.size() != meaningStrList.size()) {
            return -1;
        }

        // 2. 重复单词校验（精准查询，利用englishWord索引优化性能）
        Word existingWord = wordDao.getWordByEnglish(englishWord);
        if (existingWord != null) {
            return -2; // 单词已存在，返回重复码
        }

        // 3. 插入Word主表（复用原有实体构造，保留小写去重、默认未掌握、自动生成创建时间逻辑）
        Word word = new Word(englishWord);
        word.setExampleSentence(exampleSentence); // 设置可选例句
        long wordId = wordDao.insertWord(word);
        if (wordId <= 0) {
            return -1; // 单词主表插入失败
        }

        // 4. 构造词性实体列表，关联当前单词ID，批量插入
        List<WordPartOfSpeech> posEntityList = new ArrayList<>();
        for (String posType : posStrList) {
            posEntityList.add(new WordPartOfSpeech(wordId, posType));
        }
        List<Long> posInsertIds = posDao.insertWordPartOfSpeechList(posEntityList);
        if (posInsertIds.contains(-1L)) {
            return -3; // 词性表插入失败
        }

        // 5. 构造释义实体列表，关联当前单词ID，批量插入
        List<WordMeaning> meaningEntityList = new ArrayList<>();
        for (String meaningContent : meaningStrList) {
            meaningEntityList.add(new WordMeaning(wordId, meaningContent));
        }
        List<Long> meaningInsertIds = meaningDao.insertWordMeaningList(meaningEntityList);
        if (meaningInsertIds.contains(-1L)) {
            return -4; // 释义表插入失败
        }

        // 6. 所有步骤执行成功，返回单词主键ID
        return wordId;
    }

    /**
     * 获取所有单词（含词性+释义），按创建时间倒序（最新添加在前）
     * @return LiveData<List<WordWithPosAndMeaning>> 供ViewModel观察，自动切换主线程
     */
    public LiveData<List<WordWithPosAndMeaning>> getWordsWithPosAndMeaningByCreateTimeDesc() {
        return wordsByCreateTimeDesc;
    }

    /**
     * 获取所有单词（含词性+释义），按英文首字母正序（A-Z）
     * @return LiveData<List<WordWithPosAndMeaning>> 供ViewModel观察，自动切换主线程
     */
    public LiveData<List<WordWithPosAndMeaning>> getWordsWithPosAndMeaningByEnglishWordAsc() {
        return wordsByEnglishWordAsc;
    }
}