package io.thundra.demo.localstack.service;

import io.thundra.agent.trace.TraceSupport;
import io.thundra.agent.trace.span.impl.ErrorInjectorSpanListener;
import io.thundra.agent.trace.span.impl.FilteringSpanListener;

import java.util.Arrays;

public class ChaosInjector {
    public static void call() {
        ErrorInjectorSpanListener errorListener = new ErrorInjectorSpanListener(
                false,
                java.sql.SQLSyntaxErrorException.class,
                "X!",
                100);
        FilteringSpanListener.SpanFilter filter = new FilteringSpanListener.SimpleSpanFilter(
                null,
                "AWS-DynamoDB",
                null,
                null,
                false
        );
        FilteringSpanListener errorFilteringSpanListener = new FilteringSpanListener(errorListener, Arrays.asList(filter));
        TraceSupport.registerSpanListener(errorFilteringSpanListener);
    }
}
