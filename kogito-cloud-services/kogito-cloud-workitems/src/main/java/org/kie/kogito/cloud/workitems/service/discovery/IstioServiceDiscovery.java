/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.cloud.workitems.service.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.kogito.cloud.kubernetes.client.KogitoKubeClient;
import org.kie.kogito.cloud.kubernetes.client.operations.MapWalker;
import org.kie.kogito.cloud.workitems.ServiceInfo;

public class IstioServiceDiscovery extends BaseServiceDiscovery {

    private static final String SERVICE_KEY_HOST = "HOST";
    private static final String KEY_STATUS = "status";
    private static final String KEY_DOMAIN = "domain";

    private final String istioGatewayUrl;

    public IstioServiceDiscovery(final KogitoKubeClient kubeClient, final String istioGatewayUrl) {
        super(kubeClient);
        this.istioGatewayUrl = istioGatewayUrl;
    }

    @Override
    protected List<Map<String, Object>> query(String namespace, Map<String, String> labels) {
        return kubeClient.knativeService()
                         .listNamespaced(namespace, labels)
                         .asMapWalker()
                         .mapToListMap(KEY_ITEMS)
                         .asList();
    }

    @Override
    protected ServiceInfo buildService(List<Map<String, Object>> services, final String service) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(SERVICE_KEY_HOST,
                    new MapWalker(services.get(0)).mapToMap(KEY_STATUS).asMap().get(KEY_DOMAIN).toString());
        LOGGER.debug("Headers to be used for requests {}", headers);
        return new ServiceInfo(String.format("%s/%s", this.istioGatewayUrl, service), headers);
    }

}
