package com.github.rico.common;

import com.stratio.jam.api.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.stratio.jam.api.entity.DTCStatus.Status.ACTIVE;
import static com.stratio.jam.api.entity.DTCStatus.Status.INACTIVE;
import static com.stratio.jam.api.entity.Notification.Status.READ;
import static com.stratio.jam.api.entity.Notification.Status.UNREAD;

/**
 * @author Roberto Cortez
 */
@Startup
@Singleton
public class ProvisionTestData {

    private static Logger LOGGER = LoggerFactory.getLogger(ProvisionTestData.class);

    @PersistenceContext(unitName = "jamPu")
    private EntityManager entityManager;

    @PostConstruct
    private void init() {

        LOGGER.info("************************* INSERT TEST DATA **************************************");

        //entityManager.createQuery("DELETE FROM Prediction ").executeUpdate();

        // Setup VEHICLES
        final List<Vehicle> vehicles = new ArrayList<>();
        final Vehicle BMW = Vehicle.builder().id(1).description("BMW").build();
        final Vehicle FERRARI = Vehicle.builder().id(2).description("Ferrari").build();
        final Vehicle LAMBORGHINI = Vehicle.builder().id(3).description("Lamborghini").build();
        vehicles.add(BMW);
        vehicles.add(FERRARI);
        vehicles.add(LAMBORGHINI);
        vehicles.forEach(entityManager::persist);

        // Setup DIAGNOSTIC TROUBLE CODES
        final List<DiagnosticTroubleCode> diagnosticTroubleCodes = new ArrayList<>();
        DiagnosticTroubleCode DTC1 = DiagnosticTroubleCode.builder().code("A")
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .lastStatusDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .lastStatus(ACTIVE).vehicle(BMW).build();
        DiagnosticTroubleCode DTC2 = DiagnosticTroubleCode.builder().code("B")
                .startDate(LocalDateTime.parse("2016-01-02T00:00:00.000"))
                .lastStatusDate(LocalDateTime.parse("2016-01-02T00:00:00.000"))
                .lastStatus(ACTIVE).vehicle(BMW).build();
        DiagnosticTroubleCode DTC3 = DiagnosticTroubleCode.builder().code("C")
                .startDate(LocalDateTime.parse("2016-01-03T00:00:00.000"))
                .lastStatusDate(LocalDateTime.parse("2016-01-04T00:00:00.000"))
                .lastStatus(INACTIVE).vehicle(BMW).build();

        diagnosticTroubleCodes.add(DTC1);
        diagnosticTroubleCodes.add(DTC2);
        diagnosticTroubleCodes.add(DTC3);

        diagnosticTroubleCodes.add(DiagnosticTroubleCode.builder().code("D")
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .lastStatusDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .lastStatus(ACTIVE).vehicle(FERRARI).build());
        diagnosticTroubleCodes.add(DiagnosticTroubleCode.builder().code("E")
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .lastStatusDate(LocalDateTime.parse("2016-01-04T00:00:00.000"))
                .lastStatus(INACTIVE).vehicle(FERRARI).build());

        diagnosticTroubleCodes.forEach(entityManager::persist);

        // Setup WIFI
        final List<Wifi> wifis = new ArrayList<>();
        final Wifi wifi1 = Wifi.builder().id(1).build();
        final Wifi wifi2 = Wifi.builder().id(2).build();
        wifis.add(wifi1);
        wifis.add(wifi2);
        wifis.forEach(entityManager::persist);

        // Setup JAM DEVICE
        final List<JamDevice> jamDevices = new ArrayList<>();
        final JamDevice jamDevice = JamDevice.builder().id(1).wifi(wifi1).build();
        final JamDevice jamDevice2 = JamDevice.builder().id(2).wifi(wifi2).build();
        jamDevices.add(jamDevice);
        jamDevices.add(jamDevice2);
        jamDevices.forEach(entityManager::persist);


        // Setup JAM DEVICE VEHICLES
        final List<JamDeviceVehicle> jamDevicesVehicles = new ArrayList<>();
        jamDevicesVehicles.add(JamDeviceVehicle.builder()
                .vehicle(BMW)
                .status(JamDeviceVehicle.Status.ACTIVE)
                .jamDevice(jamDevice)
                .startDate(LocalDateTime.ofInstant(new Date(1485515318007L).toInstant(), ZoneId.systemDefault()))
                .build());
        jamDevicesVehicles.add(JamDeviceVehicle.builder()
                .vehicle(BMW)
                .status(JamDeviceVehicle.Status.INACTIVE)
                .jamDevice(jamDevice)
                .startDate(LocalDateTime.ofInstant(new Date(1485515278160L).toInstant(), ZoneId.systemDefault()))
                .build());
        jamDevicesVehicles.add(JamDeviceVehicle.builder()
                .vehicle(FERRARI)
                .status(JamDeviceVehicle.Status.INACTIVE)
                .jamDevice(jamDevice)
                .startDate(LocalDateTime.ofInstant(new Date(1485515278160L).toInstant(), ZoneId.systemDefault()))
                .build());
        jamDevicesVehicles.forEach(entityManager::persist);


        // Setup TRIPS
        final List<Trip> trips = new ArrayList<>();
        final Trip trip1 = Trip.builder().vehicle(FERRARI).consumption(0.2D).distance(0.4D).emissions(0.6D).idleTime(1L)
                .startDate(LocalDateTime.parse("2017-01-01T00:00:00.000")).build();
        final Trip trip2 = Trip.builder().vehicle(BMW).consumption(0.2D).distance(0.4D).emissions(0.6D).idleTime(3L)
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000")).build();
        trips.add(trip1);
        trips.add(trip2);
        trips.add(Trip.builder().vehicle(FERRARI).consumption(100.2D).distance(100.625D).emissions(100.6D)
                .idleTime(4L)
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .endDate(LocalDateTime.parse("2016-01-02T00:00:00.000")).build());
        trips.forEach(entityManager::persist);

        // Setup POSITIONS
        final List<Position> positions = new ArrayList<>();
        positions.add(Position.builder().id(1L).latitude(0.1D).longitude(0.2D)
                .timestamp(LocalDateTime.ofInstant(new Date(1485515278160L).toInstant(), ZoneId.systemDefault()))
                .trip(trip1).build());
        positions.add(Position.builder().id(2L).latitude(0.2D).longitude(0.3D)
                .timestamp(LocalDateTime.ofInstant(new Date(1485515318007L).toInstant(), ZoneId.systemDefault()))
                .trip(trip1).build());
        positions.forEach(entityManager::persist);

        // Setup USER
        final List<User> users = new ArrayList<>();
        User admin = User.builder().id(1).username("admin").password("admin").email("admin@stratio.org").status
                (DTCStatus.Status.ACTIVE).build();
        users.add(admin);
        User tomee = User.builder().id(2).username("tomee").password("tomee").email("tomee@stratio.org").status
                (DTCStatus.Status.ACTIVE).build();
        users.add(tomee);
        users.forEach(entityManager::persist);

        // Setup PREDICTIONS
        final List<Prediction> predictions = new ArrayList<>();
        Prediction prediction = Prediction.builder().id(1).code(1).summary("Lorem Ipsulum 1").vehicle(FERRARI).build();
        predictions.add(prediction);
        predictions.add(Prediction.builder().id(2).code(2).summary("Lorem Ipsulum 2").vehicle(FERRARI).build());
        predictions.add(Prediction.builder().id(3).code(3).summary("Lorem Ipsulum 3").vehicle(FERRARI).build());
        predictions.add(Prediction.builder().id(4).code(4).summary("Lorem Ipsulum 4").vehicle(FERRARI).build());
        predictions.forEach(entityManager::persist);

        // Setup NOTIFICATIONS
        final List<Notification> notifications = new ArrayList<>();
        notifications.add(Notification.builder().diagnosticTroubleCode(DTC1)
                .startDate(LocalDateTime.parse("2016-01-01T00:00:00.000"))
                .status(UNREAD).userId(tomee.getId()).vehicle(DTC1.getVehicle()).build());
        notifications.add(Notification.builder().diagnosticTroubleCode(DTC2)
                .startDate(LocalDateTime.parse("2016-01-02T00:00:00.000"))
                .status(UNREAD).userId(tomee.getId()).vehicle(DTC2.getVehicle()).build());
        notifications.add(Notification.builder().diagnosticTroubleCode(DTC2)
                .startDate(LocalDateTime.parse("2016-01-03T00:00:00.000"))
                .status(READ).userId(tomee.getId()).vehicle(DTC3.getVehicle()).build());
        notifications.add(Notification.builder().prediction(prediction)
                .startDate(LocalDateTime.parse("2016-01-03T00:00:00.000"))
                .status(READ).userId(tomee.getId()).vehicle(prediction.getVehicle()).build());
        notifications.forEach(entityManager::persist);

        entityManager.flush();
    }

    @PreDestroy
    private void destroy() {

        LOGGER.info("************************* DELETE TEST DATA **************************************");

        final CriteriaQuery<Object> allNotifications = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allNotifications.select(allNotifications.from(Notification.class)))
                .getResultList()
                .forEach(entityManager::remove);

        final CriteriaQuery<Object> allVehicles = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allVehicles.select(allVehicles.from(Vehicle.class))).getResultList()
                .forEach(entityManager::remove);

        final CriteriaQuery<Object> allJamDevices = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allJamDevices.select(allJamDevices.from(JamDevice.class))).getResultList()
                .forEach(entityManager::remove);

        final CriteriaQuery<Object> allWifi = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allWifi.select(allWifi.from(Wifi.class))).getResultList()
                .forEach(entityManager::remove);

        final CriteriaQuery<Object> allUsers = entityManager.getCriteriaBuilder().createQuery();
        entityManager.createQuery(allUsers.select(allUsers.from(User.class))).getResultList()
                .forEach(entityManager::remove);

    }
}
