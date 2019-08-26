package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerAnswer
{
    private final int answerId;
    private final String offairAnswerId;
    private final String text;

    public ApiWhistlerAnswer( int answerId, String offairAnswerId, String text )
    {
        this.answerId = answerId;
        this.offairAnswerId = offairAnswerId;
        this.text = text;
    }

    public int getAnswerId()
    {
        return answerId;
    }

    public String getOffairAnswerId()
    {
        return offairAnswerId;
    }

    public String getText()
    {
        return text;
    }
}
