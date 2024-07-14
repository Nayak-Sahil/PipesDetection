package com.kotharigroup.kotharipipes;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;


public class LoggingInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        // Log request details
        System.out.println("URL: " + request.url());
        System.out.println("Method: " + request.method());

        RequestBody requestBody = request.body();
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            MediaType contentType = requestBody.contentType();
            Log.d("Content-Type: ", String.valueOf(contentType));
            Log.d("Content-Length: ", String.valueOf(requestBody.contentLength()));
            Log.d("Body: ", String.valueOf(buffer.readUtf8()));
        }

        return chain.proceed(request);
    }
}
