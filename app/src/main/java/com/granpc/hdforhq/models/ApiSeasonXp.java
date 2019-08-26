package com.granpc.hdforhq.models;

public final class ApiSeasonXp
{
    private final int previousPoints;
    // private final List<ApiSeasonLevel> levels;
    private final int minimumRotationDegrees;
    private final int currentLevelNumber;
    private final int currentPoints;
    private final String name;
    private final int remainingPoints;

    public ApiSeasonXp( int previousPoints, int minimumRotationDegrees, int currentLevelNumber, int currentPoints, String name, int remainingPoints )
    {
        this.previousPoints = previousPoints;
        this.minimumRotationDegrees = minimumRotationDegrees;
        this.currentLevelNumber = currentLevelNumber;
        this.currentPoints = currentPoints;
        this.name = name;
        this.remainingPoints = remainingPoints;
    }

    public int getPreviousPoints()
    {
        return previousPoints;
    }

    public int getMinimumRotationDegrees()
    {
        return minimumRotationDegrees;
    }

    public int getCurrentLevelNumber()
    {
        return currentLevelNumber;
    }

    public int getCurrentPoints()
    {
        return currentPoints;
    }

    public String getName()
    {
        return name;
    }

    public int getRemainingPoints()
    {
        return remainingPoints;
    }
}
