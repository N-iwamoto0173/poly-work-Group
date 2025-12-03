package jp.go.jeed.nominication;

import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.SoundPool;

import android.media.MediaPlayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    // 6つのEditTextを配列で管理
    private EditText[] playerEditTexts;
    private Button btnGameStart;
    private Button btnSimpleStart;
    private Button btnAnonymousStart;

    //BGM用
    private MediaPlayer mediaPlayer;

    // ★SE用★
    private SoundPool soundPool;
    private int startSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration); // 参加者登録画面のXML

        //部品の特定
        btnGameStart = findViewById(R.id.btnGameStart);
        btnSimpleStart = findViewById(R.id.btnSimpleStart);
        btnAnonymousStart = findViewById(R.id.btnAnonymousStart);

        // 1. Viewの初期化（XMLのIDとJavaのオブジェクトを紐づけ）
        playerEditTexts = new EditText[] {
                findViewById(R.id.player1),
                findViewById(R.id.player2),
                findViewById(R.id.player3),
                findViewById(R.id.player4),
                findViewById(R.id.player5),
                findViewById(R.id.player6)
        };

        // ★ BGMの初期化と再生 ★
        initBGM();

        // ★サウンド初期化メソッドの呼び出しを追加★
        initSounds();

        // 2. ボタンにクリックリスナーを設定
        btnGameStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startGame()を呼ぶ前にSEを鳴らす
                soundPool.play(startSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                startGame(false);
            }
        });
        // ★ 新しいボタンにリスナーを設定 ★
        btnSimpleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SE再生
                 soundPool.play(startSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                showNumberSelectDialog();
            }
        });

        // ★ 匿名スタートボタンのリスナー ★
        btnAnonymousStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnonymousGame();
            }
        });

    }

    // ★サウンド初期化メソッドを追加★
    private void initSounds() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(1)
                .build();

        // 効果音ファイルを読み込み
        startSoundId = soundPool.load(this, R.raw.puyo, 1);
    }

    // ★ BGMの初期化と再生メソッド ★
    private void initBGM() {
        // R.raw.bgm_loop は、奈緒が準備したBGMファイル名に置き換えてね！
        mediaPlayer = MediaPlayer.create(this, R.raw.happyhoureverytime);
        mediaPlayer.setLooping(true); // ★ BGMをループ再生する設定 ★
        mediaPlayer.start();          // ★ 再生開始 ★
    }

    // ★ ライフサイクルメソッド (BGMの一時停止/再開/解放) ★

    // 画面が非表示になる時に一時停止
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // BGMを一時停止
        }
    }

    // 画面が再び表示された時に再生再開
    @Override
    protected void onResume() {
        super.onResume();
        // ★ 画面表示時にBGMが停止していたら再開 ★
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            // この画面に戻ってきたら再開
            mediaPlayer.start();
        }
    }

    // Activityが終了するときに音源を解放
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ★ 人数選択ダイアログを表示するメソッド ★
    private void showNumberSelectDialog() {
        // 2人から6人までを選択肢にする
        final String[] numberOptions = {"2人", "3人", "4人", "5人", "6人"};

        new AlertDialog.Builder(this)
                .setTitle("参加人数を選択してください")
                .setItems(numberOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // which は、選択された項目のインデックス (0=2人, 1=3人, ...)
                        int selectedNumber = which + 2; // 選択された人数 (2, 3, 4, 5, 6)

                        // ダミーの参加者リストを生成してスタートする
                        startSimpleGame(selectedNumber);
                    }
                })
                .setNegativeButton("キャンセル", null) // キャンセルボタン
                .show();
    }
    // ★ ダミー参加者リストを生成してスタートする専用メソッド ★
    private void startSimpleGame(int count) {
        ArrayList<String> dummyParticipants = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            dummyParticipants.add("参加者" + i); // 例: 参加者1, 参加者2, ...
        }

        // 画面遷移メソッド
        String[] participantArray = dummyParticipants.toArray(new String[0]);
        proceedToMainActivity(participantArray);
    }

    private void startGame(boolean skipMinCheck) {  // ★ 修正：チェックを行うかどうかを引数で受け取るように変更 ★
            // 3. 参加者名を取得し、2人以上であるかのチェックを行う
            ArrayList<String> participants = new ArrayList<>();

            for (EditText et : playerEditTexts) {
                String name = et.getText().toString().trim();
                if (!name.isEmpty()) {
                    participants.add(name); // 入力がある名前だけをリストに追加
                }
            }

            // 最小人数（2人）のチェック (skipMinCheck が false の場合のみ、このチェックを発動)
            if (!skipMinCheck && participants.size() < 2) {
                Toast.makeText(this, "ゲーム開始には2人以上の入力が必要です！", Toast.LENGTH_SHORT).show();
                return;
            }

        // 4. 画面遷移
        String[] participantArray = participants.toArray(new String[0]);
        proceedToMainActivity(participantArray);
        }

    // ★ 匿名モードでゲームを開始するメソッド ★
    private void startAnonymousGame() {
        // SE再生
        soundPool.play(startSoundId, 1.0f, 1.0f, 1, 0, 1.0f);

        // 匿名モードであることを示す特別な配列を渡す
        // ※ ["ANONYMOUS"] という特殊な文字列の配列を渡し、1人だけの参加者として扱う
        String[] anonymousFlag = new String[]{"ANONYMOUS"};

        // 画面遷移
        proceedToMainActivity(anonymousFlag);
    }

    // ★ 画面遷移処理 ★
    private void proceedToMainActivity(String[] participantArray) {
        // 1. BGMの停止・解放
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 2. MainActivityへデータを渡して移動
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.putExtra("PARTICIPANTS_NAMES", participantArray);
        startActivity(intent);
    }

    }



