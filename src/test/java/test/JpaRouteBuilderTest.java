package test;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.ExpressionAdapter;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import test.data.Master;
import test.data.MasterID;

import java.util.ArrayList;
import java.util.List;

public class JpaRouteBuilderTest extends CamelTestSupport {

    private static final String DIRECT_START = "direct:start";
    private static final String MOCK_RESULT = "mock:result";

    @Produce(uri = DIRECT_START)
    private ProducerTemplate template;

    @EndpointInject(uri = MOCK_RESULT)
    private MockEndpoint resultEndpoint;

    @Test
    public void testAggregation() throws Exception {

        Master a = createMaster(1, "a", 1);
        Master b = createMaster(2, "b", 1);
        Master c = createMaster(3, "c", 2);
        Master d = createMaster(4, "d", 3);

        resultEndpoint.expectedBodiesReceived(createExpectedResult(a, b, c, d));

        template.sendBody(a);
        template.sendBody(b);
        template.sendBody(c);
        template.sendBody(d);

        resultEndpoint.assertIsSatisfied();
    }

    private List<List> createExpectedResult(Master a, Master b, Master c, Master d) {
        List<Master> group1 = new ArrayList<>();
        group1.add(a);
        group1.add(b);

        List<Master> group2 = new ArrayList<>();
        group2.add(c);

        List<Master> group3 = new ArrayList<>();
        group3.add(d);

        List<List> expectedResult = new ArrayList<>();
        expectedResult.add(group1);
        expectedResult.add(group2);
        expectedResult.add(group3);
        return expectedResult;
    }

    private Master createMaster(int number, String name, int groupId) {
        Master m = new Master();
        m.masterID = new MasterID();
        m.masterID.stepNumber = number;
        m.masterID.stepName = name;
        m.groupId = groupId;
        return m;
    }

    JpaRouteBuilder testee = new JpaRouteBuilder();

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                    .aggregate(new ExpressionAdapter() {
                        @Override
                        public Object evaluate(Exchange exchange) {
                            // We have to set teh BATCH_SIZE header explicitly, because DirectConsumer is no
                            // "Batch Consumer". See http://camel.apache.org/batch-consumer.html for details.
                            /////////////////////////////////////////////////////////////////////////////////
                            exchange.setProperty(Exchange.BATCH_SIZE, 4);
                            return testee.correlationExpression.evaluate(exchange);
                        }
                    }, testee.groupedBodyAggregationStrategy)
                    /*  http://camel.apache.org/aggregator2.html - completionFromBatchConsumer
                        ----------------------------------------------------------------------
                        This option is if the exchanges are coming from a Batch Consumer. Then when enabled the
                        Aggregator2 will use the batch size determined by the Batch Consumer in the message header
                        CamelBatchSize. See more details at Batch Consumer. This can be used to aggregate all files
                        consumed from a File endpoint in that given poll.
                     */
                    .completionFromBatchConsumer()
                    .process(testee.processor)
                    .to("mock:result");
            }
        };
    }
}
