package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerAnswerResult
{
    private final int answerId;
    private final String offairAnswerId;
    private final String answer;
    private final Boolean correct;

    public ApiWhistlerAnswerResult( int answerId, String offairAnswerId, String answer,
                                    Boolean correct )
    {
        this.answerId = answerId;
        this.offairAnswerId = offairAnswerId;
        this.answer = answer;
        this.correct = correct;
    }

    public int getAnswerId()
    {
        return answerId;
    }

    public String getOffairAnswerId()
    {
        return offairAnswerId;
    }

    public String getAnswer()
    {
        return answer;
    }

    public Boolean getCorrect()
    {
        return correct;
    }
}
