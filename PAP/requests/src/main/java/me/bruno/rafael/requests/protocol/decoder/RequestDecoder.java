package me.bruno.rafael.requests.protocol.decoder;

import me.bruno.rafael.requests.blacklist.BlackListController;
import me.bruno.rafael.requests.blacklist.SimpleBlackListFactory;
import me.bruno.rafael.requests.model.CustomProxy;

import java.util.List;
import java.util.logging.Logger;

public interface RequestDecoder {

    Logger LOGGER = Logger.getLogger(RequestDecoder.class.getName());

    List<CustomProxy> create();

    default void applyBlacklist(List<CustomProxy> customProxies) {
        final int size = customProxies.size();

        SimpleBlackListFactory blackListFactory =
                BlackListController.getInstance().getFactory();

        customProxies.removeIf(customProxy -> blackListFactory.contains(customProxy.getId()));

        LOGGER.info("Blacklist applied, removed " + (size - customProxies.size()) + " proxies");
    }
}
