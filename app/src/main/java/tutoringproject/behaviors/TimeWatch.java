package tutoringproject.behaviors;

import java.util.concurrent.TimeUnit;

/**
 * Reused by aditi on 1/29/18.
 */
/**
 * Created by kjiang on 5/2/16.
 * Usage:
 * TimeWatch watch = TimeWatch.start();
 * // do something
 * long passedTimeInMs = watch.time();
 * long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
 */

public class TimeWatch {
    long starts;

    public static tutoringproject.behaviors.TimeWatch start() {
        return new tutoringproject.behaviors.TimeWatch();
    }

    private TimeWatch() {
        reset();
    }

    public tutoringproject.behaviors.TimeWatch reset() {
        starts = System.nanoTime();
        return this;
    }

    public long time() {
        long ends = System.nanoTime() - starts;
        return ends;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.NANOSECONDS);
    }
}
