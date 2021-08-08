package io.thundra.demo.localstack.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import io.thundra.agent.lambda.core.handler.request.LambdaRequestHandler;

import java.util.List;

import static io.thundra.demo.localstack.service.ClientBuilder.buildStepFunctions;

public class Worker implements LambdaRequestHandler<SQSEvent, Void> {

    public static final String STATE_MACHINE_ARN = "arn:aws:states:us-east-1:000000000000:stateMachine:processingStateMachine";

    @Override
    public Void doHandleRequest(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        AWSStepFunctions stepFunctions = buildStepFunctions();
        records.forEach(sqsMessage -> stepFunctions.startExecution(new StartExecutionRequest()
                .withStateMachineArn(STATE_MACHINE_ARN).withInput(sqsMessage.getBody())));
        return null;
    }
}
