package org.tage.pi.car;

import lombok.RequiredArgsConstructor;
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

  @Value("${car.initial.speed:5}")
  private int initialSpeed;

  @Value("${car.speed.quantum:1}")
  private int speedQuantum;

  private int currentSpeed;

  private int cleanRoadIterations;

  private Direction currentDrivingDirection;

  @PostConstruct
  private void initialize() {
    this.currentSpeed = initialSpeed;
    this.cleanRoadIterations = 0;
    this.currentDrivingDirection = Direction.FORWARD;
  }


  @Scheduled(initialDelay = 10000, fixedRateString = "${car.driving.assistant.delay:100}")
  private void drivingAssistantTask() {
    if (carEngine.getFrontLeftCollisionDetector().getCurrentDistance() < 0 ||
        carEngine.getFrontRightCollisionDetector().getCurrentDistance() < 0) {
      log.info("Waiting for the collision detectors to get activated");
      carEngine.getMotor().stop();
      return;
    }

    carEngine.getMotor().setSpeed(currentSpeed);
    if (currentDrivingDirection == Direction.FORWARD) {
      log.info("Driving forward!");
      carEngine.getMotor().forward();
      if (!carEngine.getCarStateAggregator().isFrontCollisionWarning()) {
        log.info("Driving forward CLEAN! V({}) CRI({})", currentSpeed, cleanRoadIterations);
        this.cleanRoadIterations += this.cleanRoadIterations < 10 ? this.cleanRoadIterations + 1 : 10;
        this.currentSpeed = this.currentSpeed < this.maxSpeed ? this.currentSpeed + this.speedQuantum : this.maxSpeed;
        this.carEngine.getMotor().setSpeed(this.currentSpeed);
        this.carEngine.getSteeringServo().turnStraight();
      } else {
        if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.BOTH) {
          log.info("Driving forward Obstacle! V({}) CRI({})", currentSpeed, cleanRoadIterations);
          if (carEngine.getFrontLeftCollisionDetector().getCurrentDistance() <= 3 ||
              carEngine.getFrontRightCollisionDetector().getCurrentDistance() <= 3) {
            carEngine.getMotor().stop();
            log.info("Car stopped");
            return;
          }
          this.cleanRoadIterations = cleanRoadIterations > 1 ? cleanRoadIterations - 1 : 0;
          currentSpeed = currentSpeed > speedQuantum ? currentSpeed - speedQuantum : 0;
          carEngine.getMotor().setSpeed(currentSpeed);
          if (currentSpeed == 0 || cleanRoadIterations == 0) {
            log.info("Car turned backwards!");
            currentDrivingDirection = Direction.BACKWARD;
          }
        } else {
          if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.LEFT) {
            log.info("Driving forward, obstacle at the left!");
            carEngine.getSteeringServo().turnRight();
          } else {
            log.info("Driving forward, obstacle at the right!");
            carEngine.getSteeringServo().turnLeft();
          }
        }
      }
    } else {
      log.info("Driving backward!");
      carEngine.getMotor().backward();
      if (!carEngine.getCarStateAggregator().isRearCollisionWarning()) {
        this.cleanRoadIterations += 1;
        this.currentSpeed = this.currentSpeed < this.maxSpeed ? this.currentSpeed + this.speedQuantum : this.maxSpeed;
        this.carEngine.getMotor().setSpeed(this.currentSpeed);
        this.carEngine.getSteeringServo().turnStraight();
      } else {
        if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.BOTH) {
          if (this.carEngine.getRearCollisionDetector().getCurrentDistance() <= 5) {
            carEngine.getMotor().stop();
            log.info("Car stopped");
            return;
          }
          this.cleanRoadIterations = cleanRoadIterations > 1 ? cleanRoadIterations - 1 : 0;
          currentSpeed = currentSpeed > speedQuantum ? currentSpeed - speedQuantum : 0;
          carEngine.getMotor().setSpeed(currentSpeed);
          if (currentSpeed == 0 || cleanRoadIterations == 0) {
            log.info("Car turned forwards!");
            currentDrivingDirection = Direction.FORWARD;
          }
        } else {
          if (carEngine.getCarStateAggregator().getFrontObstacle() == ObstaclePosition.LEFT) {
            carEngine.getSteeringServo().turnRight();
          } else {
            carEngine.getSteeringServo().turnLeft();
          }
        }
      }
    }

  }
}
