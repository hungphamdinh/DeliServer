package com.example.deliveryfoodserver.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.example.deliveryfoodserver.Common.Common;
import com.example.deliveryfoodserver.Model.Request;
import com.example.deliveryfoodserver.OrderActivity;
import com.example.deliveryfoodserver.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class ListenOrder extends Service implements ChildEventListener {
    private FirebaseDatabase db;
    private DatabaseReference order;
    public ListenOrder() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        db= FirebaseDatabase.getInstance();
        order =db.getReference("Requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        order.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Request request=dataSnapshot.getValue(Request.class);
        String placed="0";
        if(request.getStatus().equals(placed)) {
            showNotification(dataSnapshot.getKey(), request);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    private void showNotification(String key, Request request) {
//        Intent intent=new Intent(getBaseContext(), OrderActivity.class);
//    //    intent.putExtra("userPhone",request.getPhone());
//        PendingIntent conntentIntent=PendingIntent.getActivity(getBaseContext(),0,intent,0);
//        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());
//        builder.setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
//                .setTicker("PDHDev")
//                .setContentInfo("New order")
//                .setContentText("You have new Orer"+ key)
////                .setContentIntent(conntentIntent)
//                .setContentInfo("Info")
//                .setSmallIcon(R.mipmap.ic_launcher);
//        NotificationManager notificationManager=(NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        int randomInt=new Random().nextInt(999-1)+1;
//        notificationManager.notify(randomInt,builder.build());
        int NOTIFICATION_ID = 234;

        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "my_channel_02";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        Intent resultIntent = new Intent(getApplicationContext(), OrderActivity.class);
        //resultIntent.putExtra("userPhone",request.getPhone());
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,resultIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("PDHDev")
                .setContentInfo("Your order was uploaded")
                .setContentText("New Order #"+key+"was update ")
        //        .setContentIntent(resultPendingIntent)
                .setContentInfo("Info")
                .setSmallIcon(R.drawable.ic_menu_camera);
        // builder.setContentIntent(resultPendingIntent);
        int randomInt=new Random().nextInt(999-1)+1;
        notificationManager.notify(randomInt, builder.build());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

}



