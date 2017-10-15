package govern.ny.hack.edu.govern;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity implements Animation.AnimationListener{

    ImageView logoImage;
    Animation logoAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        logoImage = (ImageView) findViewById(R.id.govern_logo);

        logoAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.animation_logo);

        // set animation listener
        logoAnimation.setAnimationListener(this);
        logoImage.startAnimation(logoAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
    Intent i = new Intent(this,MainActivity.class);
    startActivity(i);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
