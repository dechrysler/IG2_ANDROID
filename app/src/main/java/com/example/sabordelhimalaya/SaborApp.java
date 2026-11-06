package com.example.sabordelhimalaya;

import android.app.Application;
import com.example.sabordelhimalaya.model.remote.ApiClient;
import com.example.sabordelhimalaya.session.SessionManager;

/** Punto de inicialización de la aplicación: sesión y cliente HTTP. */
public class SaborApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
        ApiClient.init(this);
    }
}
