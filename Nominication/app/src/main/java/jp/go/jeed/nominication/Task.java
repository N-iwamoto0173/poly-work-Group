package jp.go.jeed.nominication;

public class Task {

    //定数
    public static final String TARGET_SELF = "SELF";
    public static final String TARGET_RANDOM = "RANDOM";
    public static final String TARGET_ALL = "ALL";
    public static final String TARGET_PAIR = "PAIR";

    private String content;      // タスクの内容
    private String targetType;   // 対象者

    // コンストラクタ
    public Task(String content, String targetType) {
        this.content = content;
        this.targetType = targetType;
    }

    // ゲッター
    public String getContent() {
        return content;
    }

    public String getTargetType() {
        return targetType;
    }
}
