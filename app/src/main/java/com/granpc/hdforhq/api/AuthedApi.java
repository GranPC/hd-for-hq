package com.granpc.hdforhq.api;

import com.granpc.hdforhq.models.ApiWhistlerGame;
import com.granpc.hdforhq.models.ApiWhistlerQuestionSummary;
import com.granpc.hdforhq.models.ApiWhistlerRound;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthedApi
{
    @GET("offair-trivia/start-game")
    Flowable<ApiWhistlerGame> whistlerStartGame();

    @GET("offair-trivia/{gameUuid}")
    Flowable<ApiWhistlerRound> whistlerNextRound( @Path("gameUuid") String gameUuid );

    @GET("offair-trivia/{gameUuid}/answers")
    Flowable<ApiWhistlerQuestionSummary> whistlerSubmitAnswer( @Path("gameUuid") String gameUuid );
}
