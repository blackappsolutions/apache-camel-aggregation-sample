package test;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jpa.DeleteHandler;
import org.apache.camel.component.jpa.JpaComponent;
import org.apache.camel.component.jpa.JpaEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.Master;

import javax.persistence.Query;
import java.util.ArrayList;

import static java.lang.System.out;

/**
 * A Camel Java DSL Router
 */
public class JpaRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaRouteBuilder.class);

    public void configure() throws Exception {
        from(createProducer("jpa:test.data.Master"))
            .aggregate(correlationExpression, groupedBodyAggregationStrategy)
            .completionFromBatchConsumer()
            .log(LoggingLevel.DEBUG, LOGGER, "trx_imp_notification")
            .process(processor)
            .to(createConsumer())
            .end();
    }

    String createConsumer() {
        return "file:target";
    }

    Endpoint createProducer(String uri) throws Exception {
        JpaComponent jpaComponent = new JpaComponent();
        jpaComponent.setCamelContext(new DefaultCamelContext());
        JpaEndpoint endpoint = (JpaEndpoint) jpaComponent.createEndpoint(uri);
        endpoint.setDeleteHandler(deletionHandler);
        endpoint.setNamedQuery(Master.SELECT_ALL);
        endpoint.setDelay(60000);
        endpoint.setTransacted(true);
        return endpoint;
    }

    DeleteHandler deletionHandler = (entityManager, entityBean, exchange) -> {
        Master master = (Master) entityBean;
        Query detailQuery = entityManager.createNativeQuery("DELETE FROM DETAIL WHERE stepNumber=" + master.masterID.stepNumber + " AND stepName='" + master.masterID.stepName + "'");
        detailQuery.executeUpdate();
        Query masterQuery = entityManager.createNativeQuery("DELETE FROM MASTER WHERE stepNumber=" + master.masterID.stepNumber + " AND stepName='" + master.masterID.stepName + "'");
        masterQuery.executeUpdate();
    };

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

    AggregationStrategy groupedBodyAggregationStrategy = new AbstractListAggregationStrategy<Object>() {
        public Object getValue(Exchange exchange) {
            return exchange.getIn().getBody();
        }
    };

    Processor processor = exchange -> {
        ArrayList list = exchange.getIn().getBody(ArrayList.class);
        out.println("-------------------------------------");
        for (Object listItem : list) {
            Master entity = (Master) listItem;
            out.println(entity.masterID.stepName + " | " + entity.groupId);
        }
        out.println("-------------------------------------");
    };


}
