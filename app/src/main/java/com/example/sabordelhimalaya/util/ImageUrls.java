package com.example.sabordelhimalaya.util;

import com.example.sabordelhimalaya.BuildConfig;

public final class ImageUrls {
    private ImageUrls() {
    }

    public static String resolve(String imagenUrl) {
        if (imagenUrl == null || imagenUrl.trim().isEmpty()) {
            return null;
        }
        if (imagenUrl.startsWith("http://") || imagenUrl.startsWith("https://")) {
            return imagenUrl;
        }
        String base = BuildConfig.API_BASE_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (!imagenUrl.startsWith("/")) {
            imagenUrl = "/" + imagenUrl;
        }
        return base + imagenUrl;
    }
}
