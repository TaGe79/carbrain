package org.tage.pi.car;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DrivingAssistant {

  private final CarEngine carEngine;

  @Value("${car.max.speed:50}")
  private int maxSpeed;

  @Value("${car.initial.speed:10}")
  private int initialSpeed;

  @Value("${car.speed.quantum:1}")
  private int speedQuantum;

  private int currentSpeed;

  private int cleanRoadIterations;

  private Direction currentDrivingDirection;

  @Setter
  @Getter
  private boolean assistantState = false;

  @PostConstruct
  private void initialize() {
    this.currentSpeed = initialSpeed;
    this.cleanRoadIterations = 0;
    this.currentDrivingDirection = Direction.FORWARD;
  }


  @Scheduled(initialDelay = 10000, fixedRateString = "${car.driving.assistant.delay:100}")
  private void drivingAssistantTask() {
    if (!assistantState) {
      return;
    }

    if (carEngine.getFrontLeftCollisionDetector().getCurrentDistance() < 0 ||
        carEngine.getFrontRightCollisionDetector().getCurrentDistance() < 0) {
      log.info("Waiting for the collision detectors to get activated");
      carEngine.getMotor().stop();
      return;
    }

    carEngine.getMotor().setSpeed(currentSpeed);
    if (currentDrivingDirection == Direction.FORWARD) {
      driveForward();
    } else {
      driveBackward();
    }

  }

  private void driveBackward() {
    log.info("Driving backward! {}", this.currentSpeed);
    carEngine.getMotor().backward();

    this.carEngine.getSteeringServo().turnStraight();
    increaseCarSpeed();

//    if (!carEngine.getCarStateAggregator().isRearCollisionWarning()) {
//      this.carEngine.getSteeringServo().turnStraight();
//      increaseCarSpeed();
//    } else {
//      if (carEngine.getCarStateAggregator().getRearObstacle() == ObstaclePosition.BOTH) {
//        decreaseCarSpeed();
//        if (this.carEngine.getRearCollisionDetector().getCurrentDistance() <= 5) {
//          carEngine.getMotor().stop();
//          log.info("Car stopped");
//          this.cleanRoadIterations = 0;
//        }
//        if (currentSpeed == 0 || cleanRoadIterations == 0) {
//          log.info("Car turned forwards!");
//          currentDrivingDirection = Direction.FORWARD;
//        }
//      }
//    }

    if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.NONE) {
      carEngine.getMotor().stop();
      currentDrivingDirection = Direction.FORWARD;
    }
  }

  private void driveForward() {
    log.info("Driving forward! {}", carEngine.getCarStateAggregator().getSpeed());
    carEngine.getMotor().forward();
    if (!carEngine.getCarStateAggregator().isFrontCollisionWarning()) {
      log.info("Driving forward CLEAN! V({}) CRI({})", currentSpeed, cleanRoadIterations);
      increaseCarSpeed();
      this.carEngine.getSteeringServo().turnStraight();
    } else {
      if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.BOTH) {
        log.info("Driving forward Obstacle! V({}) CRI({})", currentSpeed, cleanRoadIterations);
        decreaseCarSpeed();
        if (carEngine.getFrontLeftCollisionDetector().getCurrentDistance() <= 3 ||
            carEngine.getFrontRightCollisionDetector().getCurrentDistance() <= 3) {
          carEngine.getMotor().stop();
          log.info("Car stopped");
        }
      } else {
        if (carEngine.getFrontLeftCollisionDetector().getCurrentDistance() < 3 || carEngine.getFrontRightCollisionDetector().getCurrentDistance() < 3) {
          log.info("Car stopped");
          carEngine.getMotor().stop();
          currentSpeed = 0;
          cleanRoadIterations = 0;
        } else {
          if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.LEFT) {
            log.info("Driving forward, obstacle at the left!");
            carEngine.getSteeringServo().turnRight();
            try {
              Thread.currentThread().sleep(800l);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          } else {
            log.info("Driving forward, obstacle at the right!");
            carEngine.getSteeringServo().turnLeft();
            try {
              Thread.currentThread().sleep(800l);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }

        if (currentSpeed == 0 || cleanRoadIterations == 0) {
          log.info("Car turned backwards!");
          currentDrivingDirection = Direction.BACKWARD;
        }
      }
    }
  }

  private void decreaseCarSpeed() {
    this.cleanRoadIterations = cleanRoadIterations > 1 ? cleanRoadIterations - 1 : 0;
    currentSpeed = currentSpeed > speedQuantum ? currentSpeed - speedQuantum : 0;
    carEngine.getMotor().setSpeed(currentSpeed);
  }

  private void increaseCarSpeed() {
    this.cleanRoadIterations = this.cleanRoadIterations < 10 ? this.cleanRoadIterations + 1 : 20;
    this.currentSpeed = this.currentSpeed < this.maxSpeed ? this.currentSpeed + this.speedQuantum : this.maxSpeed;
    this.carEngine.getMotor().setSpeed(this.currentSpeed);
  }
}
