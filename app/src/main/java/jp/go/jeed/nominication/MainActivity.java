package jp.go.jeed.nominication;

import static jp.go.jeed.nominication.Task.TARGET_ALL;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.media.SoundPool;
import android.media.AudioAttributes;

public class MainActivity extends AppCompatActivity {
    private String[] participants; // 参加者名を格納する配列
    private List<Task> taskList; // タスクリスト
    private int currentTurnIndex = 0; // 現在のターンのインデックス
    private Random random = new Random();

    // ★SoundPool関連(ルーレットSE用)の変数★
    private SoundPool soundPool;
    private int spinSoundId;
    private int streamId; // 再生中の音を管理するID
    // ★ 拍手SE用の変数 ★
    private int clapSoundId;
    private int clickSoundId;

    // Viewの宣言
    private ImageView imgRouletteWheel;
    private Button btnSpin;
    private Button btnBack;
    private TextView txtTurn;
    private TextView txtWho;
    private TextView txtWhat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // ★サウンド初期化メソッドの呼び出しを追加★
        initSounds();

        // Viewの初期化
        imgRouletteWheel = findViewById(R.id.imgRoulette); // ルーレット
        btnSpin = findViewById(R.id.btnspin);
        txtTurn = findViewById(R.id.txtTurn);
        txtWho = findViewById(R.id.txtWho);
        txtWhat = findViewById(R.id.txtWhat);
        btnBack = findViewById(R.id.btnBack);

        // データの受け取り
        participants = getIntent().getStringArrayExtra("PARTICIPANTS_NAMES");
        initializeTaskList(); // タスクリストを作成

        // ★ 匿名モードの判定フラグを定義 ★
        final boolean isAnonymousMode = (participants != null && participants.length == 1 && participants[0].equals("ANONYMOUS"));


