package org.tage.pi.car;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by tgergel on 30/01/16.
 */
@Data
@Component
public class CarStateAggregator {
    private long speed = 0;

    private Direction moving = Direction.NO;

    private Direction turning = Direction.NO;

    private boolean frontCollisionWarning = false;

    private boolean frontLight = false;
}
