package com.example.sabordelhimalaya.model.remote;

import android.content.Context;

import com.example.sabordelhimalaya.BuildConfig;
import com.example.sabordelhimalaya.session.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Instancia única de Retrofit, OkHttp y Gson para toda la app. */
public final class ApiClient {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private static ApiService service;
    private static Gson gson;

    private ApiClient() {
    }

    public static synchronized void init(Context context) {
        if (service != null) {
            return;
        }
        gson = buildGson();

        OkHttpClient.Builder http = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request.Builder request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json");
                    String token = SessionManager.get().getToken();
                    if (token != null && !token.isEmpty()) {
                        request.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(request.build());
                });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            http.addInterceptor(logging);
        }

        String baseUrl = BuildConfig.API_BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        service = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(http.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService.class);
    }

    public static ApiService api() {
        if (service == null) {
            throw new IllegalStateException("ApiClient no inicializado");
        }
        return service;
    }

    public static Gson gson() {
        return gson;
    }

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(DATE_TIME.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), DATE_TIME);
        }
    }

    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(DATE.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString(), DATE);
        }
    }
}
