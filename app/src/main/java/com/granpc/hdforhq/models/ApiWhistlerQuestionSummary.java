package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerQuestionSummary
{
    private final int seasonXp;
    private final int pointsEarned;
    private final Boolean youGotItRight;
    private final List<ApiWhistlerAnswerResult> answerCounts;
    private final String yourOffairAnswerId;
    private final int questionNumber;
    private final List<Boolean> answerResults;
    // private final ApiWhistlerNextQuestion nextQuestion;
    private final Boolean showAd;
    private final ApiWhistlerGameSummary gameSummary;

    public ApiWhistlerQuestionSummary( int seasonXp, int pointsEarned, Boolean youGotItRight,
                                       List<ApiWhistlerAnswerResult> answerCounts,
                                       String yourOffairAnswerId, int questionNumber,
                                       List<Boolean> answerResults, Boolean showAd,
                                       ApiWhistlerGameSummary gameSummary )
    {
        this.seasonXp = seasonXp;
        this.pointsEarned = pointsEarned;
        this.youGotItRight = youGotItRight;
        this.answerCounts = answerCounts;
        this.yourOffairAnswerId = yourOffairAnswerId;
        this.questionNumber = questionNumber;
        this.answerResults = answerResults;
        this.showAd = showAd;
        this.gameSummary = gameSummary;
    }

    public int getSeasonXp()
    {
        return seasonXp;
    }

    public int getPointsEarned()
    {
        return pointsEarned;
    }

    public Boolean getYouGotItRight()
    {
        return youGotItRight;
    }

    public List<ApiWhistlerAnswerResult> getAnswerCounts()
    {
        return answerCounts;
    }

    public String getYourOffairAnswerId()
    {
        return yourOffairAnswerId;
    }

    public int getQuestionNumber()
    {
        return questionNumber;
    }

    public List<Boolean> getAnswerResults()
    {
        return answerResults;
    }

    public Boolean getShowAd()
    {
        return showAd;
    }

    public ApiWhistlerGameSummary getGameSummary()
    {
        return gameSummary;
    }
}
