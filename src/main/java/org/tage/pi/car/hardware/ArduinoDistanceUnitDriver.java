package org.tage.pi.car.hardware;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by tgergel on 24/04/16.
 */
@Slf4j
public class ArduinoDistanceUnitDriver implements DistanceProvider {

  Serial serial;

  @Getter(onMethod = @__(@Synchronized))
  private volatile int leftDistance = -1;

  @Getter(onMethod = @__(@Synchronized))
  private volatile int rightDistance = -1;

  public ArduinoDistanceUnitDriver() {

    serial = SerialFactory.createInstance();

    try {
      serial.addListener(this::distanceReceived);

      serial.open(Serial.DEFAULT_COM_PORT, 9600);
    } catch (SerialPortException e) {
      log.error("Can not open serial connection!", e);
    }
  }

  private void distanceReceived(final SerialDataEvent sde) {
    log.info("Received distance: {}", sde);

    byte[] dataBytes = sde.getData().getBytes();
    log.debug("--> rcv data size: {}", dataBytes.length);

    if (dataBytes.length == 4) {
      leftDistance = (dataBytes[1] << 8 | dataBytes[0]);
      rightDistance = (dataBytes[3] << 8 | dataBytes[2]);

      log.info("Left distance: {}", leftDistance);
      log.info("Right distance: {}", rightDistance);
    }
  }

  @Synchronized
  @Scheduled(initialDelay = 10000, fixedRateString = "${arduino.distance.measurement.delay:10000}")
  protected void requestDistance() {
    log.debug("Request distance");
    try {
      serial.writeln("A"); // request distance from all sensors
    } catch (SerialPortException e) {
      log.error("can't request distance {}", e.toString());
    }
  }

  @Override
  @Synchronized
  public int getCurrentDistance() {
    return leftDistance;
  }
}
