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
package org.birchframework.framework.jaxrs;

import java.net.URI;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessorType;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

@XmlAccessorType(FIELD)
@SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized", "WeakerAccess"})
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class JAXRSErrorResponse {

    private String error;
    private String message;
    private URI    path;
    @JsonProperty("status")
    private int    statusCode;
    private Date   timestamp;

    /**
     * Getter for {@link #error}
     * @returns Value of {@link #error}
     */
    public String getError() {
        return error;
    }

    /**
     * Getter for {@link #message}
     * @returns Value of {@link #message}
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for {@link #path}
     * @returns Value of {@link #path}
     */
    public URI getPath() {
        return path;
    }

    /**
     * Getter for {@link #statusCode}
     * @returns Value of {@link #statusCode}
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Getter for {@link #timestamp}
     * @returns Value of {@link #timestamp}
     */
    public Date getTimestamp() {
        return timestamp;
    }
}