package org.tage.pi.car;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.tage.pi.car.hardware.PCA9685Driver;

import javax.annotation.PostConstruct;

/**
 * Created by tgergel on 16/01/16.
 */
@Slf4j
public class LightController {
    @Autowired
    protected PCA9685Driver pwmDriver;

    protected final int channel;

    public LightController(int channel) {
        this.channel = channel;
    }

    @PostConstruct
    protected void init() {
        log.info("Lights for channel {} are initialized!", channel);
        // turn off light upon initializing
        pwmDriver.setPWM(channel, 0x00, 0x00);
    }

    public void on(int intensity) {
        pwmDriver.setPWM(channel, Math.max(0x07ff, intensity), 0x0000);
    }

    public void on() {
        pwmDriver.setPWM(channel, 0x0800, 0x0000);
    }

    public void off() {
        pwmDriver.setPWM(channel, 0x0000, 0x0000);
    }
}

