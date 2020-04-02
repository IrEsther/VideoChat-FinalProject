package com.estherkom.videochatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, Publisher.PublisherListener {
private static String API_Key = "46642772";
private static String SESSION_IS="2_MX40NjY0Mjc3Mn5-MTU4NTg2NjE1NTU5NH5pd3JqWkZJQWU0TEF3RzNadmp4bUV5d0N-fg";
private static String TOKEN ="T1==cGFydG5lcl9pZD00NjY0Mjc3MiZzaWc9NzQ5YjE4ZjgxNGVlOTA4NmJlOWM4ZDQ5MTg0ZjU2ZGRhNjhhMDQ5YjpzZXNzaW9uX2lkPTJfTVg0ME5qWTBNamMzTW41LU1UVTROVGcyTmpFMU5UVTVOSDVwZDNKcVdrWkpRV1UwVEVGM1J6TmFkbXA0YlVWNWQwTi1mZyZjcmVhdGVfdGltZT0xNTg1ODY2MjQwJm5vbmNlPTAuODUxNTQyMzQ0OTczNzMxNiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTg4NDU4MjM4JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
private static final int RC_VIDEO_APP_PERM = 124;
private FrameLayout mPublisherViewController;
    private FrameLayout mSubViewController;
private com.opentok.android.Session mSession;
    private Publisher mPublisher;
private Subscriber mSubscrible;

private ImageView closeVideoBTN;
private DatabaseReference usersRef;
private String userId ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        closeVideoBTN = findViewById(R.id.close_video_chat_btn);
        closeVideoBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
usersRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
       if (dataSnapshot.child(userId).hasChild("Ringing")){
           usersRef.child(userId).child("Ringing").removeValue();
           if (mPublisher!=null){
               mPublisher.destroy();
           }
           if (mSubscrible!=null){
               mSubscrible.destroy();
           }
           startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
            finish();
       }
        if (dataSnapshot.child(userId).hasChild("Calling")){
            usersRef.child(userId).child("Calling").removeValue();

            if (mPublisher!=null){
                mPublisher.destroy();
            }
            if (mSubscrible!=null){
                mSubscrible.destroy();
            }
            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
            finish();
        }else {
            if (mPublisher!=null){
                mPublisher.destroy();
            }
            if (mSubscrible!=null){
                mSubscrible.destroy();
            }
            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
            finish();
        }

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);

    }
   @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String [] perms = {Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubViewController = findViewById(R.id.sub_container);

mSession = new com.opentok.android.Session.Builder(this,API_Key,SESSION_IS).build();
mSession.setSessionListener(VideoChatActivity.this);
mSession.connect(TOKEN);
        }else {
            EasyPermissions.requestPermissions(this,"Hey, we need your permission for microphone and camera, just allow, please",RC_VIDEO_APP_PERM, perms);
        }
    }


    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
       Log.i(LOG_TAG, "Session Connected");
       mPublisher = new Publisher.Builder(this).build();
       mPublisher.setPublisherListener(VideoChatActivity.this);
       mPublisherViewController.addView(mPublisher.getView());

       if (mPublisher.getView() instanceof GLSurfaceView){
           ( (GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
       }
       mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");

    }
//subscribing
    @Override
    public void onStreamReceived(Session session, Stream stream) {
Log.i(LOG_TAG, "Stream Received");
if (mSubscrible == null){
    mSubscrible = new Subscriber.Builder(this, stream).build();
    mSession.subscribe(mSubscrible);
    mPublisherViewController.addView(mSubscrible.getView());

}
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropping");
        if (mSubscrible!= null){
            mSubscrible = null;
            mSubViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
