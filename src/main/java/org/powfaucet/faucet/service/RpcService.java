package org.powfaucet.faucet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RpcService {

    private final URI url;
    private final String username;
    private final String password;

    public RpcService(
            @Value("${org.powfaucet.faucet.rpc.url}") String url,
            @Value("${org.powfaucet.faucet.rpc.username}") String username,
            @Value("${org.powfaucet.faucet.rpc.password}") String password
    ) throws URISyntaxException {
        this.url = new URI(url);
        this.username = username;
        this.password = password;
    }

    public String requestSync(String request) {
        AtomicReference<String> ref = new AtomicReference<>(null);
        requestAsync(request)
                .thenAccept(ref::set)
                .join();

        return ref.get();
    }

    private CompletableFuture<String> requestAsync(String rpc) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Authorization", basicAuth(username, password))
                .POST(HttpRequest.BodyPublishers.ofString(rpc, StandardCharsets.UTF_8))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}