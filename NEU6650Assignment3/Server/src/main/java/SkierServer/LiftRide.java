package SkierServer;

import lombok.Data;

@Data
public class LiftRide {
    private final int time;
    private final int liftID;
    private final int waitTime;
}

