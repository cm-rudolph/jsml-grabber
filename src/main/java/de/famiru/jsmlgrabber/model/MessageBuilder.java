package de.famiru.jsmlgrabber.model;

import java.time.LocalDateTime;

public class MessageBuilder {
    private LocalDateTime timestamp;
    private int sensorTime;
    private long totalEnergy = 0;
    private byte energyScale = 0;
    private int currentPower = 0;
    private byte powerScale = 0;
    private long totalFeedInEnergy = 0;
    private byte feedInEnergyScale = 0;

    public Message build() {
        return new Message(timestamp, sensorTime, totalEnergy, energyScale, currentPower, powerScale,
                totalFeedInEnergy, feedInEnergyScale);
    }

    public MessageBuilder timestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageBuilder sensorTime(int sensorTime) {
        this.sensorTime = sensorTime;
        return this;
    }

    public MessageBuilder totalEnergy(long totalEnergy, byte scale) {
        this.totalEnergy = totalEnergy;
        this.energyScale = scale;
        return this;
    }

    public MessageBuilder currentPower(int currentPower, byte scale) {
        this.currentPower = currentPower;
        this.powerScale = scale;
        return this;
    }

    public MessageBuilder totalFeedInEnergy(long totalFeedInEnergy, byte scale) {
        this.totalFeedInEnergy = totalFeedInEnergy;
        this.feedInEnergyScale = scale;
        return this;
    }
}
