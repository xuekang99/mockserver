package org.mockserver.mock.action;

import com.google.common.util.concurrent.SettableFuture;
import org.mockserver.client.netty.NettyHttpClient;
import org.mockserver.filters.HopByHopHeaderFilter;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

import static org.mockserver.model.HttpResponse.notFoundResponse;

/**
 * @author jamesdbloom
 */
public abstract class HttpForwardAction {

    protected final MockServerLogger mockServerLogger;
    private final NettyHttpClient httpClient;
    private HopByHopHeaderFilter hopByHopHeaderFilter = new HopByHopHeaderFilter();

    HttpForwardAction(MockServerLogger mockServerLogger, NettyHttpClient httpClient) {
        this.mockServerLogger = mockServerLogger;
        this.httpClient = httpClient;
    }

    protected HttpForwardActionResult sendRequest(HttpRequest httpRequest, @Nullable InetSocketAddress remoteAddress) {
        try {
            return new HttpForwardActionResult(httpRequest, httpClient.sendRequest(hopByHopHeaderFilter.onRequest(httpRequest), remoteAddress));
        } catch (Exception e) {
            mockServerLogger.error(httpRequest, e, "Exception forwarding request " + httpRequest);
        }
        return notFoundFuture(httpRequest);
    }

    HttpForwardActionResult notFoundFuture(HttpRequest httpRequest) {
        SettableFuture<HttpResponse> notFoundFuture = SettableFuture.create();
        notFoundFuture.set(notFoundResponse());
        return new HttpForwardActionResult(httpRequest, notFoundFuture);
    }
}
