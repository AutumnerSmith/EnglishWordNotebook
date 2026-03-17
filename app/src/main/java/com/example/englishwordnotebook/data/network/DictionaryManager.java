package com.example.englishwordnotebook.data.network;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.englishwordnotebook.data.db.WordDatabase;
import com.example.englishwordnotebook.data.entity.WordMeaningCache;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DictionaryManager {

    private static final String TAG = "DictionaryManager";
    private static DictionaryManager instance;
    private DictionaryApiService apiService;
    private WordDatabase database;

    private DictionaryManager(Context context) {
        apiService = RetrofitClient.getDictionaryApiService();
        database = Room.databaseBuilder(context, WordDatabase.class, "word_database").build();
    }

    public static synchronized DictionaryManager getInstance(Context context) {
        if (instance == null) {
            instance = new DictionaryManager(context);
        }
        return instance;
    }

    public void getWordMeaning(String word, DictionaryCallback callback) {
        // 先从缓存中查询
        Executors.newSingleThreadExecutor().execute(() -> {
            WordMeaningCache cachedMeaning = database.getWordMeaningCacheDao().getByWord(word);
            if (cachedMeaning != null) {
                // 缓存命中
                callback.onSuccess(cachedMeaning.getMeaning());
            } else {
                // 缓存未命中，调用API
                fetchFromApi(word, callback);
            }
        });
    }

    private void fetchFromApi(String word, DictionaryCallback callback) {
        // 这里使用免费的Dictionary API，无需API key
        Call<DictionaryResponse> call = apiService.getWordMeaning(word, "");
        call.enqueue(new Callback<DictionaryResponse>() {
            @Override
            public void onResponse(Call<DictionaryResponse> call, Response<DictionaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DictionaryResponse dictionaryResponse = response.body();
                    String meaning = parseMeaning(dictionaryResponse);
                    
                    // 缓存结果
                    Executors.newSingleThreadExecutor().execute(() -> {
                        WordMeaningCache cache = new WordMeaningCache(word, meaning);
                        database.getWordMeaningCacheDao().insert(cache);
                    });
                    
                    callback.onSuccess(meaning);
                } else {
                    callback.onFailure("查询失败：" + response.message());
                }
            }

            @Override
            public void onFailure(Call<DictionaryResponse> call, Throwable t) {
                Log.e(TAG, "API调用失败", t);
                callback.onFailure("网络错误，请检查网络连接");
            }
        });
    }

    private String parseMeaning(DictionaryResponse response) {
        StringBuilder meaningBuilder = new StringBuilder();
        
        // 添加音标
        if (response.getPhonetic() != null && response.getPhonetic().getText() != null) {
            meaningBuilder.append("音标: ").append(response.getPhonetic().getText()).append("\n\n");
        }
        
        // 添加释义
        if (response.getMeanings() != null && !response.getMeanings().isEmpty()) {
            for (DictionaryResponse.Meaning meaning : response.getMeanings()) {
                meaningBuilder.append(meaning.getPartOfSpeech()).append(":\n");
                if (meaning.getDefinitions() != null && !meaning.getDefinitions().isEmpty()) {
                    for (int i = 0; i < meaning.getDefinitions().size(); i++) {
                        DictionaryResponse.Definition definition = meaning.getDefinitions().get(i);
                        meaningBuilder.append((i + 1)).append(". ").append(definition.getDefinition()).append("\n");
                        if (definition.getExample() != null) {
                            meaningBuilder.append("   例句: " ).append(definition.getExample()).append("\n");
                        }
                    }
                }
                meaningBuilder.append("\n");
            }
        }
        
        return meaningBuilder.toString();
    }

    public interface DictionaryCallback {
        void onSuccess(String meaning);
        void onFailure(String error);
    }
}
