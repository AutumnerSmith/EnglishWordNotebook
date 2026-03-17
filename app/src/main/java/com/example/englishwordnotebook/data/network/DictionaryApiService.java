package com.example.englishwordnotebook.data.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DictionaryApiService {

    // 这里使用有道词典API作为示例
    // 实际使用时需要替换为真实的API地址和参数
    @GET("/api/v3/dictionary/en")
    Call<DictionaryResponse> getWordMeaning(
            @Query("word") String word,
            @Query("key") String apiKey
    );
}
