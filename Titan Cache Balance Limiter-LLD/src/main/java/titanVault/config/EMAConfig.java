package titanVault.config;

import java.time.Duration;
import java.util.Objects;

public class EMAConfig {

    private final long maxCapacity;
    private final double alpha;

    /**
     * Constructor to initialize Configuration.
     *
     * @param maxCapacity     Maximum number of requests allowed.
     * @param averagingPeriod Duration over which EMA is calculated.
     */
    public EMAConfig(long maxCapacity, Duration averagingPeriod) {
        this.maxCapacity = maxCapacity;
        this.alpha = 2.0 / (averagingPeriod.toMillis() + 1.0);
    }

    public long getMaxCapacity() {
        return this.maxCapacity;
    }

    public double getAlpha() {
        return this.alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EMAConfig emaConfig)) return false;
        return ((getMaxCapacity() == emaConfig.getMaxCapacity())
                && (Double.compare(getAlpha(), emaConfig.getAlpha()) == 0));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(getMaxCapacity(), getAlpha()));
    }

    @Override
    public String toString() {
        return "EMAConfig{" +
                "maxCapacity=" + maxCapacity +
                ", alpha=" + alpha +
                '}';
    }
}
