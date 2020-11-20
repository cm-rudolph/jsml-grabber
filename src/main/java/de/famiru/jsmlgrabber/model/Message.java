package de.famiru.jsmlgrabber.model;

import java.time.LocalDateTime;

public class Message {
    private final LocalDateTime timestamp;
    private final int sensorTime;
    private final long totalEnergy;
    private final byte energyScale;
    private final int currentPower;
    private final byte powerScale;
    private long totalFeedInEnergy;
    private byte feedInEnergyScale;

    Message(LocalDateTime timestamp, int sensorTime, long totalEnergy, byte energyScale, int currentPower, byte powerScale,
            long totalFeedInEnergy, byte feedInEnergyScale) {
        this.timestamp = timestamp;
        this.sensorTime = sensorTime;
        this.totalEnergy = totalEnergy;
        this.energyScale = energyScale;
        this.currentPower = currentPower;
        this.powerScale = powerScale;
        this.totalFeedInEnergy = totalFeedInEnergy;
        this.feedInEnergyScale = feedInEnergyScale;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getSensorTime() {
        return sensorTime;
    }

    public long getTotalEnergy() {
        return totalEnergy;
    }

    public byte getEnergyScale() {
        return energyScale;
    }

    public int getCurrentPower() {
        return currentPower;
    }

    public byte getPowerScale() {
        return powerScale;
    }

    public long getTotalFeedInEnergy() {
        return totalFeedInEnergy;
    }

    public byte getFeedInEnergyScale() {
        return feedInEnergyScale;
    }
}
