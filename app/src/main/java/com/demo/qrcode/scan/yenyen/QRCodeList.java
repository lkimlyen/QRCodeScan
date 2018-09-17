package com.demo.qrcode.scan.yenyen;

import java.util.Collections;

import io.realm.RealmList;
import io.realm.RealmObject;

public class QRCodeList extends RealmObject {
    @SuppressWarnings("unused")
    private RealmList<QRCode> itemList;

    public RealmList<QRCode> getItemList() {
        return itemList;
    }
}
