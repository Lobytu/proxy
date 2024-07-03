package me.bruno.rafael.requests.protocol.http;

import me.bruno.rafael.requests.Requests;
import me.bruno.rafael.requests.model.CustomProxy;
import me.bruno.rafael.requests.scheduler.RequestsTimerScheduler;
import me.bruno.rafael.requests.util.HttpRequestUtil;

import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class HttpRequestController {

    public static final int DELAY_IN_MILLIS = 2000;

    private static final Logger LOGGER = Logger.getLogger("[HttpLogger]");

    private static final String SKIPPING_FORMAT = "[HttpLogger] Invalid proxy: %s (%sth | %s S)";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static int successfullyProxies;

    private static Queue<CustomProxy> proxies;

    private final Supplier<Queue<CustomProxy>> updater;

    private final int maxProxiesRefresh;

    private int proxyCounter, updaterCounter;

    public HttpRequestController(int maxProxiesRefresh, Supplier<Queue<CustomProxy>> updater) {
        this(maxProxiesRefresh, updater, 1);
    }

    public HttpRequestController(int maxProxiesRefresh, Supplier<Queue<CustomProxy>> updater, int threads) {
        proxies = updater.get();

        this.updater = updater;
        this.maxProxiesRefresh = Math.max(maxProxiesRefresh, proxies.size());

        for (int i = 0; i < threads; i++) {
            new RequestsTimerScheduler(DELAY_IN_MILLIS, this::nextProxy);
        }
    }

    public HttpRequestController(int i, Object o, Object o1, Object collect, int i1) {
    }

    public void nextProxy() {
        EXECUTOR_SERVICE.submit(() -> {
            if (proxies.isEmpty()) {
                refreshProxies();
                return;
            }

            CustomProxy currentProxy = proxies.poll();
            if (currentProxy == null) return;

            LOGGER.info("Testing proxy: " + currentProxy.getId());

            ProxySelector.setDefault(null);

            final HttpURLConnection connection =
                    HttpRequestUtil.createUrlConnection(currentProxy.getProxy(), Requests.REQUEST_URI);

            try {
                if (connection != null && connection.getResponseCode() == 200) {
                    LOGGER.info(
                            String.format("Success proxy: [%s] | %s | ", currentProxy.getId(), successfullyProxies));

                    successfullyProxies++;
                } else {
                    printInvalidAndRemove(currentProxy);
                }
            } catch (Exception ignored) {
                printInvalidAndRemove(currentProxy);
            }

            //if (connection != null) connection.disconnect();
        });
    }

    public boolean refreshProxies() {
        if (updater != null && this.updaterCounter >= this.maxProxiesRefresh) {
            this.updaterCounter = 0;

            Queue<CustomProxy> newProxies;

            try {
                newProxies = updater.get();

                proxies = newProxies == null || newProxies.isEmpty() ? proxies : newProxies;

                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        return false;
    }

    private void printInvalidAndRemove(CustomProxy proxy) {
        remove(proxy.getId());

        final String message = String.format(SKIPPING_FORMAT, proxy.getId(), proxyCounter++, successfullyProxies);

        System.out.println(message);
    }

    private void remove(String id) {
        this.updaterCounter++;

        proxies.removeIf(proxy -> proxy.getId().equals(id));
    }
}