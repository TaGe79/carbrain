package org.tage.pi.car.hardware;

import com.pi4j.io.gpio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by tgergel on 30/01/16.
 */
@Slf4j
@RequiredArgsConstructor
public class HCSR04USDistance {

    protected final int triggerGpioPort;

    protected final int echoGpioPort;

    private final GpioController gpio;

    private final static double SOUND_SPEED = 343000.0; // mm/s

    protected GpioPinDigitalOutput triggerPin;

    protected GpioPinDigitalInput echoPin;

    protected volatile long distanceMillimeter = 999;

    @Autowired
    AsyncTaskExecutor te;


    @PostConstruct
    protected void initialize() throws InterruptedException {
        triggerPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + triggerGpioPort),
                "HCSR04_Trigger", PinState.LOW);
        echoPin = gpio.provisionDigitalInputPin(RaspiPin.getPinByName("GPIO " + echoGpioPort),
                "HCSR04_Echo", PinPullResistance.PULL_DOWN);

        log.info("Distance controller initialized with trigger: {} and echo: {}", triggerPin, echoPin);
        triggerSensor();
    }

    private void triggerSensor() {
        try {
            Thread.sleep(2);
            triggerPin.high();
            Thread.sleep(0, 12000);
            triggerPin.low();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final static Object distLocker = new Object();

    public long getCurrentDistance() {
        synchronized (distLocker) {
            return distanceMillimeter;
        }
    }

    @Scheduled(initialDelay = 10000, fixedRateString = "${hcsr04.measurement.delay:500}")
    protected void distanceMeasurementTask() throws InterruptedException {

        final Future<Long> future = te.submit(this::measureDistance);

        long localDistance = 0;
        try {
            localDistance = future.get(1, TimeUnit.SECONDS);
            log.info("Measure distance: {}", localDistance);
            synchronized (distLocker) {
                distanceMillimeter = localDistance;
            }
        } catch (Exception e) {
            log.error("Can't finish distance measurement!", e);
            synchronized (distLocker) {
                distanceMillimeter = 50;
            }
        }

    }

    private long measureDistance() {
        triggerSensor();

        while (echoPin.getState() == PinState.LOW) {
        }
        final long echoReceivedTime = System.nanoTime();
        while (echoPin.getState() == PinState.HIGH) {
        }
        return calculateDistance(System.nanoTime() - echoReceivedTime);
    }

    private long calculateDistance(long timeInNanoSec) {
        return Double.valueOf(Math.floor((SOUND_SPEED * (timeInNanoSec * 1.0 / 1000000000.0))) / 2.0).longValue();
//        return Double.valueOf(Math.ceil(timeInNanoSec / 5800.0)).longValue();
    }
}
