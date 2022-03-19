/*===============================================================
 = Copyright (c) 2022 Birch Framework
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

package org.birchframework.bridge;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;
import org.birchframework.framework.metric.RateGauge;

/**
 * {@link RoutePolicySupport} concrete implementation used to record incoming and outgoing messages to the bridge.
 * @author Keivan Khalichi
 */
@RequiredArgsConstructor
public class BridgeRoutePolicy extends RoutePolicySupport {

   private final RateGauge inGuage;
   private final RateGauge outGauge;

   @Override
   public void onExchangeBegin(final Route route, final Exchange exchange) {
      this.inGuage.increment();
   }

   @Override
   public void onExchangeDone(final Route route, final Exchange exchange) {
      this.outGauge.increment();
   }
}
