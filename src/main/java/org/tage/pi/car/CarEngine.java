package org.tage.pi.car;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tage.pi.car.hardware.HCSR04USDistance;

import javax.annotation.PostConstruct;

/**
 * Created by tgergel on 22/01/16.
 */
@Slf4j
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

  @Getter
  @Autowired
  @Qualifier("FrontLeftCollisionDetector")
  HCSR04USDistance frontLeftCollisionDetector;

  @Getter
  @Autowired
  @Qualifier("FrontRightCollisionDetector")
  HCSR04USDistance frontRightCollisionDetector;

  @Getter
  @Autowired
  @Qualifier("RearCollisionDetector")
  HCSR04USDistance rearCollisionDetector;

  @Getter
  @Autowired
  CarStateAggregator carStateAggregator;

  @PostConstruct
  protected void initialize() {
    frontLight.setStateChangeHandler(carStateAggregator::setFrontLight);

    motor.setDirectionStateChangeHandler(carStateAggregator::setMoving);
    motor.setSpeedStateChangeHandler(carStateAggregator::setSpeed);

    steeringServo.setDirectionStateChangeHandler(carStateAggregator::setTurning);
  }

  @Override
  public void onApplicationEvent(ApplicationContextEvent contextEvent) {
    if (contextEvent instanceof ContextClosedEvent) {
      steeringServo.turnStraight();
      frontLight.off();
    }
  }

  @Scheduled(initialDelay = 10000, fixedDelayString = "${car.engine.collision.avoidance.task.delay:200}")
  protected void collisionAvoidanceTask() throws InterruptedException {

    final long frontLeftObstacleDistance = frontLeftCollisionDetector.getCurrentDistance();
    final long frontRightObstacleDistance = frontRightCollisionDetector.getCurrentDistance();
    final long rearObstacleDistance =
      carStateAggregator.getMoving() == Direction.FORWARD ? -1 : rearCollisionDetector.getCurrentDistance();
    carStateAggregator.setRearCollisionDetector(carStateAggregator.getMoving() != Direction.FORWARD);

    log.debug("Front distance: L({}) R({})", frontLeftObstacleDistance, frontRightObstacleDistance);
    log.debug("Rear distance: {}", rearObstacleDistance);

    if (frontLeftObstacleDistance < 100 || frontRightObstacleDistance < 100) {
      carStateAggregator.setFrontCollisionWarning(true);
    } else {
      carStateAggregator.setFrontCollisionWarning(false);
    }

    if (rearObstacleDistance < 160) {
      carStateAggregator.setRearCollisionWarning(true);
    } else {
      carStateAggregator.setRearCollisionWarning(false);
    }
  }
}
