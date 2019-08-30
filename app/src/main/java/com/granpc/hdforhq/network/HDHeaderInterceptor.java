package com.granpc.hdforhq.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Be polite

public class HDHeaderInterceptor implements Interceptor
{
    @Override
    public Response intercept( Chain chain) throws IOException
    {
        Request request = chain.request();
        request = request.newBuilder()
            .addHeader( "x-hq-client", "Android/1.40.0 + HD4HQ" )
            .build();

        return chain.proceed( request );
    }
}
