package com.example.englishwordnotebook.base.enums;

// 单词业务操作结果枚举（后续新增状态只需添加枚举值，无需修改业务逻辑）
public enum WordOperateStatus {
    SUCCESS("操作成功"),
    WORD_EMPTY("单词不能为空"),
    WORD_DUPLICATE("单词已存在"),
    POS_ADD_FAILED("词性添加失败"),
    MEANING_ADD_FAILED("释义添加失败"),
    WORD_NOT_FOUND("单词不存在"),
    PARAM_INVALID("参数无效"),
    UNKNOWN_ERROR("未知错误");

    private final String desc; // 结果描述（可用于UI层提示）

    WordOperateStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}