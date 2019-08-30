package com.granpc.hdforhq.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor
{
    private String bearerToken;

    public AuthInterceptor( String bearerToken )
    {
        this.bearerToken = bearerToken;
    }

    @Override
    public Response intercept( Chain chain ) throws IOException
    {
        Request request = chain.request();
        request = request.newBuilder()
            .addHeader( "Authorization", "Bearer " + bearerToken )
            .build();

        return chain.proceed( request );
    }
}
