package com.example.sabordelhimalaya.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Sesión del cliente. Solo se guarda el token y el perfil público.
 * Nunca se almacena la contraseña.
 *
 * Se usa SharedPreferences privado de la app (MODE_PRIVATE). El token no se
 * expone fuera del proceso y se borra al cerrar sesión o al recibir un 401.
 */
public final class SessionManager {
    private static final String PREFS = "sabor_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_ROL = "rol";
    private static final String KEY_DIRECCION = "direccion";

    private static SessionManager instance;

    private final SharedPreferences preferences;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    public static SessionManager get() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager no inicializado");
        }
        return instance;
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public void saveProfile(String usuarioId, String nombre, String login,
                            String correo, String rol, String direccion) {
        preferences.edit()
                .putString(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_LOGIN, login)
                .putString(KEY_CORREO, correo)
                .putString(KEY_ROL, rol)
                .putString(KEY_DIRECCION, direccion)
                .apply();
    }

    public void updateProfile(String usuarioId, String nombre, String correo, String rol) {
        SharedPreferences.Editor editor = preferences.edit()
                .putString(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_CORREO, correo)
                .putString(KEY_ROL, rol);
        editor.apply();
    }

    public void saveDireccion(String direccion) {
        preferences.edit().putString(KEY_DIRECCION, direccion).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getUsuarioId() {
        return preferences.getString(KEY_USUARIO_ID, null);
    }

    public String getNombre() {
        return preferences.getString(KEY_NOMBRE, null);
    }

    public String getLogin() {
        return preferences.getString(KEY_LOGIN, null);
    }

    public String getCorreo() {
        return preferences.getString(KEY_CORREO, null);
    }

    public String getRol() {
        return preferences.getString(KEY_ROL, null);
    }

    public String getDireccion() {
        return preferences.getString(KEY_DIRECCION, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public boolean isCliente() {
        return "CLIENTE".equals(getRol());
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
