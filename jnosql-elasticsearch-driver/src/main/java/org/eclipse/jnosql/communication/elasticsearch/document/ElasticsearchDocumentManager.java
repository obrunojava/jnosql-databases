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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.communication.elasticsearch.document;


import co.elastic.clients.elasticsearch.core.SearchRequest;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentManager;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.stream.Stream;

/**
 * The ES implementation of {@link DocumentManager}
 */
public interface ElasticsearchDocumentManager extends DocumentManager {

    /**
     * Find entities from {@link SearchRequest}
     *
     * @param query the search request builder
     * @return the objects from search
     * @throws NullPointerException when the search request builder is null
     */
    Stream<DocumentEntity> search(SearchRequest query) throws NullPointerException;

    /**
     * Find entities from {@link QueryBuilder}
     *
     * @param query the query
     * @return the objects from query
     * @throws NullPointerException when query is null
     */
    Stream<DocumentEntity> search(QueryBuilder query) throws NullPointerException;

}
