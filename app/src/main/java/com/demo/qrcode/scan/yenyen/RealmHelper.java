package com.demo.qrcode.scan.yenyen;


import java.util.Collection;

import butterknife.internal.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmHelper {
    private static Realm mRealm = Realm.getDefaultInstance();
    public static void addItemAsync(QRCode item) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                QRCode.create(realm, item);
            }
        });
    }

    public static RealmResults<QRCode> getAll() {
        return mRealm.where(QRCode.class).findAll();
    }


    public static int getMaxId() {
        int nextId = 0;
        Number id = mRealm.where(QRCode.class).max("id");
        // If id is null, set it to 1, else set increment it by 1
        nextId = (id == null) ? 0 : id.intValue();
        return nextId +1;
    }

    public static void deleteItemAsync(final int id) {
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                QRCode.delete(realm, id);
            }
        });
    }


    public static void deleteItemsAsync(Realm realm, Collection<Integer> ids) {
        // Create an new array to avoid concurrency problem.
        final Integer[] idsToDelete = new Integer[ids.size()];
        ids.toArray(idsToDelete);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Integer id : idsToDelete) {
                    QRCode.delete(realm, id);
                }
            }
        });
    }



}
