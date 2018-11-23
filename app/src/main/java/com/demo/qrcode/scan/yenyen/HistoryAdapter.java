package com.demo.qrcode.scan.yenyen;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;

public class HistoryAdapter extends RealmRecyclerViewAdapter<QRCode, HistoryAdapter.HistoryHolder> {

    private Set<Integer> countersToDelete = new HashSet<>();
    private OnItemClickListener listener;

    public HistoryAdapter(OrderedRealmCollection<QRCode> data, OnItemClickListener listener) {
        super(data, true);
        setHasStableIds(true);
        this.listener = listener;
    }


    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    public void clearCounterDelete() {
        countersToDelete.clear();
    }

    public void selectAll(boolean isChecked, Set<Integer> integerSet) {
        if (isChecked) {
            countersToDelete.addAll(integerSet);
        } else {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        HistoryHolder holder = new HistoryHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HistoryHolder holder, int position) {
        final QRCode obj = getItem(position);
        setDataToViews(holder, obj);
        holder.bind(obj, listener);


    }

    private void setDataToViews(HistoryHolder holder, QRCode item) {
        holder.txtContent.setText(item.getContent());
        holder.txtDate.setText(item.getDateCreate() + "");
        holder.cbDelete.setChecked(countersToDelete.contains(item.getId()));
        holder.cbDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    countersToDelete.add(item.getId());
                } else {
                    if (countersToDelete.contains(item.getId())) {
                        countersToDelete.remove(item.getId());
                    }
                }
            }
        });

    }


    public class HistoryHolder extends RecyclerView.ViewHolder {

        TextView txtContent;
        TextView txtDate;
        CheckBox cbDelete;

        private HistoryHolder(View v) {
            super(v);
            txtContent = (TextView) v.findViewById(R.id.txt_content);
            txtDate = (TextView) v.findViewById(R.id.txt_time);
            cbDelete = (CheckBox) v.findViewById(R.id.cb_delete);
        }

        private void bind(final QRCode item, final OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(QRCode item);
    }

}
