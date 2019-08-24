package com.granpc.hdforhq.api;

import com.granpc.hdforhq.models.ApiWhistlerGame;

import io.reactivex.Flowable;
import retrofit2.http.GET;

public interface AuthedApi
{
    @GET("offair-trivia/start-game")
    Flowable<ApiWhistlerGame> whistlerStartGame();
}
