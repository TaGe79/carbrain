package org.tage.pi.car.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.tage.pi.car.CarEngine;

/**
 * Created by tgergel on 15/01/16.
 */
@Slf4j
@Controller
@RequestMapping("/car")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CarController {

  protected final CarEngine engine;

  @RequestMapping(path = "turn/{direction}", method = RequestMethod.GET)
  @ResponseBody
  public String turn(@PathVariable("direction") String direction) {
    switch (direction) {
      case "left":
        engine.getSteeringServo().turnLeft();
        break;
      case "right":
        engine.getSteeringServo().turnRight();
        break;
      case "straight":
        engine.getSteeringServo().turnStraight();
        break;
      default:
        log.warn("Don't know what to do for: {}", direction);
    }

    return direction;
  }

  @RequestMapping(path = "speed/{speed}", method = RequestMethod.GET)
  @ResponseBody
  public String setSpeed(@PathVariable("speed") String speed) {
    engine.getMotor().setSpeed(Integer.parseInt(speed));
    return speed;
  }

  @RequestMapping(path = "go/{direction}", method = RequestMethod.GET)
  @ResponseBody
  public String go(@PathVariable("direction") String direction,
                   @RequestParam(required = false, defaultValue = "50") int speed) {
    engine.getMotor().setSpeed(speed);
    switch (direction) {
      case "forward":
        engine.getMotor().forward();
        break;
      case "backward":
        engine.getMotor().backward();
        break;
      default:
        log.warn("Don't know what to do for: {}", direction);
    }

    return direction;
  }

  @RequestMapping(path = "stop", method = RequestMethod.GET)
  @ResponseBody
  public String stop() {
    engine.getMotor().stop();
    return "stop";
  }

  @RequestMapping(path = "front/light/{action}")
  @ResponseBody
  public String frontLight(@PathVariable("action") String action) {
    switch (action) {
      case "on":
        engine.getFrontLight().on();
        break;
      case "off":
        engine.getFrontLight().off();
        break;
      default:
        log.warn("Don't know what to do for: {}", action);
    }

    return "front light turned " + action;
  }
}
