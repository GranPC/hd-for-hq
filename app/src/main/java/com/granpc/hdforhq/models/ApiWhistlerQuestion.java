package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerQuestion
{
    private final String question;
    private final List<ApiWhistlerAnswer> answers;
    private final int questionNumber;
    private final int totalTimeMs;
    private final int timeLeftMs;
    private final Boolean erase1;

    public ApiWhistlerQuestion( String question, List<ApiWhistlerAnswer> answers, int questionNumber, int totalTimeMs, int timeLeftMs, Boolean erase1 )
    {
        this.question = question;
        this.answers = answers;
        this.questionNumber = questionNumber;
        this.totalTimeMs = totalTimeMs;
        this.timeLeftMs = timeLeftMs;
        this.erase1 = erase1;
    }

    public String getQuestion()
    {
        return question;
    }

    public List<ApiWhistlerAnswer> getAnswers()
    {
        return answers;
    }

    public int getQuestionNumber()
    {
        return questionNumber;
    }

    public int getTotalTimeMs()
    {
        return totalTimeMs;
    }

    public int getTimeLeftMs()
    {
        return timeLeftMs;
    }

    public Boolean getErase1()
    {
        return erase1;
    }
}
