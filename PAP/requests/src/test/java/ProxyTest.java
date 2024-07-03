import me.bruno.rafael.requests.decoder.RequestProxyURLDecoderBuilder;
import me.bruno.rafael.requests.protocol.decoder.ProtocolDecoderType;
import me.bruno.rafael.requests.protocol.http.HttpRequestController;
import java.util.ArrayDeque;
import java.net.Proxy;

public class ProxyTest {

    public static void main(String[] args) {
        new HttpRequestController(
                10000,
                () -> RequestProxyURLDecoderBuilder.builder()
                        .urlText("https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks4&timeout=10000&country=all&ssl=all&anonymity=all")
                        .type(ProtocolDecoderType.HOST_AND_PORT)
                        .ignoreBlacklist(true)
                        .proxyType(Proxy.Type.SOCKS)
                        .build()
                        .create()
                        .stream()
                        .collect(ArrayDeque::new, ArrayDeque::add, ArrayDeque::addAll),
                20);
    }
}
