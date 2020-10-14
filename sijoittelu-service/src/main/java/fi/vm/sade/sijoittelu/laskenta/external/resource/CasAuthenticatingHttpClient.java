package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.javautils.cas.ApplicationSession;
import fi.vm.sade.javautils.cas.SessionToken;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CasAuthenticatingHttpClient extends java.net.http.HttpClient {
    private final java.net.http.HttpClient client;
    private final ApplicationSession applicationSession;

    public CasAuthenticatingHttpClient(java.net.http.HttpClient client, ApplicationSession applicationSession) {
        this.client = client;
        this.applicationSession = applicationSession;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return this.client.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return this.client.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return this.client.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return this.client.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return this.client.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return this.client.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return this.client.authenticator();
    }

    @Override
    public Version version() {
        return this.client.version();
    }

    @Override
    public Optional<Executor> executor() {
        return this.client.executor();
    }

    private static boolean isRedirectToCas(HttpResponse<?> response) {
        return response.statusCode() == 302
                && response.headers().allValues("Location").stream()
                .anyMatch(location -> location.contains("/cas/login"));
    }

    private static boolean isUnauthenticated(HttpResponse<?> response) {
        return response.statusCode() == 401;
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        try {
            SessionToken sessionToken = this.applicationSession.getSessionToken().get();
            HttpResponse<T> response = this.client.send(httpRequest, bodyHandler);
            if (isUnauthenticated(response) || isRedirectToCas(response)) {
                this.applicationSession.invalidateSession(sessionToken);
                this.applicationSession.getSessionToken().get();
                response = this.client.send(httpRequest, bodyHandler);
            }
            return response;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler, Optional<HttpResponse.PushPromiseHandler<T>> pushPromiseHandler) {
        return this.applicationSession
                .getSessionToken()
                .thenComposeAsync(sessionToken ->
                        (pushPromiseHandler.isPresent()
                                ? this.client.sendAsync(httpRequest, bodyHandler, pushPromiseHandler.get())
                                : this.client.sendAsync(httpRequest, bodyHandler))
                                .thenComposeAsync(response -> {
                                    if (isUnauthenticated(response) || isRedirectToCas(response)) {
                                        this.applicationSession.invalidateSession(sessionToken);
                                        return this.applicationSession
                                                .getSessionToken()
                                                .thenComposeAsync(s -> pushPromiseHandler.isPresent()
                                                        ? this.client.sendAsync(httpRequest, bodyHandler, pushPromiseHandler.get())
                                                        : this.client.sendAsync(httpRequest, bodyHandler));
                                    }
                                    return CompletableFuture.completedFuture(response);
                                }));
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler) {
        return this.sendAsync(httpRequest, bodyHandler, Optional.empty());
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return this.sendAsync(httpRequest, bodyHandler, Optional.of(pushPromiseHandler));
    }
}
