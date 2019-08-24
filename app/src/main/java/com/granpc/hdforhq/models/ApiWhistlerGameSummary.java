package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerGameSummary
{
    private final int coinsEarned;
    // private final ApiWhistlerGameSummaryPointsInfo pointsInfo;
    private final int pointsEarned;
    private final int questionsCorrect;
    private final int questionsIncorrect;
    private final int waitTimeMs;
    private final Boolean showAdToUnlock;
    // private final List<ApiWhistlerReminder> reminders;

    public ApiWhistlerGameSummary( int coinsEarned, int pointsEarned, int questionsCorrect,
                                   int questionsIncorrect, int waitTimeMs, Boolean showAdToUnlock )
    {
        this.coinsEarned = coinsEarned;
        this.pointsEarned = pointsEarned;
        this.questionsCorrect = questionsCorrect;
        this.questionsIncorrect = questionsIncorrect;
        this.waitTimeMs = waitTimeMs;
        this.showAdToUnlock = showAdToUnlock;
    }

    public int getCoinsEarned()
    {
        return coinsEarned;
    }

    public int getPointsEarned()
    {
        return pointsEarned;
    }

    public int getQuestionsCorrect()
    {
        return questionsCorrect;
    }

    public int getQuestionsIncorrect()
    {
        return questionsIncorrect;
    }

    public int getWaitTimeMs()
    {
        return waitTimeMs;
    }

    public Boolean getShowAdToUnlock()
    {
        return showAdToUnlock;
    }
}
