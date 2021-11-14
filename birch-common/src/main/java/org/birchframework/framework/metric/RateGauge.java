/*===============================================================
 = Copyright (c) 2021 Birch Framework
 = This program is free software: you can redistribute it and/or modify
 = it under the terms of the GNU General Public License as published by
 = the Free Software Foundation, either version 3 of the License, or
 = any later version.
 = This program is distributed in the hope that it will be useful,
 = but WITHOUT ANY WARRANTY; without even the implied warranty of
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 = GNU General Public License for more details.
 = You should have received a copy of the GNU General Public License
 = along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ==============================================================*/
package org.birchframework.framework.metric;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.util.CollectionUtils;

/**
 * Rate calculating gauge.  Registers a {@link Gauge} given the configurable parameters of the builder.  At the very least, the following
 * configurations are needed:
 * <ul>
 *    <li>{@link RateGaugeBuilder#withName(String)}</li>
 *    <li>{@link RateGaugeBuilder#register()}</li>
 * </ul>
 * Call the {@link RateGaugeBuilder#register()} method to obtain an immutable instance.  The instance internally builds and delegates to a {@link Gauge}.
 * Must call the {@link #increment()} method in order to update the internal counter of the gauge. During each sampling, number of internal counter
 * increments per second is calculated and reported to the {@link MeterRegistry}.  In order to override this default calculation, provide a
 * {@link BiFunction} using {@link RateGaugeBuilder#withValueFunction(BiFunction)}.  For example:
 * <br/><br/>
 * <pre>
 * final var rateGauge = RateGauge.builder()
 *                                .name("my.app.metric")
 *                                .valueFunction((count, time) -&gt; count / (time / 1000D))   // Calculate rate as count per second
 *                                .register();
 * </pre>
 * The {@link #increment()} method updates the internal atomic counter, asynchronously in order to minimize performance degradation at the caller.
 * @author Keivan Khalichi
 */
@SuppressWarnings({"AutoBoxing", "PMD.UnusedPrivateField"})
public class RateGauge {

   private static final ExecutorService threadPool = Executors.newCachedThreadPool();

   @Delegate
   private final Gauge                          delegate;
   private final AtomicLong                     counter;
   private final StopWatch                      stopWatch;
   private final BiFunction<Long, Long, Double> valueFunction;

   @Builder(buildMethodName = "register")
   protected RateGauge(@NonNull final String withName, final String withDescription, @NonNull final MeterRegistry withRegistry,
                       final BiFunction<Long, Long, Double> withValueFunction, final List<Tag> withTags) {
      this.counter       = new AtomicLong(0);
      this.valueFunction = withValueFunction;
      final var aBuilder = Gauge.builder(withName, this::sample);
      if (!CollectionUtils.isEmpty(withTags)) {
         aBuilder.tags(withTags);
      }
      if (StringUtils.isNotBlank(withDescription)) {
         aBuilder.description(withDescription);
      }
      this.delegate  = aBuilder.register(withRegistry);
      this.stopWatch = StopWatch.createStarted();
   }

   public void increment() {
      threadPool.execute(this.counter::incrementAndGet);
   }

   /**
    * Calculates rate as count per second.  To override, provide a {@link BiFunction} to the
    * {@link RateGaugeBuilder#valueFunction} when building a new instance of this class.
    * @return rate as count per second
    */
   @SuppressWarnings("AutoUnboxing")
   protected double sample() {
      final var aCount = this.counter.getAndSet(0);
      final var aTime = this.stopWatch.getTime();
      this.stopWatch.reset();
      this.stopWatch.start();
      return this.valueFunction == null ? aCount / (aTime / 1_000D) : this.valueFunction.apply(aCount, aTime);
   }
}