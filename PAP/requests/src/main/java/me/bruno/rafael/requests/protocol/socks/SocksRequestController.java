package me.bruno.rafael.requests.protocol.socks;

import me.bruno.rafael.requests.Requests;
import me.bruno.rafael.requests.blacklist.BlackListController;
import me.bruno.rafael.requests.blacklist.SimpleBlackListFactory;
import me.bruno.rafael.requests.protocol.socks.provider.SocksProxyProvider;
import me.bruno.rafael.requests.protocol.socks.proxy.SocksCustomProxySelector;
import me.bruno.rafael.requests.scheduler.RequestsTimerScheduler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.ProxySelector;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SocksRequestController {

    public static final int DEFAULT_TIMEOUT = 1500, MAX_RETRIES_BY_CLOUDFLARE = 2;

    private static final Logger LOGGER = Logger.getLogger("[SocksLogger]");

    private static final String SKIPPING_FORMAT = "[SocksLogger] Invalid proxy: %s (%sth | %s S)";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private Queue<SocksCustomProxySelector> proxies;

    private final Supplier<Queue<SocksCustomProxySelector>> updater;

    private final int maxProxiesRefresh;

    private int proxyCounter, updaterCounter, successfullyProxies;

    private final SimpleBlackListFactory blackListFactory =
            BlackListController.getInstance().getFactory();

    private boolean ignoreBlacklist;

    public SocksRequestController(int maxProxiesRefresh, Supplier<Queue<SocksCustomProxySelector>> updater) {
        this.updater = updater;
        this.proxies = updater.get();
        this.maxProxiesRefresh = Math.max(maxProxiesRefresh, proxies.size());

        new RequestsTimerScheduler(1, this::nextProxy);
    }

    public SocksRequestController(int maxProxiesRefresh, Supplier<Queue<SocksCustomProxySelector>> updater, boolean ignoreBlacklist) {
        this.updater = updater;
        this.proxies = updater.get();
        this.maxProxiesRefresh = maxProxiesRefresh;
        this.ignoreBlacklist = ignoreBlacklist;

        new RequestsTimerScheduler(Math.min(2, DEFAULT_TIMEOUT / 1000), this::nextProxy);
    }

    public void nextProxy() {
        if (proxies.isEmpty()) {
            refreshProxies();
            return;
        }

        SocksCustomProxySelector currentProxy = proxies.peek();

        if (currentProxy == null) return;

        if (ignoreBlacklist) {
            proxies.removeIf(proxy -> proxy.getId().equals(currentProxy.getId()));
        } else if (blackListFactory.contains(currentProxy.getId())) {
            try {
                proxies.removeIf(proxy -> proxy.getId().equals(currentProxy.getId()));
            } catch (Exception ignored) {}
            return;
        }

        if (refreshProxies()) {
            return;
        }

        blackListFactory.add(currentProxy.getId());

        EXECUTOR_SERVICE.submit(() -> {
            ProxySelector.setDefault(currentProxy);

            final URI uri = Requests.REQUEST_URI;
            final HttpGet httpRequest = new HttpGet(uri);

            try (CloseableHttpClient closeableHttpClient = SocksProxyProvider.createClient()) {
                if (closeableHttpClient == null) return;

                try (CloseableHttpResponse response = closeableHttpClient.execute(httpRequest)) {
                    final String responseEntity = EntityUtils.toString(response.getEntity());

                    if (responseEntity.contains("cloudflare")) { // check firewall
                        currentProxy.addRetry();

                        if (currentProxy.getRetries() >= MAX_RETRIES_BY_CLOUDFLARE) {
                            remove(currentProxy.getId());
                        }

                        LOGGER.info(String.format(
                                SKIPPING_FORMAT,
                                currentProxy.getId() + " (Retry CF: " + currentProxy.getRetries() + "th)",
                                proxyCounter++,
                                successfullyProxies));
                    } else {
                        successfullyProxies++;
                        proxyCounter++;

                        LOGGER.info(String.format("Success proxy: [%s] | %s", currentProxy.getId(), responseEntity));

                        remove(currentProxy.getId());
                    }
                }
            } catch (Exception ignored) {
                remove(currentProxy.getId());

                final String message =
                        String.format(SKIPPING_FORMAT, currentProxy.getId(), proxyCounter++, successfullyProxies);

                System.out.println(message);
            }
        });
    }

    public boolean refreshProxies() {
        if (updater != null && this.updaterCounter >= 10) {
            this.updaterCounter = 0;

            Queue<SocksCustomProxySelector> newProxies;

            try {
                newProxies = updater.get();

                this.proxies = newProxies == null || newProxies.isEmpty() ? this.proxies : newProxies;

                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        return false;
    }

    private void remove(String currentProxyId) {
        this.updaterCounter++;

        proxies.removeIf(proxy -> proxy.getId().equals(currentProxyId));
    }
}
