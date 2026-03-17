package com.example.englishwordnotebook.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Transaction;

import com.example.englishwordnotebook.base.enums.WordOperateStatus;
import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;
import com.example.englishwordnotebook.data.repository.WordRepository;
import com.example.englishwordnotebook.data.vo.WordWithPosAndMeaning;
import com.example.englishwordnotebook.data.dao.IWordDao;
import com.example.englishwordnotebook.data.dao.IWordMeaningDao;
import com.example.englishwordnotebook.data.dao.IWordPartOfSpeechDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 修正方法名匹配、类型适配，解决所有编译错误
 */
public class WordViewModel extends AndroidViewModel {
    // 仓库层实例（匹配最新Repository）
    private final WordRepository wordRepository;
    // 子线程执行器
    private final java.util.concurrent.Executor executor = Executors.newSingleThreadExecutor();
    // 修正类型：匹配Repository返回的WordWithPosAndMeaning
    private LiveData<List<WordWithPosAndMeaning>> currentWordListLiveData;
    // 操作状态LiveData
    private final MutableLiveData<WordOperateStatus> _operateStatusLiveData = new MutableLiveData<>();
    public LiveData<WordOperateStatus> operateStatusLiveData = _operateStatusLiveData;
    // 对外暴露的单词列表（修正类型）
    public LiveData<List<WordWithPosAndMeaning>> wordListLiveData;

    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
        // 初始化加载按时间倒序的单词（修正方法名匹配Repository）
        loadAllWordsByTimeDesc();
    }



    /**
     * 按创建时间倒序加载单词（修正：调用Repository正确方法名）
     */
    public void loadAllWordsByTimeDesc() {
        currentWordListLiveData = wordRepository.getWordsWithPosAndMeaningByCreateTimeDesc();
        wordListLiveData = currentWordListLiveData;
    }

    /**
     * 按英文首字母正序加载单词（修正：调用Repository正确方法名）
     */
    public void loadAllWordsByLetterAsc() {
        currentWordListLiveData = wordRepository.getWordsWithPosAndMeaningByEnglishWordAsc();
        wordListLiveData = currentWordListLiveData;
    }

    /**
     * 按词性筛选单词
     */
    public void loadWordsByPosType(String posType) {
        currentWordListLiveData = wordRepository.getWordsWithPosAndMeaningByPosType(posType);
        wordListLiveData = currentWordListLiveData;
    }

    /**
     * 搜索单词
     */
    public void searchWords(String keyword) {
        currentWordListLiveData = wordRepository.searchWordsWithPosAndMeaning(keyword);
        wordListLiveData = currentWordListLiveData;
    }

    /**
     * 标记单词为已掌握
     */
    public void markWordAsMastered(Word word) {
        if (word == null || word.getId() <= 0) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }
        executor.execute(() -> {
            try {
                word.setMastered(true);
                wordRepository.updateWord(word);
                _operateStatusLiveData.postValue(WordOperateStatus.SUCCESS);
            } catch (Exception e) {
                _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
            }
        });
    }

    /**
     * 新增完整单词（适配Repository参数，修正内部逻辑）
     */
    public void addCompleteWord(String englishWord,
                                String exampleSentence,
                                List<String> posStrList,
                                List<String> meaningStrList) {
        // 基础非空校验
        if (englishWord == null || englishWord.trim().isEmpty()
                || posStrList == null || posStrList.isEmpty()
                || meaningStrList == null || meaningStrList.isEmpty()) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }

        executor.execute(() -> {
            try {
                // 调用Repository新增方法
                long wordId = wordRepository.addCompleteWord(englishWord, exampleSentence, posStrList, meaningStrList);

                // 状态回调
                if (wordId > 0) {
                    _operateStatusLiveData.postValue(WordOperateStatus.SUCCESS);
                    loadAllWordsByTimeDesc(); // 刷新列表
                } else if (wordId == -2) {
                    _operateStatusLiveData.postValue(WordOperateStatus.WORD_DUPLICATE);
                } else {
                    _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
                }
            } catch (Exception e) {
                // 替换printStackTrace为更规范的日志（此处简化为状态回调）
                _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
            }
        });
    }

    /**
     * 新增完整单词（接受Map参数）
     */
    public void addCompleteWord(String english, String example, Map<String, List<String>> posWithMeaningMap) {
        // 基础非空校验
        if (english == null || english.trim().isEmpty() || posWithMeaningMap == null || posWithMeaningMap.isEmpty()) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }

        executor.execute(() -> {
            try {
                // 调用Repository新增方法
                WordOperateStatus status = wordRepository.addCompleteWord(english, example, posWithMeaningMap);

                // 状态回调
                _operateStatusLiveData.postValue(status);
                if (status == WordOperateStatus.SUCCESS) {
                    loadAllWordsByTimeDesc(); // 刷新列表
                }
            } catch (Exception e) {
                // 替换printStackTrace为更规范的日志（此处简化为状态回调）
                _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
            }
        });
    }

    /**
     * （后续可使用）删除单词
     */
    public void deleteWord(Word word) {
        if (word == null || word.getId() <= 0) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }
        executor.execute(() -> {
            // 调用Repository删除方法
            boolean success = wordRepository.deleteWord(word);
            if (success) {
                _operateStatusLiveData.postValue(WordOperateStatus.SUCCESS);
                loadAllWordsByTimeDesc();
            } else {
                _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
            }
        });
    }

    /**
     * （后续可使用）更新单词
     */
    public void updateWord(Word word) {
        if (word == null || word.getId() <= 0) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }
        executor.execute(() -> {
            // 后续补充Repository的update方法调用
            _operateStatusLiveData.postValue(WordOperateStatus.SUCCESS);
            loadAllWordsByTimeDesc();
        });
    }

    /**
     * 更新完整单词（包含多词性多释义）
     */
    public void updateWord(Word oldWord, String english, String example, Map<String, List<String>> posWithMeaningMap) {
        // 基础非空校验
        if (oldWord == null || oldWord.getId() <= 0 || english == null || english.trim().isEmpty()
                || posWithMeaningMap == null || posWithMeaningMap.isEmpty()) {
            _operateStatusLiveData.postValue(WordOperateStatus.PARAM_INVALID);
            return;
        }

        executor.execute(() -> {
            try {
                // 调用Repository更新方法
                boolean success = wordRepository.updateCompleteWord(oldWord, english, example, posWithMeaningMap);

                // 状态回调
                if (success) {
                    _operateStatusLiveData.postValue(WordOperateStatus.SUCCESS);
                    loadAllWordsByTimeDesc(); // 刷新列表
                } else {
                    _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
                }
            } catch (Exception e) {
                // 替换printStackTrace为更规范的日志（此处简化为状态回调）
                _operateStatusLiveData.postValue(WordOperateStatus.UNKNOWN_ERROR);
            }
        });
    }




}

