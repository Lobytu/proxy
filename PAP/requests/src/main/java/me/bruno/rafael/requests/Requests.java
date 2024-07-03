package me.bruno.rafael.requests;

import me.bruno.rafael.requests.decoder.RequestProxyURLDecoderBuilder;
import me.bruno.rafael.requests.protocol.decoder.ProtocolDecoderType;
import me.bruno.rafael.requests.protocol.http.HttpRequestController;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.List;

public class Requests {
    private static final List<String> BLACKLIST_COUNTRIES = List.of(
            "US", "CA", "BR", "HK", "RU", "DE", "FR", "IN", "AR", "IT", "NL", "ID", "KR", "PL", "GB", "SG", "AE", "CN",
            "BD");

    public static final String REQUEST_URL = "https://aesjt.pt/";

    public static final URI REQUEST_URI = URI.create(REQUEST_URL);

    private static final int MAX_PROXIES = 1000;

    public static void main(String[] args) {
      /*  new SocksRequestController(1, () -> RequestProxyURLDecoderBuilder.builder()
                .urlText("https://api.proxyscrape.com/v3/free-proxy-list/get?request=displayproxies&protocol=socks4,socks5&country=all&timeout=250&proxy_format=ipport&format=text")
                .type(ProtocolDecoderType.HOST_AND_PORT)
                .proxyType(Proxy.Type.SOCKS)
                .build()
                .create()
                .stream()
                .map(customProxy -> new SocksCustomProxySelector(customProxy.getProxy(), customProxy.getId()))
                .limit(MAX_PROXIES)
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));*/

        System.out.println("------------------------------------");

        new HttpRequestController(1, () -> RequestProxyURLDecoderBuilder.builder()
                .urlText("https://api.proxyscrape.com/v3/free-proxy-list/get?request=displayproxies&protocol=http&country=all&timeout=250&proxy_format=ipport&format=text")
                .elementsClass("blob-code blob-code-inner js-file-line")
                .type(ProtocolDecoderType.HOST_AND_PORT)
                .build()
                .create()
                .stream()
                .limit(MAX_PROXIES)
                .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll));
    }
}
