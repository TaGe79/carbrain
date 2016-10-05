package org.tage.pi.car;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tage.pi.car.hardware.DistanceProvider;

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
  DistanceProvider frontLeftCollisionDetector;

  @Getter
  @Autowired
  @Qualifier("FrontRightCollisionDetector")
  DistanceProvider frontRightCollisionDetector;

  @Getter
  @Autowired
  @Qualifier("RearCollisionDetector")
  DistanceProvider rearCollisionDetector;

  @Getter
  @Autowired
  CarStateAggregator carStateAggregator;

  @Value("${car.front.obstacle.warning.distance:10}")
  long frontObstacleWarningDistance;

  @Value("${car.rear.obstacle.warning.distance:16}")
  long rearObstacleWarningDistance;

  @PostConstruct
  protected void initialize() {
    frontLight.setStateChangeHandler(carStateAggregator::setFrontLight);

    motor.setDirectionStateChangeHandler(carStateAggregator::setMoving);
    motor.setSpeedStateChangeHandler(carStateAggregator::setSpeed);

    steeringServo.setDirectionStateChangeHandler(carStateAggregator::setTurning);

    carStateAggregator.setFrontObstacle(ObstaclePosition.NONE);
  }

  @Override
  public void onApplicationEvent(ApplicationContextEvent contextEvent) {
    if (contextEvent instanceof ContextClosedEvent) {
      motor.stop();
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

    log.info("Front distance: L({}) R({})", frontLeftObstacleDistance, frontRightObstacleDistance);
    log.debug("Rear distance: {}", rearObstacleDistance);

    if ( frontLeftObstacleDistance < 0 || frontRightObstacleDistance < 0 ) {
      log.info("Waiting for the collision detector to active");
      return;
    }

    if (frontLeftObstacleDistance < frontObstacleWarningDistance || frontRightObstacleDistance < frontObstacleWarningDistance) {
      carStateAggregator.setFrontCollisionWarning(true);
      if ( frontLeftObstacleDistance < frontObstacleWarningDistance && frontRightObstacleDistance < frontObstacleWarningDistance ) {
        log.info("Frontal collision warning");
        carStateAggregator.setFrontObstacle(ObstaclePosition.BOTH);
      } else {
        if (frontLeftObstacleDistance < frontObstacleWarningDistance ) {
          log.info("Frontal LEFT collision warning");
          carStateAggregator.setFrontObstacle(ObstaclePosition.LEFT);
        }

        if (frontRightObstacleDistance < frontObstacleWarningDistance ){
          log.info("Frontal RIGHT collision warning");
          carStateAggregator.setFrontObstacle(ObstaclePosition.RIGHT);
        }
      }
    } else {
      carStateAggregator.setFrontCollisionWarning(false);
      carStateAggregator.setFrontObstacle(ObstaclePosition.NONE);
    }

    if (rearObstacleDistance < rearObstacleWarningDistance) {
      carStateAggregator.setRearCollisionWarning(true);
    } else {
      carStateAggregator.setRearCollisionWarning(false);
    }
  }
}
