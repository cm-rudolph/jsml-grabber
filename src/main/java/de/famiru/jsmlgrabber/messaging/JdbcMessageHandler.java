package de.famiru.jsmlgrabber.messaging;

import de.famiru.jsmlgrabber.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class JdbcMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(JdbcMessageHandler.class);
    private final DataSource dataSource;
    private final long meterNumber;
    private final long feedInMeterNumber;
    private final String INSERT_STATEMENT = "INSERT INTO Powerlog (sensortime, hosttime, totalEnergy, energyScale, totalFeedInEnergy, feedInEnergyScale, currentPower, powerScale) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String DAILY_INSERT_STATEMENT = "INSERT INTO Meter_amountLog (Meter_number, value, logdate, scaleFactor) VALUES (?, ?, ?, ?)";
    private LocalDate lastDailyInsert = LocalDate.MIN;

    public JdbcMessageHandler(DataSource dataSource, long meterNumber, long feedInMeterNumber) {
        this.dataSource = dataSource;
        this.meterNumber = meterNumber;
        this.feedInMeterNumber = feedInMeterNumber;
    }

    @Override
    public void handleMessage(Message message) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(INSERT_STATEMENT);
            stmt.setInt(1, message.getSensorTime());
            stmt.setObject(2, message.getTimestamp());
            stmt.setLong(3, message.getTotalEnergy());
            stmt.setByte(4, message.getEnergyScale());
            stmt.setLong(5, message.getTotalFeedInEnergy());
            stmt.setByte(6, message.getFeedInEnergyScale());
            stmt.setInt(7, message.getCurrentPower());
            stmt.setByte(8, message.getPowerScale());
            stmt.executeUpdate();
            if (needToExecuteDailyInsert()) {
                executeDailyInsert(connection, message);
            }
        } catch (SQLException e) {
            log.warn("Caught SQLException", e);
        }
    }

    private boolean needToExecuteDailyInsert() {
        return lastDailyInsert.isBefore(LocalDate.now());
    }

    private void executeDailyInsert(Connection connection, Message message) throws SQLException {
        log.debug("Executing daily insert");
        PreparedStatement stmt = connection.prepareStatement(DAILY_INSERT_STATEMENT);
        stmt.setLong(1, meterNumber);
        stmt.setLong(2, message.getTotalEnergy());
        stmt.setObject(3, message.getTimestamp());
        stmt.setInt(4, calculateScaleFactor(message.getEnergyScale()));
        stmt.executeUpdate();
        stmt.setLong(1, feedInMeterNumber);
        stmt.setLong(2, message.getTotalFeedInEnergy());
        stmt.setObject(3, message.getTimestamp());
        stmt.setInt(4, calculateScaleFactor(message.getFeedInEnergyScale()));
        stmt.executeUpdate();

        lastDailyInsert = LocalDate.now();
    }

    private int calculateScaleFactor(byte energyScale) {
        int result = 1;
        for (int i = energyScale; i < 3; i++) {
            result *= 10;
        }
        return result;
    }
}
