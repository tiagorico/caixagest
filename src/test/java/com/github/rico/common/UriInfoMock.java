package com.github.rico.common;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.jaxrs.impl.UriInfoImpl;

import javax.ws.rs.core.MultivaluedMap;

import static java.util.Collections.singletonList;

/**
 * Description.
 *
 * @author Roberto Cortez
 */
public class UriInfoMock extends UriInfoImpl {
    private final MultivaluedMap<String, String> queryParameters;
    private final MultivaluedMap<String, String> pathParameters;

    private UriInfoMock(final MultivaluedMap<String, String> queryParameters,
                        MultivaluedMap<String, String> pathParameters) {
        super(null, null);
        this.queryParameters = queryParameters;
        this.pathParameters = pathParameters;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return pathParameters;
    }

    public static UriInfoMock empty() {
        return new UriInfoMock(new MetadataMap<>(), new MetadataMap<>());
    }

    public UriInfoMock addParam(final String key, final String value) {
        queryParameters.put(key, singletonList(value));
        return this;
    }

    public UriInfoMock addPath(final String key, final String value) {
        pathParameters.put(key, singletonList(value));
        return this;
    }

    public UriInfoMock paginate(final int offset, final int limit) {
        return addParam("offset", String.valueOf(offset)).addParam("limit", String.valueOf(limit));
    }
}
