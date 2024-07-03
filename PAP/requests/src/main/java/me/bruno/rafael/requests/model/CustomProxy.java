package me.bruno.rafael.requests.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.Proxy;

@RequiredArgsConstructor
@Getter
public class CustomProxy {

    private final String id;
    private final Proxy proxy;
}
