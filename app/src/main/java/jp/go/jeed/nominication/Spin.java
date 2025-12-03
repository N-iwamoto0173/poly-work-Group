//package jp.go.jeed.nominication;
//
//import android.os.Bundle;
//import android.widget.ImageView;
//import android.view.animation.RotateAnimation;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.util.List;
//import java.util.Random;
//
//public class Spin extends AppCompatActivity {
//    private ImageView imgRouletteWheel;
//    private List<Task> taskList; // ステップ3で作成したもの
//    private Random random = new Random();
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        imgRouletteWheel = findViewById(R.id.imgRoulette);
//    }
//
//    private void startSpin() {
//        // 1. 停止するタスクをランダムに選ぶ
//        int selectedTaskIndex = random.nextInt(taskList.size());
//        Task selectedTask = taskList.get(selectedTaskIndex);
//
//        // 2. 停止角度を計算
//        // 8分割なら1区画は45度。何周か回してから停止位置に合わせる。
//        float degreesPerSection = 360f / taskList.size();
//        float finalTargetDegree = 360f * 5 + (selectedTaskIndex * degreesPerSection); // 5周＋停止位置
//
//        // 3. アニメーションを作成
//        RotateAnimation rotate = new RotateAnimation(
//                0, // 開始角度
//                finalTargetDegree, // 終了角度
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f, // X軸の回転中心 (中央)
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f  // Y軸の回転中心 (中央)
//        );
//
//        rotate.setDuration(4000); // 4秒かけて回す
//        rotate.setFillAfter(true); // 停止位置で固定する
//
//        // 4. アニメーション開始
//        imgRouletteWheel.startAnimation(rotate);
//
//        // 5. アニメーションが終了したら結果を表示するロジックをここで追加
//        // (AnimationListenerを使う)
//    }
//}
//
