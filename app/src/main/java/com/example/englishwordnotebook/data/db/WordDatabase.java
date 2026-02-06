package com.example.englishwordnotebook.data.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.englishwordnotebook.data.dao.IWordDao;
import com.example.englishwordnotebook.data.dao.IWordMeaningDao;
import com.example.englishwordnotebook.data.dao.IWordPartOfSpeechDao;
import com.example.englishwordnotebook.data.entity.Word;
import com.example.englishwordnotebook.data.entity.WordMeaning;
import com.example.englishwordnotebook.data.entity.WordPartOfSpeech;

// entities包含所有实体类，version保持1即可，无需修改
@Database(entities = {Word.class, WordPartOfSpeech.class, WordMeaning.class}, version = 1, exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {
    // 单例实例，volatile保证线程安全
    private static volatile WordDatabase INSTANCE;

    // 提供所有Dao的抽象获取方法
    public abstract IWordDao getWordDao();
    public abstract IWordPartOfSpeechDao getWordPartOfSpeechDao();
    public abstract IWordMeaningDao getWordMeaningDao();

    // 线程安全的单例初始化方法（核心新增fallbackToDestructiveMigration()）
    public static WordDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WordDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(), // 必须用Application上下文，避免内存泄漏
                                    WordDatabase.class,
                                    "word_database" // 数据库文件名，不变
                            )
                            .allowMainThreadQueries() // 测试阶段保留，避免主线程查询报错
                            .fallbackToDestructiveMigration() // 核心配置：架构变化时重建数据库
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}