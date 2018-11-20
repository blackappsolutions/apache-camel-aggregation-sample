package test;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.out;

/**
 * A Camel Java DSL Router
 */
public class MyDBRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyDBRouteBuilder.class);

    public void configure() {
        ExpressionAdapter correlationExpression = new ExpressionAdapter() {
            @Override
            public String evaluate(Exchange exchange) {
                return String.valueOf(getEntity(exchange).groupId);
            }

            private MultiSteps getEntity(Exchange exchange) {
                return exchange.getIn().getBody(MultiSteps.class);
            }

        };
        from("jpa://test.MultiSteps" +
                "?consumer.query=Select mul from test.MultiSteps as mul order by mul.step, mul.groupId asc" +
                "&consumeDelete=true")
                .aggregate(
                        correlationExpression,
                        new GroupedBodyAggregationStrategy()
                )
                .completionFromBatchConsumer()
                .log(LoggingLevel.DEBUG, LOGGER, "trx_imp_notification")
                .process(new MyProcessor())
                .marshal().string("UTF-8")
                .to("file:target/messages/others");
    }

    private class MyProcessor implements Processor {
        @Override
        public void process(Exchange exchange) {
            ArrayList list = exchange.getIn().getBody(ArrayList.class);
            out.println("-------------------------------------");
            for (Object listItem : list) {
                MultiSteps entity = (MultiSteps) listItem;
                out.println(entity.step + " | " + entity.groupId);
            }
            out.println("-------------------------------------");
        }
    }
}
