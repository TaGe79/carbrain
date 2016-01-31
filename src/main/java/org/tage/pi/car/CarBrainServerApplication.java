package org.tage.pi.car;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.tage.pi.car.hardware.HCSR04USDistance;

import javax.annotation.PostConstruct;

/**
 * The car-brain application class;
 * <p></p>
 * Created by tgergel on 12/01/16.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = {"org.tage.pi.car"})
@EnableScheduling
@EnableAutoConfiguration
public class CarBrainServerApplication {

    @PostConstruct
    protected void init() {
        log.info("Car brain started!");
    }

    @Autowired
    private GpioController gpio;

    @Bean(name = "FrontLightController")
    public LightController frontLightController() {
        return new LightController(1);
    }

    @Bean(name = "FrontCollisionDetector")
    public HCSR04USDistance frontCollisionDetector() {
        return new HCSR04USDistance(14, 10, gpio);
    }

    @Bean(name = "RearCollisionDetector")
    public HCSR04USDistance rearCollisionDetector() {
        return new HCSR04USDistance(13,6, gpio);
    }

    @Bean
    public GpioController gpio() {
        return GpioFactory.getInstance();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("car-tasks");
        taskScheduler.setPoolSize(5);
        taskScheduler.afterPropertiesSet();
        return taskScheduler;
    }

    @Bean
    public AsyncTaskExecutor taskExevutor() {
        final AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        return taskExecutor;
    }

    public static void main(String[] args) {
        SpringApplication.run(CarBrainServerApplication.class, args);
    }
}
