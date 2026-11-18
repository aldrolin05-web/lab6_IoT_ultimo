package com.example.lab6;

import android.app.Application;
import android.app.LocaleManager;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.firebase.auth.FirebaseAuth;

public class LaboratorioApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // El laboratorio utiliza español tanto en los formularios como en los correos.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(LocaleManager.class).setApplicationLocales(LocaleList.forLanguageTags("es"));
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"));
        }
        FirebaseAuth.getInstance().setLanguageCode("es");
    }
}
