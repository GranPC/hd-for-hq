package com.granpc.hdforhq.api;

import com.granpc.hdforhq.models.ApiOutgoingWhistlerAnswer;
import com.granpc.hdforhq.models.ApiWhistlerAnswer;
import com.granpc.hdforhq.models.ApiWhistlerGame;
import com.granpc.hdforhq.models.ApiWhistlerQuestionSummary;
import com.granpc.hdforhq.models.ApiWhistlerRound;

import io.reactivex.Flowable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthedApi
{
    @POST("offair-trivia/start-game")
    Flowable<ApiWhistlerGame> whistlerStartGame();

    @GET("offair-trivia/{gameUuid}")
    Flowable<ApiWhistlerRound> whistlerNextRound( @Path("gameUuid") String gameUuid );

    @POST("offair-trivia/{gameUuid}/answers")
    Flowable<ApiWhistlerQuestionSummary> whistlerSubmitAnswer( @Path("gameUuid") String gameUuid, @Body ApiOutgoingWhistlerAnswer answer );
}
