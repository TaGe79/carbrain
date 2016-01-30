package org.tage.pi.car;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tage.pi.car.hardware.PCA9685Driver;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

/**
 * Created by tgergel on 15/01/16.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SteeringServo {
    protected final PCA9685Driver pwmDriver;

    protected int leftPWM = 300;
    protected int straightPWM = 400;
    protected int rightPWM = 500;

    @Setter
    private Consumer<Direction> directionStateChangeHandler;

    @Value("${steering.servo.channel:0}")
    protected int channel;

    @Getter
    @Value("${steering.servo.offset:0}")
    protected int offset;       // fine-tune the servo and store the offset in properties

    @PostConstruct
    protected void init() {
        leftPWM += offset;
        rightPWM += offset;
        straightPWM += offset;

        pwmDriver.setPWMFreq(60);

        calibrate(offset);
    }

    public void turnLeft() {
        pwmDriver.setPWM(channel, 0, leftPWM);

        if ( directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.LEFT);
    }

    public void turnRight() {
        pwmDriver.setPWM(channel, 0, rightPWM);

        if ( directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.RIGHT);
    }

    public void turnStraight() {
        pwmDriver.setPWM(channel, 0, straightPWM);

        if ( directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.NO);
    }

    public void calibrate(int x) {
        pwmDriver.setPWM(channel, 0, straightPWM + x);
    }

    public void setServoPulse(int channel, int pulse) {
        long pulseLength = 1000000;      // 1, 000, 000 us per second
        pulseLength /= 60;               // 60 Hz
        log.info("{} us per period", pulseLength);

        pulseLength /= 4096;             // 12 bits of resolution
        log.info("{} us per bit", pulseLength);

        pulse *= 1000;
        pulse /= pulseLength;
        pwmDriver.setPWM(channel, 0, pulse);
    }
}
