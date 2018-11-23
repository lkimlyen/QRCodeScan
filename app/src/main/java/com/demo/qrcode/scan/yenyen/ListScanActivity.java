package com.demo.qrcode.scan.yenyen;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ListScanActivity extends AppCompatActivity {
    private final String TAG = ListScanActivity.class.getName();
    @BindView(R.id.rv_history)
    RecyclerView rvHistory;
    @BindView(R.id.cb_delete)
    CheckBox cbDelete;
    private RealmResults<QRCode> realmList;
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

        realmList = realm.where(QRCode.class).sort("id").findAll();
        adapter = new HistoryAdapter(realmList, new HistoryAdapter.OnItemClickListener() {
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
        if (realmList.size() > 0) {
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
        } else {
            cbDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    cbDelete.setChecked(false);
                }
            });
        }


    }

    @OnClick(R.id.img_delete)
    public void delete() {
        if (realmList.size() > 0) {
            final Collection<Integer> ids = adapter.getCountersToDelete();
            RealmHelper.deleteItemsAsync(realm, ids);
            adapter.clearCounterDelete();
            if (cbDelete.isChecked()){

                cbDelete.setChecked(false);
            }
        }
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
        if (realmList.size() > 0) {
            //   adapter.enableDeletionMode(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (realmList.size() > 0) {
            //  adapter.enableDeletionMode(true);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
