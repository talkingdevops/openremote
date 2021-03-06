/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.shared.syslog;

import jsinterop.annotations.JsType;
import org.openremote.manager.shared.http.RequestParams;
import org.openremote.manager.shared.http.SuccessStatusCode;
import org.openremote.model.syslog.SyslogEvent;
import org.openremote.model.syslog.SyslogLevel;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("syslog")
@JsType(isNative = true)
public interface SyslogResource {

    @GET
    @Path("event")
    @Produces(APPLICATION_JSON)
    @RolesAllowed({"read:admin"})
    SyslogEvent[] getEvents(@BeanParam RequestParams requestParams, @QueryParam("level") SyslogLevel level, @QueryParam("limit") Integer limit);

    @DELETE
    @Path("event")
    @SuccessStatusCode(204)
    @RolesAllowed({"write:admin"})
    void clearEvents(@BeanParam RequestParams requestParams);

    @GET
    @Path("config")
    @SuccessStatusCode(200)
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"read:admin"})
    SyslogConfig getConfig(@BeanParam RequestParams requestParams);

    @PUT
    @Path("config")
    @SuccessStatusCode(204)
    @Consumes(APPLICATION_JSON)
    @RolesAllowed({"write:admin"})
    void updateConfig(@BeanParam RequestParams requestParams, @Valid SyslogConfig config);

}
