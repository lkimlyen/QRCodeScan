package com.demo.qrcode.scan.yenyen;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CustomDialogResult extends DialogFragment {


    private String content = "";

    public void setContent(String content) {
        this.content = content;
    }

    private OnDismissDialogListener listener;

    public void setListener(OnDismissDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.dialog_result);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final TextView txtContent = dialog.findViewById(R.id.txt_content);
        if (!content.equals(""))
            txtContent.setText(content);
        dialog.findViewById(R.id.btn_browse_website).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    i.setData(Uri.parse(txtContent.getText().toString()));
                    startActivity(i);

                } catch (Exception e) {

                }
            }
        });

        dialog.findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(txtContent.getText().toString(), txtContent.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String escapedQuery = null;
                try {
                    escapedQuery = URLEncoder.encode(txtContent.getText().toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        dialog.findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, txtContent.getText().toString());
                shareIntent.setType("text/plain");
                getActivity().startActivity(Intent.createChooser(shareIntent, getActivity().getText(R.string.send_to)));
            }
        });

        return dialog;
    }

    public interface OnDismissDialogListener {
        void onDismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) listener.onDismiss();
    }
}
