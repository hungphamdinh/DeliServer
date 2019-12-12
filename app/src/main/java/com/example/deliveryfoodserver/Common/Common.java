package com.example.deliveryfoodserver.Common;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.example.deliveryfoodserver.Model.Request;
import com.example.deliveryfoodserver.Model.User;
import com.example.deliveryfoodserver.Retrofit.IGeoCoordinates;
import com.example.deliveryfoodserver.Retrofit.RetrofitClient;

public class Common {
    public static User currentUser;
    public static Request currentRequest;
    public static final String UPDATE = "Update";
    public static final String DELETE= "Delete";
    public static final int PICK_IMAGE_REQUEST=71;
    public static final String baseUrl="https://maps.googleapis.com";
    public static String changeCodeToStatus(String status) {
        if(status.equals("0"))
            return "Place";
        else if(status.equals("1"))
            return "On the way";
        else
            return "Shipped";
    }
    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }
    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth,int newHeight){
        Bitmap scaleBitmap=Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
        float scaleX=newWidth/(float)bitmap.getWidth();
        float scaleY=newWidth/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;
        Matrix scaleMatrix=new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);
        Canvas canvas=new Canvas(scaleBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaleBitmap;

    }
}
