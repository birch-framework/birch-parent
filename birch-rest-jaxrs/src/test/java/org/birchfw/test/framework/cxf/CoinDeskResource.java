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
package org.birchfw.test.framework.cxf;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.birchframework.framework.cxf.AutoProxy;

import static javax.ws.rs.core.MediaType.*;

/**
 * A JAX-RS resource used for integration testing only.
 * @author Keivan Khalichi
 */
@Path("/bpi")
@Produces(APPLICATION_JSON)
@AutoProxy(baseURI = "${services.coindesk.address}")
public interface CoinDeskResource {

   @Path("/currentprice.json")
   @GET
   Response currentPrice();
}