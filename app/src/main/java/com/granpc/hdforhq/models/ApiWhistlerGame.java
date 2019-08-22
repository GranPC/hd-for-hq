package com.granpc.hdforhq.models;

import java.util.List;

public final class ApiWhistlerGame
{
    private final String gameUuid;
    private final String status;
    private final int questionNumber;
    private final int questionCount;
    private final List<Boolean> answerResults;
    private final String category;
    // private final List<ApiWhistlerReminder> reminders;

    public ApiWhistlerGame( String gameUuid, String status, int questionNumber, int questionCount,
                            List<Boolean> answerResults, String category )
    {
        this.gameUuid = gameUuid;
        this.status = status;
        this.questionNumber = questionNumber;
        this.questionCount = questionCount;
        this.answerResults = answerResults;
        this.category = category;
    }
}
