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

package org.birchframework.framework.bridge;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static org.birchframework.framework.bridge.DestinationType.*;

@Getter
@Setter
@SuppressWarnings("unused")
public class Destination implements Serializable {

    @JsonProperty
    private String destinationType;
    @JsonProperty
    private String name;
    @JsonIgnore
    private DestinationType type;

    public Destination() {
    }

    public Destination(final String theDestinationType, final String theName) {
        this.destinationType = theDestinationType;
        this.name            = theName;
    }

    public Destination(final String theName, final DestinationType theType) {
        this.name = theName;
        this.type = theType;
        switch(this.type) {
            case QUEUE:
                this.destinationType = QUEUE.asString();
                break;
            case TOPIC:
                this.destinationType = TOPIC.asString();
                break;
        }
    }
}