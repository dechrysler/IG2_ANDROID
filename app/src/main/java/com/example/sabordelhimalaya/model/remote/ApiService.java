package com.example.sabordelhimalaya.model.remote;

import com.example.sabordelhimalaya.model.dto.ActivateRequest;
import com.example.sabordelhimalaya.model.dto.AuthResponse;
import com.example.sabordelhimalaya.model.dto.CategoryResponse;
import com.example.sabordelhimalaya.model.dto.CreateOrderRequest;
import com.example.sabordelhimalaya.model.dto.DishResponse;
import com.example.sabordelhimalaya.model.dto.LoginRequest;
import com.example.sabordelhimalaya.model.dto.MenuResponse;
import com.example.sabordelhimalaya.model.dto.OrderPreviewResponse;
import com.example.sabordelhimalaya.model.dto.OrderResponse;
import com.example.sabordelhimalaya.model.dto.RegisterRequest;
import com.example.sabordelhimalaya.model.dto.ResendCodeRequest;
import com.example.sabordelhimalaya.model.dto.TodayMenuResponse;
import com.example.sabordelhimalaya.model.dto.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("auth/activate")
    Call<AuthResponse> activate(@Body ActivateRequest body);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/resend-code")
    Call<AuthResponse> resendCode(@Body ResendCodeRequest body);

    @GET("auth/me")
    Call<UserProfile> me();

    @GET("catalog/categories")
    Call<List<CategoryResponse>> categories();

    @GET("catalog/categories/{numero}/dishes")
    Call<List<DishResponse>> dishes(@Path("numero") int numero);

    @GET("menu/today")
    Call<TodayMenuResponse> todayMenu();

    @GET("menu/available")
    Call<List<MenuResponse>> availableMenus();

    @GET("menu/{id}")
    Call<MenuResponse> menu(@Path("id") String id);

    @POST("orders/preview")
    Call<OrderPreviewResponse> preview(@Body CreateOrderRequest body);

    @POST("orders")
    Call<OrderResponse> create(@Body CreateOrderRequest body);

    @GET("orders/my")
    Call<List<OrderResponse>> myOrders();

    @GET("orders/{id}")
    Call<OrderResponse> order(@Path("id") String id);

    @PUT("orders/{id}")
    Call<OrderResponse> update(@Path("id") String id, @Body CreateOrderRequest body);

    @DELETE("orders/{id}")
    Call<OrderResponse> cancel(@Path("id") String id,
                               @Query("observaciones") String observaciones);
}
