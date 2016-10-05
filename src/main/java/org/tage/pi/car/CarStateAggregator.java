package org.tage.pi.car;

import org.springframework.stereotype.Component;

/**
 * Created by tgergel on 30/01/16.
 */
@Component
public class CarStateAggregator {
  private final static Object sync = new Object();

  public long getSpeed() {
    return speed;
  }

  public void setSpeed(long speed) {
    this.speed = speed;
  }

  public Direction getMoving() {
    return moving;
  }

  public void setMoving(Direction moving) {
    this.moving = moving;
  }

  public Direction getTurning() {
    return turning;
  }

  public void setTurning(Direction turning) {
    this.turning = turning;
  }

  public boolean isFrontCollisionWarning() {
    synchronized (sync) {
      return frontCollisionWarning;
    }
  }

  public void setFrontCollisionWarning(boolean frontCollisionWarning) {
    synchronized (sync) {
      this.frontCollisionWarning = frontCollisionWarning;
    }
  }

  public ObstaclePosition getFrontObstacle() {
    synchronized (sync) {
      return frontObstacle;
    }
  }

  public void setFrontObstacle(ObstaclePosition frontObstacle) {
    synchronized (sync) {
      this.frontObstacle = frontObstacle;
    }
  }

  public ObstaclePosition getRearObstacle() {
    return rearObstacle;
  }

  public void setRearObstacle(ObstaclePosition rearObstacle) {
    this.rearObstacle = rearObstacle;
  }

  public boolean isRearCollisionDetector() {
    return rearCollisionDetector;
  }

  public void setRearCollisionDetector(boolean rearCollisionDetector) {
    this.rearCollisionDetector = rearCollisionDetector;
  }

  public boolean isRearCollisionWarning() {
    return rearCollisionWarning;
  }

  public void setRearCollisionWarning(boolean rearCollisionWarning) {
    this.rearCollisionWarning = rearCollisionWarning;
  }

  public boolean isFrontLight() {
    return frontLight;
  }

  public void setFrontLight(boolean frontLight) {
    this.frontLight = frontLight;
  }

  private long speed = 0;

  private Direction moving = Direction.NO;

  private Direction turning = Direction.NO;

  private volatile boolean frontCollisionWarning = false;

  private volatile ObstaclePosition frontObstacle = ObstaclePosition.NONE;

  private volatile ObstaclePosition rearObstacle = ObstaclePosition.NONE;

  private volatile boolean rearCollisionDetector = false;

  private volatile boolean rearCollisionWarning = false;

  private boolean frontLight = false;

}
