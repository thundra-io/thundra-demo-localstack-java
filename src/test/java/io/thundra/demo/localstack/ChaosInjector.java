package io.thundra.demo.localstack;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import io.thundra.agent.lambda.localstack.FunctionEnvironmentInfo;
import io.thundra.agent.lambda.localstack.FunctionEnvironmentInitializer;
import io.thundra.agent.trace.TraceSupport;
import io.thundra.agent.trace.span.impl.ErrorInjectorSpanListener;
import io.thundra.agent.trace.span.impl.FilteringSpanListener;

/**
 * @author oguzhan
 */
public final class ChaosInjector {

    private ChaosInjector() {
    }

    private static abstract class FunctionNameAwareEnvironmentInitializer<C>
            implements FunctionEnvironmentInitializer<C> {

        private final String functionName;

        private FunctionNameAwareEnvironmentInitializer(String functionName) {
            this.functionName = functionName;
        }

        @Override
        public final C beforeInit(FunctionEnvironmentInfo functionEnvironmentInfo) {
            if (functionEnvironmentInfo.getFunctionName().endsWith(functionName)) {
                return doBeforeInit(functionEnvironmentInfo);
            }
            return null;
        }

        protected C doBeforeInit(FunctionEnvironmentInfo functionEnvironmentInfo) {
            return null;
        }

        @Override
        public final void afterInit(FunctionEnvironmentInfo functionEnvironmentInfo, C context) {
            if (functionEnvironmentInfo.getFunctionName().endsWith(functionName)) {
                doAfterInit(functionEnvironmentInfo, context);
            }
        }

        protected void doAfterInit(FunctionEnvironmentInfo functionEnvironmentInfo, C context) {
        }

    }

    /*
     * This must be defined in static context because initializer defined here
     * is serialized into function sandbox environment.
     */
    public static FunctionEnvironmentInitializer createDynamoDBChaosInjector(String functionName) {
        return new FunctionNameAwareEnvironmentInitializer(functionName) {
            @Override
            protected void doAfterInit(FunctionEnvironmentInfo functionEnvironmentInfo, Object context) {
                ChaosInjector.injectDynamoDBError();
            }
        };
    }

    public static void injectDynamoDBError() {
        ErrorInjectorSpanListener errorListener =
                ErrorInjectorSpanListener.
                        builder().
                        errorType(AmazonDynamoDBException.class).
                        errorMessage("As AWS CTO Werner Vogels said, 'Everything fails, all the time.'").
                        injectPercentage(100).
                        build();
        FilteringSpanListener errorFilteringSpanListener =
                FilteringSpanListener.
                        builder().
                        listener(errorListener).
                        filter(FilteringSpanListener.
                                    filterBuilder().
                                    className("AWS-DynamoDB").
                                    build()).
                        build();
        TraceSupport.registerSpanListener(errorFilteringSpanListener);
    }

}
