package com.even.zining.inherit.sound.zbmvre.allpro;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.even.zining.inherit.sound.start.FnnStartFun;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FnnAllPro extends ContentProvider {
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }


    public String getType(Uri uri) {
        return null;
    }


    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        Context context = getContext();
        if (context instanceof Application) {
            Application application = (Application) context;
            FnnStartFun.INSTANCE.init(application, false);
        }
        return true;
        // return false;
    }


    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        try {
            Class<?> helperClass = Class.forName("com.even.zining.inherit.sound.zeros.FnnFan");
            Method method = helperClass.getDeclaredMethod("fnnllc", Context.class, Uri.class);
            return (Cursor) method.invoke(null, getContext(), uri);
        } catch (ClassNotFoundException | NoSuchMethodException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }
}

