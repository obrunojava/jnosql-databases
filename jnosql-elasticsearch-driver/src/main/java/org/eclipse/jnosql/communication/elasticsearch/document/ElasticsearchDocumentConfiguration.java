/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *   Alessandro Moscatelli
 */
package org.eclipse.jnosql.communication.elasticsearch.document;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jsonb.JsonbJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.nosql.Configurations;
import jakarta.nosql.Settings;
import jakarta.nosql.document.DocumentConfiguration;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * The implementation of {@link DocumentConfiguration}
 * that returns {@link ElasticsearchDocumentManagerFactory}.
 *
 * @see ElasticsearchConfigurations
 */
public class ElasticsearchDocumentConfiguration implements DocumentConfiguration {

    private static final int DEFAULT_PORT = 9200;

    private List<HttpHost> httpHosts = new ArrayList<>();

    private List<Header> headers = new ArrayList<>();


    public ElasticsearchDocumentConfiguration() {

    }

    /**
     * Adds a host in the configuration
     *
     * @param host the host
     * @throws NullPointerException when host is null
     */
    public void add(HttpHost host) {
        this.httpHosts.add(Objects.requireNonNull(host, "host is required"));
    }

    /**
     * Adds a header in the configuration
     *
     * @param header the header
     * @throws NullPointerException when header is null
     */
    public void add(Header header) {
        this.headers.add(Objects.requireNonNull(header, "header is required"));
    }

    @Override
    public ElasticsearchDocumentManagerFactory apply(Settings settings) {
        requireNonNull(settings, "settings is required");

        settings.prefixSupplier(asList(ElasticsearchConfigurations.HOST, Configurations.HOST))
                .stream()
                .map(Object::toString)
                .map(h -> ElasticsearchAddress.of(h, DEFAULT_PORT))
                .map(ElasticsearchAddress::toHttpHost)
                .forEach(httpHosts::add);

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));
        builder.setDefaultHeaders(headers.stream().toArray(Header[]::new));

        final Optional<String> username = settings
                .getSupplier(asList(Configurations.USER,
                        ElasticsearchConfigurations.USER))
                .map(Object::toString);
        final Optional<String> password = settings
                .getSupplier(asList(Configurations.PASSWORD,
                        ElasticsearchConfigurations.PASSWORD))
                .map(Object::toString);

        if (username.isPresent()) {
            final CredentialsProvider credentialsProvider =
                    new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username.orElse(null), password.orElse(null)));
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider));
        }

        return this.get(builder);
    }

    /**
     * returns an {@link ElasticsearchDocumentManagerFactory} instance
     *
     * @param builder the builder {@link RestClientBuilder}
     * @return a manager factory instance
     * @throws NullPointerException when builder is null
     */
    public ElasticsearchDocumentManagerFactory get(RestClientBuilder builder) {
        Objects.requireNonNull(builder, "builder is required");
        return this.get(builder, new JsonbJsonpMapper());
    }

    /**
     * returns an {@link ElasticsearchDocumentManagerFactory} instance
     *
     * @param builder    the builder {@link RestClientBuilder}
     * @param jsonMapper the jsonMapper {@link JsonpMapper}
     * @return a manager factory instance
     * @throws NullPointerException when transport is null or jsonMapper is null
     */
    public ElasticsearchDocumentManagerFactory get(RestClientBuilder builder, JsonpMapper jsonMapper) {
        Objects.requireNonNull(builder, "builder is required");
        Objects.requireNonNull(jsonMapper, "jsonMapper is required");
        RestClient restClient = builder.build();
        return get(restClient,jsonMapper);
    }

    /**
     * returns an {@link ElasticsearchDocumentManagerFactory} instance
     *
     * @param restClient    the restClient {@link RestClient}
     * @param jsonMapper the jsonMapper {@link JsonpMapper}
     * @return a manager factory instance
     * @throws NullPointerException when restClient is null or jsonMapper is null
     */
    public ElasticsearchDocumentManagerFactory get(RestClient restClient, JsonpMapper jsonMapper) {
        Objects.requireNonNull(restClient, "restClient is required");
        Objects.requireNonNull(jsonMapper, "jsonMapper is required");
        var transport = new RestClientTransport(restClient, jsonMapper);
        return this.get(transport);
    }

    /**
     * returns an {@link ElasticsearchDocumentManagerFactory} instance
     *
     * @param transport the transport {@link ElasticsearchTransport}
     * @return a manager factory instance
     * @throws NullPointerException when transport is null
     */
    public ElasticsearchDocumentManagerFactory get(ElasticsearchTransport transport) {
        Objects.requireNonNull(transport, "transport is required");
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return this.get(client);
    }

    /**
     * returns an {@link ElasticsearchDocumentManagerFactory} instance
     *
     * @param client the client {@link ElasticsearchClient}
     * @return a manager factory instance
     * @throws NullPointerException when client is null
     */
    public ElasticsearchDocumentManagerFactory get(ElasticsearchClient client) {
        Objects.requireNonNull(client, "client is required");
        return new ElasticsearchDocumentManagerFactory(client);
    }


}
