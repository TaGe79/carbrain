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

  @Scheduled(initialDelay = 10000, fixedDelayString = "${car.engine.collision.avoidance.task.delay:500}")
  protected void collisionAvoidanceTask() throws InterruptedException {

    final long frontLeftObstacleDistance = frontLeftCollisionDetector.getCurrentDistance();
    final long frontRightObstacleDistance = frontRightCollisionDetector.getCurrentDistance();
    final long rearObstacleDistance =
      carStateAggregator.getMoving() == Direction.FORWARD ? -1 : rearCollisionDetector.getCurrentDistance();
    carStateAggregator.setRearCollisionDetector(carStateAggregator.getMoving() != Direction.FORWARD);

    if (frontLeftObstacleDistance < 130 || frontRightObstacleDistance < 130) {
      log.info("Front collision warning. Distance: L({}) R({})", frontLeftObstacleDistance, frontRightObstacleDistance);
      carStateAggregator.setFrontCollisionWarning(true);
    } else {
      carStateAggregator.setFrontCollisionWarning(false);
    }

    carStateAggregator.setRearCollisionDetector(rearObstacleDistance != -1);

    if (rearObstacleDistance > -1 && rearObstacleDistance < 170) {
      log.info("Rear collision warning. Distance: {}", rearObstacleDistance);
      carStateAggregator.setRearCollisionWarning(true);
    } else {
      carStateAggregator.setRearCollisionWarning(false);
    }
  }
}