        // 匿名モードの場合のView非表示
        if (isAnonymousMode) {
            // ターン表示と「誰が」表示を完全に隠す
            txtTurn.setVisibility(View.GONE);
//            txtWho.setVisibility(View.GONE);
        } else {
            // 初期ターンの表示を更新 (通常モードのみ)
            updateTurnDisplay();
        }
        // Nullチェック/人数チェック
        if (!isAnonymousMode && (participants == null || participants.length < 2)) {
            // データが不正で、かつ登録はあったけど1人だった場合
            Toast.makeText(this, "参加者データが不正です", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        // ボタンにリスナーを設定
        btnSpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSpin.setEnabled(false); // スピン中はボタンを無効化
                startSpin(isAnonymousMode);
            }
        });

        // ★ メンバー変更ボタンのリスナー ★
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SE再生
                soundPool.play(clickSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                // 参加者登録画面 (RegisterActivity) へ戻る
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    // ターンの表示を更新
    private void updateTurnDisplay() {
        if (participants != null && participants.length > 0) {
            String currentName = participants[currentTurnIndex];
            txtTurn.setText("NEXT👉" + currentName);
        }
    }

    private void moveToNextTurn() {
        // ターンのインデックスを更新（周回処理）
        currentTurnIndex = (currentTurnIndex + 1) % participants.length;
        updateTurnDisplay();
    }

    // ★サウンド初期化メソッドを追加★
    private void initSounds() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1) // 同時に鳴らせる音の数
                .build();

        // 効果音ファイルを読み込み（ルーレットSE）
        spinSoundId = soundPool.load(this, R.raw.drum, 1);
        // ★ 拍手SE★
        clapSoundId = soundPool.load(this, R.raw.clap, 1);
        //戻るボタンSE
        clickSoundId = soundPool.load(this, R.raw.backbtn, 1);
    }

    // ★引数を受け取るように定義を変更★
    private void startSpin(final boolean isAnonymousMode) {
        // 1. 停止するタスクをランダムに選ぶ
        int selectedTaskIndex = random.nextInt(taskList.size());
        final Task selectedTask = taskList.get(selectedTaskIndex); // finalを付けて、Listener内でも使えるようにする

        // 2. 停止角度を計算 (ルーレットの分割数に合わせて計算)
        float degreesPerSection = 360f / taskList.size();
        float targetRotation = degreesPerSection * selectedTaskIndex; // 停止位置までの角度

        float adjustment = 0f; // ズレに応じて 1f ~ 18f 程度の値を試す
        // 5周以上回るようにランダムな角度を加え、必ず一回転以上させる
        float finalTargetDegree = 360f * 5 + (360f - targetRotation); // 上にあるポインターに角度調整

        // ★スピン音の再生★
        streamId = soundPool.play(spinSoundId, 1.0f, 1.0f, 1, 0, 1.0f); // -1: ループ再生

        // 回転アニメーション (中央回転)
        RotateAnimation rotate = new RotateAnimation(
                0,
                finalTargetDegree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f
        );

        rotate.setDuration(4500); // 4.5秒かけて回す
        rotate.setFillAfter(true); // 停止位置で固定

        // 3. アニメーション終了時の処理を設定
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // ★ 拍手SEの再生 ★
                soundPool.play(clapSoundId, 1.0f, 1.0f, 1, 0, 1.0f);

                // 結果を表示
                displayResult(selectedTask);

                // ★ 匿名モードでなければターンを進める ★
                if (!isAnonymousMode) {
                    moveToNextTurn();
                }

                btnSpin.setEnabled(true); // ボタンを再有効化
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // 4. アニメーション開始
        imgRouletteWheel.startAnimation(rotate);
    }

    //  Activityが終了するときに音源を解放
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }


    private void initializeTaskList() {
        taskList = new ArrayList<>();

        //ルーレット項目
        taskList.add(new Task("1杯奢り！", Task.TARGET_PAIR));
        taskList.add(new Task("渾身のモノマネ！", Task.TARGET_SELF));
        taskList.add(new Task("失敗談を語って！", Task.TARGET_SELF));
        taskList.add(new Task("あ～ん", Task.TARGET_PAIR));
        taskList.add(new Task("様付け", Task.TARGET_PAIR));
        taskList.add(new Task("この場で一番〇〇な人を選ぶ", Task.TARGET_RANDOM)); // ランダムに選ばれた人に対してタスクを実行
        taskList.add(new Task("武勇伝を語って！", Task.TARGET_ALL));
        taskList.add(new Task("10分間お嬢様", Task.TARGET_ALL));
        taskList.add(new Task("10分間カタカナ禁止！", Task.TARGET_ALL));
        taskList.add(new Task("乾杯の音頭！", Task.TARGET_ALL));
    }


    private void displayResult(Task task) {
        //変数の初期化
        String whoText = "";
        String whatText = "";
        String currentName = participants[currentTurnIndex];

        // ★ 匿名モード判定の追加 ★
        boolean isAnonymousMode = (currentName.equals("ANONYMOUS"));

        // 通常モードでのみランダムな参加者を決定
        String otherName = "";
        if (!isAnonymousMode) {
            int otherIndex;
            do {
                otherIndex = random.nextInt(participants.length);
            } while (otherIndex == currentTurnIndex); // 自分自身は除外
            otherName = participants[otherIndex];
        }

// 対象者や文言を調整するための処理
        switch (task.getTargetType()) {

            // ===========================================
            // 1. ペアタスク (TARGET_PAIR): 本人と相手役で役割を交換
            // ===========================================
            case Task.TARGET_PAIR:
                String performer, receiver;

                // 匿名モードなら、whoTextは常に空にする
                if (isAnonymousMode) {
                    whoText = "";
                    // 匿名モード用の簡潔な whatText に調整
                    String content = task.getContent();
                    if (content.equals("あ～ん")) {
                        whatText = "誰かを選んで『あ～ん』しよう！";
                    } else if (content.equals("様付け")) {
                        whatText = "今から10分間、\n誰かに『様』付けで話すべし！";
                    } else {
                        whatText = content;
                    }
                } else { // ★ 通常モードのロジック★

                    // どちらのパターンにするかランダムに決定 (true: 本人がやる人 / false: 相手がやる人)
                    if (random.nextBoolean()) {
                        performer = currentName;
                        receiver = otherName;
                    } else {
                        performer = otherName;
                        receiver = currentName;
                    }

                    //それぞれのタスクで表示する文章
                    String content = task.getContent();
                    if (content.equals("1杯奢り！")) {
                        content = content;
                    } else if (content.equals("あ～ん")) {
                        content = "『あ～ん』しよう！";
                    } else if (content.equals("様付け")) {
                        content = "今から10分間、\n『様』付けで\n話すべし！";
                    }


                    whoText = performer + "が " + receiver + "に";
                    whatText = content;
                }
                    break;

                    // ===========================================
                    // 2. 指名タスク (TARGET_RANDOM): 自由指名
                    // ===========================================
                    case Task.TARGET_RANDOM:

                        // 「一番〇〇な人を選ぶ」タスク
                        String[] randomThemes = {
                                "UFOキャッチャー下手そうな", "世界征服してそうな", "歌が上手そうな",
                                "ゆるキャラ好きそうな", "絵が下手そうな", "腹黒そうな",
                                "応援団長似合いそうな", "社畜そうな", "犬っぽい", "猫っぽい",
                                "サンタさん信じてそうな", "霊感ありそうな", "執事/メイドが似合いそうな",
                                "ご主人様が似合いそうな", "学生時代謳歌してそうな", "ナルシストな",
                                "元気な", "ミステリアスな", "クールな", "かわいい",
                                "ディズニープリンセスっぽい", "運動神経がよさそうな", "お金持ちそうな",
                                "優等生っぽい", "身体柔らかそうな", "大食いな", "カラオケ上手そうな",
                                "ロマンチストな", "面白い", "ドSっぽい", "脚が綺麗な",
                                "お酒弱そうな", "育ちがよさそうな", "バンドでヴォーカルやってそうな",
                                "アイドルやってそうな", "ネトゲで無双してそうな"
                        };

                        String selectedTheme = randomThemes[random.nextInt(randomThemes.length)];

                        // 匿名モードなら、whoTextは常に空にする
                        if (isAnonymousMode) {
                            whoText = "お題：" + selectedTheme+"人";
                            whatText = "この中で「一番〇〇な人を発表！\nその理由も語ろう！";
                        } else {

                            whoText = currentName + "のお題\n『" + selectedTheme + "人』";
                            whatText = "この中で「一番〇〇な人を発表！\nその理由も語ろう！";
                        }
                        break;

                    // ===========================================
                    // 3. 実行タスク (TARGET_SELF / TARGET_ALL): 指示のみ
                    // ===========================================
                    case Task.TARGET_SELF:
                    case Task.TARGET_ALL:

                        // 匿名モードなら、whoTextは常に空にする
                        if (isAnonymousMode) {
                            whoText = "";

                            // 最初に whatText に指示内容を設定
                            whatText = task.getContent();

                        } else {

                            // タスク内容に応じて文言を調整
                            if (task.getContent().equals("渾身のモノマネ！")) {
                                whatText = "\n渾身のモノマネを披露";
                            } else if (task.getContent().equals("失敗談を語って！")) {
                                whatText = "\n失敗談を語って";
                            } else if (task.getContent().equals("武勇伝を語って！")) {
                                whatText = "\n武勇伝を語って";
                            } else if (task.getContent().equals("10分間お嬢様")) {
                                whatText = "\n今から10分間、\nお嬢様言葉を使おう";
                            } else if (task.getContent().equals("10分間カタカナ禁止！")) {
                                whatText = "\n今から10分間、\nカタカナ禁止";
                            } else if (task.getContent().equals("乾杯の音頭！")) {
                                whatText = "乾杯！";
                            }

                            // ============= TARGET_SELF の処理 =============
                            if (task.getTargetType().equals(Task.TARGET_SELF)) {
                                // モノマネ、失敗談
                                whoText = currentName + "は";
                                whatText = whatText + "！";

                                // ============= TARGET_ALL の処理 =============
                            } else if (task.getTargetType().equals(Task.TARGET_ALL)) {

                                // 乾杯の音頭の場合
                                if (task.getContent().equals("乾杯の音頭！")) {
                                    whoText = "音頭は" + currentName + "！";
                                    // currentName の「音頭」で乾杯
                                    whatText = "みんなで乾杯！";
                                } else if (task.getContent().equals("武勇伝を語って！")) {
                                    // 武勇伝の場合 (ターン主が実行)
                                    whoText = currentName + "が";
                                    whatText = whatText + "！";

                                } else {
                                    // その他の全員対象タスク (お嬢様、カタカナ禁止)
                                    whoText = "みんなで！";
                                    whatText = whatText + "！";
                                }
                            }
                        }

                        break;

                    default:
                        whoText = "";
                        whatText = task.getContent();
                }


                // テキストビューへの反映
                txtWho.setText(whoText);
                txtWhat.setText(whatText);
        }


    }



