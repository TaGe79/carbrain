package org.tage.pi.car;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * The car-brain application class;
 * <p></p>
 * Created by tgergel on 12/01/16.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"org.tage.pi.car"})
@EnableAutoConfiguration
public class CarBrainServerApplication {

    @PostConstruct
    protected void init() {
        log.info("Car brain started!");
    }

    @Bean(name = "FrontLightController")
    public LightController frontLightController() {
        return new LightController(1);
    }

    @Bean(name = "FrontLightDetector")
    public LightController frontLightDetector() {
        return new LightController(2);
    }

    @Bean(name = "gpio")
    public GpioController gpio() {
        return GpioFactory.getInstance();
    }

    public static void main(String[] args) {
        SpringApplication.run(CarBrainServerApplication.class, args);
    }
}
