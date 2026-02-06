package com.example.englishwordnotebook.base.util;

// 你的词性枚举，补充getDisplayName()
public enum PartOfSpeech {
    NOUN("名词"),
    VERB("动词"),
    ADJ("形容词"),
    ADV("副词"),
    PREP("介词"),
    CONJ("连词");

    private final String displayName; // 词性显示名称（与arrays.xml中一致）

    PartOfSpeech(String displayName) {
        this.displayName = displayName;
    }

    // 新增：获取显示名称（关键，匹配MainActivity的字符串词性）
    public String getDisplayName() {
        return displayName;
    }

    // 保留原有其他方法（如getShortCode等）
}