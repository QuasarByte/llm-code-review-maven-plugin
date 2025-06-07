package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PProxy;

import java.net.Proxy;

public interface PProxyMapper {
    Proxy map(PProxy proxy);
}
