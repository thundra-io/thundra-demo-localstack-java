package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.thundra.agent.lambda.core.handler.request.LambdaRequestHandler;
import io.thundra.demo.localstack.service.HelloService;

/**
 * @author serkan
 */
public class HelloHandler
        implements LambdaRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final HelloService helloService = new HelloService();

    @Override
    public APIGatewayProxyResponseEvent doHandleRequest(APIGatewayProxyRequestEvent request,
                                                        Context context) {
        String name = request.getQueryStringParameters().get("name");
        context.getLogger().log("Saying hello to " + name);
        String helloMessage = helloService.sayHello(name);
        return new APIGatewayProxyResponseEvent().
                withStatusCode(200).
                withBody(helloMessage);
    }

}
