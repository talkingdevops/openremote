/*
 * Copyright 2016, OpenRemote Inc.
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
package org.openremote.container.web.socket;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.openremote.container.security.AuthContext;
import org.openremote.model.Constants;

import java.util.function.Consumer;

public class WebsocketConsumer extends DefaultConsumer {

    private final WebsocketEndpoint endpoint;

    public WebsocketConsumer(WebsocketEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        endpoint.connect(this);
    }

    @Override
    public void doStop() throws Exception {
        endpoint.disconnect(this);
        super.doStop();
    }

    public WebsocketEndpoint getEndpoint() {
        return endpoint;
    }

    public void sendMessage(final String sessionKey, final AuthContext authContext, final String message) {
        sendMessage(sessionKey, authContext, (Object) message);
    }

    public void sendMessage(final String sessionKey, final AuthContext authContext, final Object message) {
        sendMessage(sessionKey, authContext, message, null);
    }

    public void sendMessage(final String sessionKey, final AuthContext authContext, final Object message, Consumer<Exchange> exchangePreparer) {
        final Exchange exchange = getEndpoint().createExchange();
        exchange.getIn().setHeader(WebsocketConstants.SESSION_KEY, sessionKey);
        exchange.getIn().setHeader(Constants.AUTH_CONTEXT, authContext);
        exchange.getIn().setBody(message);
        if (exchangePreparer != null) {
            exchangePreparer.accept(exchange);
        }

        getAsyncProcessor().process(exchange, doneSync -> {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        });
    }
}
