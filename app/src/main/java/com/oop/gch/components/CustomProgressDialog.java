package com.oop.gch.components;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context, String message) {
        super(context);
        setCancelable(false);
        setMessage(message);
    }
}
