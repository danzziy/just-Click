package my.app.danny.justclick;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    TextView funnyText;
    TextView personalClickCount;
    TextView highScoreLabel;

    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions googleSignInOptions;

    int RC_SIGN_IN = 1000;
    int RC_LEADER=0;
    private static final int RC_ACHIEVEMENT_UI = 9003;

    int clicks =0;
    int timesAppOpened;
    int highScore;
    int times30000;


    private static final String TAG = "MainActivity";

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    int interstitialLoad=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6152952574809449/8697361295");//ca-app-pub-6152952574809449/8697361295
        if(timesAppOpened%interstitialLoad==0)
            mInterstitialAd.loadAd(adRequest);

        funnyText = (TextView) findViewById(R.id.funnyText);

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();
        if(googleApiClient == null && !googleApiClient.isConnected()){
            startSignInIntent();
        }


//getString(R.string.leaderboard_number_of_clicks)

        Button leaderboardButton = findViewById(R.id.button2);
        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GoogleSignIn.getLastSignedInAccount(MainActivity.this)!=null)
                    showLeaderboard();
                else startSignInIntent();
            }
        });

        Button achievementButton = findViewById(R.id.achievementButton);
        achievementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GoogleSignIn.getLastSignedInAccount(MainActivity.this)!=null)
                    showAchievements();
                else startSignInIntent();
            }
        });

        // Find the root view
        // Set the color
        getResources().getColor(android.R.color.background_dark);

        personalClickCount = (TextView) findViewById(R.id.personalClickCount);
        personalClickCount.setTextColor(Color.rgb(255,255,255));

        highScoreLabel = (TextView) findViewById(R.id.highScoreLabel);
        highScoreLabel.setTextColor(Color.rgb(255,255,255));
        SharedPreferences settingsHighScore = getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        SharedPreferences settingsTimesAppOpened = getSharedPreferences("myPrefsKey2", Context.MODE_PRIVATE);
        SharedPreferences settingsTimes30000 = getSharedPreferences("myPrefsKey3", Context.MODE_PRIVATE);

        times30000 = settingsTimes30000.getInt("300000",0);
        highScore = settingsHighScore.getInt("HIGH_SCORE",0);
        timesAppOpened = settingsTimesAppOpened.getInt("TIME_APP_OPENED",0);
        timesAppOpened++;
        times30000=0;
        if(highScore>=300000)
        {
            times30000++;
        }
        else            times30000=0;

        SharedPreferences.Editor editorAppOpened = settingsTimesAppOpened.edit();
        editorAppOpened.putInt("TIME_APP_OPENED",timesAppOpened);
        editorAppOpened.commit();
        SharedPreferences.Editor click300000 = settingsTimes30000.edit();
        click300000.putInt("300000",times30000);

        //  getGameHelper().isSignedIn();
        if(clicks>highScore)
        {
            highScoreLabel.setText(("HIGH SCORE: "+clicks));

            SharedPreferences.Editor editorHighScore = settingsHighScore.edit();
            editorHighScore.putInt("HIGH_SCORE",clicks);
            editorHighScore.commit();
        }
        else
        {
            highScoreLabel.setText("HIGH SCORE: "+highScore);
        }

        if(times30000==0&&highScore>=30000)
        {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .unlock(getString(R.string.jackie));
        }
        if(times30000==0&&highScore>=30001)
        {
            Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .unlock(getString(R.string.achievement_beginners_luck));
        }
        if(googleApiClient.isConnected())
        {
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore("CgkIg_TOs6kMEAIQAA", highScore);
        }
        if(clicks>highScore)
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                    .submitScore("CgkIg_TOs6kMEAIQAA", clicks);
      //  funnyText.setAllCaps(true);

      //  funnyText.setText(String.valueOf(timesAppOpened));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mInterstitialAd.setAdListener(new AdListener(){
            public void onAdLoaded(){
                if (mInterstitialAd.isLoaded()&&timesAppOpened%interstitialLoad==0) {
                    mInterstitialAd.show();
                    Log.d("SHOWED","SHOWED");
                }            }
        });

    }
    int steps=1;
    int temp=0;
    Random r = new Random();
    int randomNum = r.nextInt(500 - 300 + 1) + 300;
    void setFunnyText(Animation in,Animation out){
        if(clicks>=1&&clicks<15&&timesAppOpened==1)
        {
            if(clicks==1)
                funnyText.startAnimation(in);
            funnyText.setText("Well hello there newcomer!");
            if(clicks==14)
                funnyText.startAnimation(out);
        }else if(clicks>=15&&clicks<30&&timesAppOpened==1)
        {
            if(clicks==15)
                funnyText.startAnimation(in);
            funnyText.setText("As you embark on your journey to become the greatest clicker of them all");
            if(clicks==29)
                funnyText.startAnimation(out);
        }else if(clicks>=30&&clicks<45&&timesAppOpened==1)
        {
            if(clicks==30)
            funnyText.startAnimation(in);
            funnyText.setText("You must stay wary of all of the distractions that may come along");
            if(clicks==44)
                funnyText.startAnimation(out);
        }else if(clicks>=45&&clicks<60&&timesAppOpened==1)
        {
            if(clicks==45)
                funnyText.startAnimation(in);
            funnyText.setText("For whenever you leave this screen your score will be reset to zero");
            if(clicks==59)
                funnyText.startAnimation(out);
        }
        else if(clicks>=60&&clicks<75&&timesAppOpened==1)
        {
            if(clicks==60)
                funnyText.startAnimation(in);
            funnyText.setText("Anyways, good luck!");
            if(clicks==74)
                funnyText.startAnimation(out);
        }
        else if(clicks>=1&&clicks<15&&timesAppOpened==2)
        {
            if(clicks==1)
                funnyText.startAnimation(in);
            funnyText.setText("Surprised?");
            if(clicks==14)
                funnyText.startAnimation(out);
        }
        else if(clicks>=15&&clicks<30&&timesAppOpened==2)
        {
            if(clicks==15)
                funnyText.startAnimation(in);
            funnyText.setText("Did you think you were going to see the end of me,");
            if(clicks==29)
                funnyText.startAnimation(out);
        }
        else if(clicks>=30&&clicks<45&&timesAppOpened==2)
        {
            if(clicks==30)
                funnyText.startAnimation(in);
            funnyText.setText("After only one visit? Well you'll see me more I promise");
            if(clicks==44)
                funnyText.startAnimation(out);
        }
        else if(clicks>=45&&clicks<60&&timesAppOpened==2)
        {
            if(clicks==45)
                funnyText.startAnimation(in);
            funnyText.setText("But in all seriousness I can't do this forever");
            if(clicks==69)
                funnyText.startAnimation(out);
        }else if(clicks>=20&&clicks<35&&timesAppOpened%21==0)
        {
            if(clicks==20)
                funnyText.startAnimation(in);
            funnyText.setText("What's 9 + 10...");
            if(clicks==34)
                funnyText.startAnimation(out);
        }
        else if(clicks>=999&&clicks<1100&&timesAppOpened%3==0)
        {
            if(clicks==999)
                funnyText.startAnimation(in);
            funnyText.setText("Why would you do this to yourself?");
            if(clicks==1099)
                funnyText.startAnimation(out);
        } else if(clicks>=1111&&clicks<1200&&timesAppOpened%3==0)
        {
            if(clicks==1111)
                funnyText.startAnimation(in);
            funnyText.setText("Just give up you dont have to do this anymore");
            if(clicks==1199)
                funnyText.startAnimation(out);
        }
        else if((clicks>=randomNum&&clicks<randomNum+15)&&timesAppOpened%17==0)
        {
            if(clicks==randomNum) {
                funnyText.startAnimation(in);
            }
            funnyText.setText("WOW! You sure did come a long way haven't ya");
            if(clicks==randomNum+14) {
                funnyText.startAnimation(out);
            }
        }
        else if((clicks>=randomNum+15&&clicks<randomNum+30)&&timesAppOpened%17==0)
        {
            if(clicks==randomNum+15) {
                funnyText.startAnimation(in);
            }
            funnyText.setText("It would be a shame if...");
            if(clicks==randomNum+29) {
                funnyText.startAnimation(out);
            }
        }
        else if((clicks>=randomNum+30&&clicks<randomNum+50)&&timesAppOpened%17==0)
        {
            if(clicks==randomNum+30) {
                funnyText.startAnimation(in);
            }
            funnyText.setText("It all disappeared");
            if(clicks==randomNum+49) {
                funnyText.startAnimation(out);
            }
        }
        else if(((clicks>=randomNum+50&&clicks<randomNum+54)||(temp>=randomNum+50&&temp<randomNum+60))&&timesAppOpened%17==0)
        {
            if(clicks==randomNum+50){
                funnyText.startAnimation(in);
                temp=clicks;
                clicks=0;
            }
            temp++;
            funnyText.setText("Whoops");

            if(temp==randomNum+59)
                funnyText.startAnimation(out);
        }
        else if((temp>=randomNum+60||(clicks>=randomNum+55&&clicks<randomNum+70))&&timesAppOpened%17==0)
        {
            if(temp==randomNum+60){
                funnyText.startAnimation(in);
                clicks=randomNum+54;
                temp=0;
            }
            funnyText.setText("JK");
            if(clicks==randomNum+69)
                funnyText.startAnimation(out);
        }
        else if(clicks>=666&&clicks<700&&timesAppOpened%6==0){
            if(clicks==666){
                funnyText.startAnimation(in);
            }
            funnyText.setText("O NOOOOOOO!!!!");
            if(clicks==699)
                funnyText.startAnimation(out);
        }
        else if(clicks>=69&&clicks<89&&timesAppOpened%7==0){
            if(clicks==69){
                funnyText.startAnimation(in);
            }
            funnyText.setText("This app is pg");
            if(clicks==88)
                funnyText.startAnimation(out);
        }
        else if(clicks>=777&&clicks<820&&timesAppOpened%77==0){
            if(clicks==777){
                funnyText.startAnimation(in);
            }
            funnyText.setText("WoW, much lucky, Low chances... Bad mEme");
            if(clicks==819)
                funnyText.startAnimation(out);
        } else if(clicks>=10000&&clicks<11000){
            if(clicks==10000){
                funnyText.startAnimation(in);
            }
            funnyText.setText("Im so tired it's 4 in the morning and I'm still typing");
            if(clicks==10999)
                funnyText.startAnimation(out);
        }
        else if(clicks>=randomNum+1000&&clicks<randomNum+1100){
            if(clicks==randomNum+1000){
                funnyText.startAnimation(in);
            }
            funnyText.setText("What am i doing with my life");
            if(clicks==randomNum+1099)
                funnyText.startAnimation(out);
        }
        else if(clicks>=1&&clicks<40&&timesAppOpened%10==0&&timesAppOpened%100!=0){
            if(clicks==1){
                funnyText.startAnimation(in);
            }
            funnyText.setText("CMON YOU CAN DO IT YOU CAN CLICK MORE");
            if(clicks==39)
                funnyText.startAnimation(out);
        }
        else if(clicks>=1&&clicks<40&&timesAppOpened%100==0){
            if(clicks==1){
                funnyText.startAnimation(in);
            }
            funnyText.setText("I can't believe you have opened this app "+ String.valueOf(timesAppOpened)+" times. Thank you :) ");
            if(clicks==39)
                funnyText.startAnimation(out);
        }
        else if(clicks>=118&&clicks<150&&timesAppOpened%4==0){
            if(clicks==118){
                funnyText.startAnimation(in);
            }
            funnyText.setText("Fun fact: There are 118 elements");
            if(clicks==149)
                funnyText.startAnimation(out);
        }
        else if(clicks>=3140&&clicks<3333&&timesAppOpened%6==0){
            if(clicks==3140){
                funnyText.startAnimation(in);
            }
            funnyText.setText("I like pie, lemon pie is my favourite");
            if(clicks==3332)
                funnyText.startAnimation(out);
        }else if(clicks>=150&&clicks<170&&timesAppOpened%6==0){
            if(clicks==150){
                funnyText.startAnimation(in);
            }
            funnyText.setText("And onwards does the hero go!!!");
            if(clicks==169){
                funnyText.startAnimation(out);
            }
        }else if(clicks>=170&&clicks<200&&timesAppOpened%6==0){
            if(clicks==150){
                funnyText.startAnimation(in);
            }
            funnyText.setText("With a strong heart and even stronger fingers the hero countinues to click");
            if(clicks==199){
                funnyText.startAnimation(out);
            }
        }
        else if(clicks>=1&&clicks<30&&timesAppOpened==5){
            if(clicks==1){
                funnyText.startAnimation(in);
            }
            funnyText.setText("In all honesty, I don't know why you continue to click");
            if(clicks==29){
                funnyText.startAnimation(out);
            }
        }
        else if(clicks>=30&&clicks<60&&timesAppOpened==5){
            if(clicks==30){
                funnyText.startAnimation(in);
            }
            funnyText.setText("Don't get me wrong I love the attention (and the ad revenue)");
            if(clicks==59){
                funnyText.startAnimation(out);
            }
        }
        else if(clicks>=60&&clicks<90&&timesAppOpened==5){
            if(clicks==60){
                funnyText.startAnimation(in);
            }
            funnyText.setText("But you are just clicking a loooooot ");
            if(clicks==90){
                funnyText.startAnimation(out);
            }
        }
        else if(clicks>=2000&&clicks<2050&&timesAppOpened%3==0){
            if(clicks==2000){
                funnyText.startAnimation(in);
            }
            funnyText.setText("I'm proud of you, I'm very impressive ");
            if(clicks==2049){
                funnyText.startAnimation(out);
            }
        }
        else if(clicks>=8100&&clicks<8200&&timesAppOpened%11==0){
            if(clicks==8100){
                funnyText.startAnimation(in);
            }
            funnyText.setText("I live in Canada and let me tell you!");
            if(clicks==8199){
                funnyText.startAnimation(out);
            }
        }else if(clicks>=8200&&clicks<8300&&timesAppOpened%11==0){
            if(clicks==8200){
                funnyText.startAnimation(in);
            }
            funnyText.setText("Our healthcare is neither free nor is it any good");
            if(clicks==8299){
                funnyText.startAnimation(in);
            }
        }
        else {
            funnyText.startAnimation(out);
            funnyText.setText("");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        funnyText = (TextView) findViewById(R.id.funnyText);
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(3000);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(3000);

        setFunnyText(in,out);

        int touched=0;
        int eventaction = event.getAction();

        switch (eventaction) {

            case MotionEvent.ACTION_DOWN:
                touched=1;
                break;

            case MotionEvent.ACTION_MOVE:
                touched=0;
                break;
        }
        if(touched==1)
            clicks+=1;
        personalClickCount.setText(String.valueOf(clicks));

        SharedPreferences settings = getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        int highScore = settings.getInt("HIGH_SCORE",0);

        if(clicks>highScore)
        {
            highScoreLabel.setText(("HIGH SCORE: "+String.valueOf(clicks)));

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("HIGH_SCORE",clicks);
            editor.commit();
        }
        else
        {
            highScoreLabel.setText("HIGH SCORE: "+highScore);
        }
        return true;
    }

    @Override
    public void onUserLeaveHint() {
        clicks=0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }else if(requestCode==RC_LEADER){
            //GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //handleResultBoard(result);
        }
    }

    private void showAchievements() {
        Games.getAchievementsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                    }
                });
    }
    private void handleResult(GoogleSignInResult result) {
        if(result.isSuccess())
        {
          GoogleSignInAccount account = result.getSignInAccount();
          updateUI(true);
        }
      else
          updateUI(false);
    }


    public void showLeaderboard() {
       Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent("CgkIg_TOs6kMEAIQAA")
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADER);

                    }
                });
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .submitScore("CgkIg_TOs6kMEAIQAA", highScore);

       // startActivityForResult(Games.getLeaderboardsClient(), RC_LEADER);

    }


    private void updateUI(boolean isLogin) {
        if(isLogin){
        //    Toast.makeText(MainActivity.this," you passed the updateUI",Toast.LENGTH_LONG).show();
        }
        else {
            //    Toast.makeText(MainActivity.this," you failed the updateUI",Toast.LENGTH_LONG).show();
        }
    }
    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
        Toast.makeText(MainActivity.this,"Nigga you in",Toast.LENGTH_LONG).show();
    }
    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            GoogleSignInAccount signedInAccount = task.getResult();
                        } else {
                            // Player will need to sign-in explicitly using via UI
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
