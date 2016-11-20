package org.tage.pi.car.hardware;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.tage.pi.car.Direction;


@Slf4j
public class ArduinoDistanceUnitDriver implements DistanceProvider {

  Serial serial;

  @Getter(onMethod = @__(@Synchronized))
  private volatile int leftDistance = -1;

  @Getter(onMethod = @__(@Synchronized))
  private volatile int rightDistance = -1;


  private final Direction distanceOrientation;

  public ArduinoDistanceUnitDriver(final Direction distanceOrientation) {
    this.distanceOrientation = distanceOrientation;
    serial = SerialFactory.createInstance();

    try {
      serial.addListener(this::distanceReceived);

      serial.open(Serial.DEFAULT_COM_PORT, 9600);
    } catch (SerialPortException e) {
      log.error("Can not open serial connection!", e);
    }
  }

  @Synchronized
  private void distanceReceived(final SerialDataEvent sde) {
    log.debug("Received distance: {}", sde);

    byte[] dataBytes = sde.getData().getBytes();
    log.trace("--> rcv data size: {}", dataBytes.length);

    if (dataBytes.length == 4) {
      leftDistance = (dataBytes[1] << 8 | dataBytes[0]);
      rightDistance = (dataBytes[3] << 8 | dataBytes[2]);

      log.debug("Left distance: {}", leftDistance);
      log.debug("Right distance: {}", rightDistance);
    }
  }

  @Scheduled(initialDelay = 10000, fixedRateString = "${arduino.distance.measurement.delay:300}")
  protected void requestDistance() {
    log.trace("Request distance");
    try {
      serial.writeln("A"); // request distance from all sensors
    } catch (SerialPortException e) {
      log.error("can't request distance {}", e.toString());
    }
  }

  @Override
  @Synchronized
  public int getCurrentDistance() {
    return distanceOrientation == Direction.LEFT ? leftDistance :
        distanceOrientation == Direction.RIGHT ? rightDistance : 0;
  }
}
