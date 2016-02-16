package org.tage.pi.car.hardware;

import com.pi4j.io.gpio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  protected final String name;

  protected final int triggerGpioPort;

  protected final int echoGpioPort;

  private final GpioController gpio;

  private final static double SOUND_SPEED = 343000.0; // mm/s

  protected GpioPinDigitalOutput triggerPin;

  protected GpioPinDigitalInput echoPin;

  protected volatile long distanceMillimeter = 999;

  @Value("${hcsr04.measurement.timeout:700}")
  protected long measurementTimeout;

  @Autowired
  protected AsyncTaskExecutor te;

  private final Object locker = new Object();

  @PostConstruct
  protected void initialize() throws InterruptedException {
    triggerPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + triggerGpioPort),
        "HCSR04_Trigger", PinState.LOW);
    echoPin = gpio.provisionDigitalInputPin(RaspiPin.getPinByName("GPIO " + echoGpioPort),
        "HCSR04_Echo", PinPullResistance.PULL_DOWN);

    log.info("Distance controller initialized with trigger: {} and echo: {}", triggerPin, echoPin);
  }

  public long getCurrentDistance() {
    synchronized (locker) {
      return distanceMillimeter;
    }
  }

  @Scheduled(initialDelay = 10000, fixedRateString = "${hcsr04.measurement.delay:300}")
  protected void distanceMeasurementTask() throws InterruptedException {

    final Future<Long> future = te.submit(this::measureDistance);

    try {
      synchronized (locker) {
        distanceMillimeter = future.get(measurementTimeout, TimeUnit.MILLISECONDS);
      }
    } catch (Exception e) {
      log.error("Can't finish distance measurement: {}!", name);
      distanceMillimeter = 999;
      Thread.sleep(200);
    }
  }

  long echoReceivedTime = 0;
  long echoDuration = 0;

  private long measureDistance() {
    try {
      triggerPin.low();
      Thread.sleep(0, 2000);
      triggerPin.high();
      Thread.sleep(0, 12000);
      triggerPin.low();
      echoReceivedTime = System.nanoTime();
      //noinspection StatementWithEmptyBody
      while (echoPin.getState() == PinState.HIGH) {
      }
      echoDuration = System.nanoTime() - echoReceivedTime;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return calculateDistance();
  }

  private long calculateDistance() {
    //return Double.valueOf(Math.floor((SOUND_SPEED * (timeInNanoSec * 1.0 / 1000000000.0))) / 2.0).longValue();
    return (long) Math.ceil(echoDuration / 100.0 / 29.0 / 2.0);
  }
}
