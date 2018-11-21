package test;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jpa.DeleteHandler;
import org.apache.camel.component.jpa.JpaComponent;
import org.apache.camel.component.jpa.JpaEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.Detail;
import test.data.Master;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;

import static java.lang.System.out;

/**
 * A Camel Java DSL Router
 */
public class MyDBRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyDBRouteBuilder.class);

    public void configure() throws Exception {
        ExpressionAdapter correlationExpression = new ExpressionAdapter() {
            @Override
            public String evaluate(Exchange exchange) {
                Master entity = getEntity(exchange);
                return String.valueOf(entity.groupId);
            }

            private Master getEntity(Exchange exchange) {
                return exchange.getIn().getBody(Master.class);
            }

        };
        DeleteHandler deleteHandler = (entityManager, entityBean, exchange) -> {
            Master master = (Master) entityBean;
            Query detailQuery = entityManager.createNativeQuery("DELETE FROM DETAIL WHERE stepNumber=" + master.masterID.stepNumber + " AND stepName='" + master.masterID.stepName + "'");
            detailQuery.executeUpdate();
            Query masterQuery = entityManager.createNativeQuery("DELETE FROM MASTER WHERE stepNumber=" + master.masterID.stepNumber + " AND stepName='" + master.masterID.stepName + "'");
            masterQuery.executeUpdate();
        };
        JpaComponent jpaComponent = new JpaComponent();
        jpaComponent.setCamelContext(new DefaultCamelContext());
        JpaEndpoint endpoint = (JpaEndpoint) jpaComponent.createEndpoint("jpa:test.data.Master");
        endpoint.setDeleteHandler(deleteHandler);
        endpoint.setNamedQuery("selectAll");
        endpoint.setDelay(60000);
        endpoint.setTransacted(true);
        endpoint.setConsumeDelete(true);
        from(endpoint)
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
                Master entity = (Master) listItem;
                out.println(entity.masterID.stepName + " | " + entity.groupId);
            }
            out.println("-------------------------------------");
        }
    }
}
