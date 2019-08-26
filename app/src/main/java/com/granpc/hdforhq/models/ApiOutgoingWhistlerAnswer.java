package com.granpc.hdforhq.models;

public final class ApiOutgoingWhistlerAnswer
{
    private final String offairAnswerId;

    public ApiOutgoingWhistlerAnswer( String offairAnswerId )
    {
        this.offairAnswerId = offairAnswerId;
    }

    public String getOffairAnswerId()
    {
        return offairAnswerId;
    }
}
