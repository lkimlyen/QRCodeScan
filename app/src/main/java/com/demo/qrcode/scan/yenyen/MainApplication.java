package com.demo.qrcode.scan.yenyen;

import android.app.Application;
import android.graphics.Typeface;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.res.ResourcesCompat;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(QRCodeList.class);
                    }})
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.notosans);
    }
}
