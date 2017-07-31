package co.gofun.funanimation.demo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import co.gofun.funanimation.R;

public class SimpleAnimateActivity extends AppCompatActivity {


    ValueAnimator anim;
    ObjectAnimator animator;
    TextView textView;
    MyAnimView myAnimView;

    LinearInterpolator linearInterpolator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_animate);

        textView = (TextView) findViewById(R.id.text_view);
        myAnimView = (MyAnimView) findViewById(R.id.my_anim_view);

//        init();
        init2();
        init3();
        init4();

    }

    private void init4() {
        /*ObjectAnimator anim = ObjectAnimator.ofObject(myAnimView, "color", new ColorEvaluator(),
                "#0000FF", "#FF0000");
        anim.setDuration(5000);
        anim.start();*/
    }

    private void init3() {
        ObjectAnimator moveIn = ObjectAnimator.ofFloat(textView, "translationX", -500f, 0f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(textView, "rotation", 0f, 360f);
        ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(rotate).with(fadeInOut).after(moveIn);
        animSet.setDuration(5000);
        animSet.start();
    }

    private void init2() {
//        animator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f, 1f);
//        animator = ObjectAnimator.ofFloat(textView, "rotation", 0f, 360f);
//        animator = ObjectAnimator.ofFloat(textView, "translationX", 0, -500f, 0);
        animator = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 3f, 1f);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setDuration(5000);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    private void init() {
        anim = ValueAnimator.ofFloat(0f, 5f, 1f);
        anim.setDuration(3000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (float) animation.getAnimatedValue();
                Log.d("TAG", "cuurent value is " + currentValue);
            }
        });


        anim.start();
    }
}
