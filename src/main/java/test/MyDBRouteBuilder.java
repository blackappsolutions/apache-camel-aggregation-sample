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
        ExpressionAdapter grpCnt = new ExpressionAdapter() {
            @Override
            public String evaluate(Exchange exchange) {
                return String.valueOf(exchange.getIn().getBody(MultiSteps.class).grpCnt);
            }
        };
        from("jpa://test.MultiSteps?consumeDelete=true")
                .aggregate(grpCnt, new GroupedBodyAggregationStrategy()).completionSize(grpCnt)
                .log(LoggingLevel.DEBUG, LOGGER, "trx_imp_notification")
                .process(new MyProcessor())
                .marshal().string("UTF-8")
                .to("file:target/messages/others");
    }

    private class MyProcessor implements Processor {
        @Override
        public void process(Exchange exchange) {
            ArrayList list = exchange.getIn().getBody(ArrayList.class);
            out.println(list.size());
        }
    }
}
