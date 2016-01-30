package org.tage.pi.car.hardware;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by tgergel on 13/01/16.
 */
@Slf4j
@Component
public class PCA9685Driver {

    protected static final int DRIVER_BASE_ADDRESS = 0x40;

    protected static final int ALL_LED_ON_L = 0xFA;
    protected static final int ALL_LED_ON_H = 0xFB;
    protected static final int ALL_LED_OFF_L = 0xFC;
    protected static final int ALL_LED_OFF_H = 0xFD;
    protected static final int LED0_ON_L = 0x06;
    protected static final int LED0_ON_H = 0x07;
    protected static final int LED0_OFF_L = 0x08;
    protected static final int LED0_OFF_H = 0x09;
    protected static final int REG_MODE1 = 0x00;
    protected static final int REG_MODE2 = 0x01;
    protected static final int REG_PRESCALE = 0xFE;

    protected static final byte RM2_OUTDRV_TOTEM_POLE = 0x04;
    protected static final byte RM1_ALLCALL_ON = 0x01;
    protected static final byte RM1_SLEEP = 0x10;


    protected final I2CDevice generalAddressDevice;
    protected final I2CDevice pwmDevice;

    public PCA9685Driver() throws IOException {
        final I2CBus bus = I2CFactory.getInstance(1); // The Bus number for B.Rev-2. Pi
        generalAddressDevice = bus.getDevice(0x00);
        pwmDevice = bus.getDevice(DRIVER_BASE_ADDRESS);

    }

    @PostConstruct
    protected void initialize() {
        log.debug("Resetting PCA9685 MODE1 (without SLEEP) and MODE2");
        setAllPWM(0, 0);

        try {
            pwmDevice.write(REG_MODE2, RM2_OUTDRV_TOTEM_POLE);
            pwmDevice.write(REG_MODE1, RM1_ALLCALL_ON);
            Thread.sleep(5);

            int mode1 = pwmDevice.read(REG_MODE1) & ~RM1_SLEEP;         // wake up
            pwmDevice.write(REG_MODE1, (byte) mode1);
            Thread.sleep(5);
        } catch (IOException e) {
            log.error("Can not initialize PCA9685 PW Driver!", e);
        } catch (InterruptedException e) {
            log.error("Sleep interrupted while waiting for oscillator!", e);
        }
    }

    public void setPWM(int channel, int on, int off) {
        try {
            pwmDevice.write(LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
            pwmDevice.write(LED0_ON_H + 4 * channel, (byte) (on >> 8));
            pwmDevice.write(LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
            pwmDevice.write(LED0_OFF_H + 4 * channel, (byte) (off >> 8));
        } catch (IOException e) {
            log.error("Can not set PWM Device frequency", e);
        }
    }

    public void setPWMFreq(int frequency) {
        double preScaleVal = 25000000.0;    // 25MHz
        preScaleVal /= 4096.0;              // 12-bit
        preScaleVal /= (float) frequency;
        preScaleVal -= 1.0;
        final long preScale = (long) Math.floor(preScaleVal + 0.5);

        log.debug("Final pre-scale: {}", preScale);

        try {
            int oldMode = pwmDevice.read(REG_MODE1);
            int newMode = (oldMode & 0x7F) | RM1_SLEEP;     // sleep
            pwmDevice.write(REG_MODE1, (byte) newMode);     // go to sleep
            pwmDevice.write(REG_PRESCALE, (byte) preScale);

            pwmDevice.write(REG_MODE1, (byte) oldMode);
            Thread.sleep(5);
            pwmDevice.write(REG_MODE1, (byte) (oldMode | 0x80));
        } catch (IOException e) {
            log.error("Can not set PWM Device frequency", e);
        } catch (InterruptedException e) {
            log.error("Slep interrupted while waiting for oscillator!", e);
        }
    }

    public void setAllPWM(int on, int off) {
        try {
            pwmDevice.write(ALL_LED_ON_L, (byte) (on & 0xFF));
            pwmDevice.write(ALL_LED_ON_H, (byte) (on >> 8));
            pwmDevice.write(ALL_LED_OFF_L, (byte) (off & 0xFF));
            pwmDevice.write(ALL_LED_OFF_H, (byte) (off >> 8));
        } catch (IOException e) {
            log.error("Can not reset PWMs", e);
        }
    }

    public void softReset() {
        try {
            generalAddressDevice.write((byte) 0x06);
        } catch (IOException e) {
            log.error("Can not reset the PWM module!", e);
        }
    }

    public int getPWM(int channel) {
        return 0;
    }
}
