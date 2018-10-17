package com.demo.qrcode.scan.yenyen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.Sort;

public class ListScanActivity extends AppCompatActivity {
    private final String TAG = ListScanActivity.class.getName();
    @BindView(R.id.rv_history)
    RecyclerView rvHistory;

    @BindView(R.id.cb_delete)
    CheckBox cbDelete;
    private Realm realm = Realm.getDefaultInstance();
    private RecyclerView.LayoutManager mLayoutManager;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_scan);
        ButterKnife.bind(this);
        AdView mAdView = findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("7EC1E06EE9854334195EC438256A9218").build();

        mAdView.loadAd(adRequest);
        adapter = new HistoryAdapter(realm.where(QRCodeList.class).findFirst().getItemList(), new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(QRCode item) {
                CustomDialogResult dialog = new CustomDialogResult();
                dialog.show(getFragmentManager(), TAG);
                dialog.setContent(item.getContent());
            }
        });
        mLayoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(mLayoutManager);
        rvHistory.setAdapter(adapter);
       // rvHistory.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        cbDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Set<Integer> integerSet = new HashSet<>();
                for (QRCode item : adapter.getData()) {
                    integerSet.add(item.getId());
                }
                adapter.selectAll(isChecked, integerSet);
            }
        });

    }

    @OnClick(R.id.img_delete)
    public void delete() {
        RealmHelper.deleteItemsAsync(realm, adapter.getCountersToDelete());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.img_back)
    public void back() {

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        adapter.enableDeletionMode(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.enableDeletionMode(true);
    }
}
