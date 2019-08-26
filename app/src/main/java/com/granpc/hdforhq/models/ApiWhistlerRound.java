package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerRound
{
    private final String gameUuid;
    private final int questionCount;
    private final ApiWhistlerQuestion question;
    private final int erase1s;

    public ApiWhistlerRound( String gameUuid, int questionCount, ApiWhistlerQuestion question, int erase1s )
    {
        this.gameUuid = gameUuid;
        this.questionCount = questionCount;
        this.question = question;
        this.erase1s = erase1s;
    }

    public String getGameUuid()
    {
        return gameUuid;
    }

    public int getQuestionCount()
    {
        return questionCount;
    }

    public ApiWhistlerQuestion getQuestion()
    {
        return question;
    }

    public int getErase1s()
    {
        return erase1s;
    }
}
