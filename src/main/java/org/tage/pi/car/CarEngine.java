package org.tage.pi.car;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by tgergel on 22/01/16.
 */
@Component
public class CarEngine implements ApplicationListener<ApplicationContextEvent> {

    @Getter
    @Autowired
    @Qualifier("FrontLightController")
    LightController frontLight;

    @Getter
    @Autowired
    SteeringServo steeringServo;

    @Getter
    @Autowired
    CarMotor motor;

    @Override
    public void onApplicationEvent(ApplicationContextEvent contextEvent) {
        if (contextEvent instanceof ContextClosedEvent) {
            steeringServo.turnStraight();
            frontLight.off();
        }
    }
}
