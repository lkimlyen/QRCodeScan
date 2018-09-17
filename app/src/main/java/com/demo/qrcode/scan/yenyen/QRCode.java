package com.demo.qrcode.scan.yenyen;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

public class QRCode extends RealmObject {

    @PrimaryKey
    private int id;
    private String content;
    private String dateCreate;

    public QRCode() {
    }

    public QRCode(int id, String content, String dateCreate) {
        this.id = id;
        this.content = content;
        this.dateCreate = dateCreate;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    static void delete(Realm realm, int id) {
        QRCode item = realm.where(QRCode.class).equalTo("id", id).findFirst();
        // Otherwise it has been deleted already.
        if (item != null) {
            item.deleteFromRealm();
        }
    }

    static void create(Realm realm, QRCode item) {
        QRCodeList parent = realm.where(QRCodeList.class).findFirst();
        RealmList<QRCode> items = parent.getItemList();
        items.clear();
        realm.copyToRealmOrUpdate(item);
        RealmResults<QRCode> realmResults = realm.where(QRCode.class).sort("id", Sort.DESCENDING).findAll();
        for (QRCode qrCode : realmResults){
            items.add(qrCode);
        }
    }
}
