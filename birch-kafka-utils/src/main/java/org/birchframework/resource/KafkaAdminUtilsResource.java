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

package org.birchframework.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.birchframework.framework.kafka.KafkaAdminUtils;

import static javax.ws.rs.core.MediaType.*;

/**
 * Resource to expose methods of {@link KafkaAdminUtils}.
 * @author Keivan Khalichi
 */
@Path("/kafka")
@Produces(APPLICATION_JSON)
public interface KafkaAdminUtilsResource {

   @Path("/topicLags")
   @GET
   Response topicLags();
}