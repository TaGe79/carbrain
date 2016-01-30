package org.tage.pi.car;

import com.pi4j.io.gpio.*;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tage.pi.car.hardware.PCA9685Driver;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by tgergel on 22/01/16.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CarMotor {
    protected final PCA9685Driver pwmDriver;

    @Autowired
    @Qualifier("gpio")
    protected GpioController gpio;

    @Autowired
    CarStateAggregator carState;

    @Value("${car.stop-on-collision-warning:false}")
    boolean stopOnCollisionWarning;

    // ===========================================================================
    // Raspberry Pi pin11, 12, 13 and 15 to realize the clockwise/counterclockwise
    // rotation and forward and backward movements
    // ===========================================================================
    static final Pin Motor0_A = RaspiPin.GPIO_00;  // pin11
    static final Pin Motor0_B = RaspiPin.GPIO_01;  // pin12
    static final Pin Motor1_A = RaspiPin.GPIO_02;  // pin13
    static final Pin Motor1_B = RaspiPin.GPIO_03;  // pin15

    // ===========================================================================
    // Set channel 4 and 5 of the servo driver IC to generate PWM, thus
    // controlling the speed of the car
    // ===========================================================================
    static final int EN_M0 = 4;  // servo driver IC CH4
    static final int EN_M1 = 5;  // servo driver IC CH5


    Map<Pin, GpioPinDigitalOutput> motorPins;

    @Setter
    private Consumer<Direction> directionStateChangeHandler;

    @Setter
    private Consumer<Long> speedStateChangeHandler;

    @PostConstruct
    protected void setup() {
        motorPins = Arrays.asList(Motor0_A, Motor1_A, Motor0_B, Motor1_B)
                .stream()
                .map(pin -> new Object[]{pin, gpio.provisionDigitalOutputPin(pin, PinState.LOW)})
                .collect(Collectors.toMap(tuple -> (Pin) tuple[0], tuple -> (GpioPinDigitalOutput) tuple[1]));

        setSpeed(50);
    }

    public void setSpeed(int speed) {
        int convSpeed = speed * 40;
        pwmDriver.setPWM(EN_M0, 0, convSpeed);
        pwmDriver.setPWM(EN_M1, 0, convSpeed);
        if (speedStateChangeHandler != null) speedStateChangeHandler.accept((long) speed);
    }

    public void motor(int motor, MotorDirection direction) {
        if (motor < 0 || motor > 1) throw new IllegalArgumentException("We only have two motors in this car!");

        switch (direction) {
            case BACKWARD:
                motorPins.get(motor == 0 ? Motor0_A : Motor1_A).low();
                motorPins.get(motor == 0 ? Motor0_B : Motor1_B).high();
                break;
            case FORWARD:
                motorPins.get(motor == 0 ? Motor0_A : Motor1_A).high();
                motorPins.get(motor == 0 ? Motor0_B : Motor1_B).low();
                break;
        }
    }

    public void forward() {
        if (carState.isFrontCollisionWarning() && stopOnCollisionWarning) {
            return;
        }

        motor(0, MotorDirection.FORWARD);
        motor(1, MotorDirection.FORWARD);

        if (directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.FORWARD);
    }

    public void backward() {
        motor(0, MotorDirection.BACKWARD);
        motor(1, MotorDirection.BACKWARD);

        if (directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.BACKWARD);

    }

    public void stop() {
        motorPins.values().forEach(pin -> pin.low());

        if (directionStateChangeHandler != null) directionStateChangeHandler.accept(Direction.NO);
    }

    public enum MotorDirection {
        FORWARD,
        BACKWARD
    }
}
