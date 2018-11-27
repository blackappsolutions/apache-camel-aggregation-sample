package test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jpa.JpaComponent;
import org.apache.camel.component.jpa.JpaConstants;
import org.apache.camel.component.jpa.JpaEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.data.Master;

import static java.lang.System.out;

/**
 * A Camel Java DSL Router
 */
public class JpaRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaRouteBuilder.class);
    private static final String DO_NOT_DELETE = "doNotDelete";

    public void configure() throws Exception {
        from(createProducer("jpa:test.data.Master"))
                .aggregate(correlationExpression, groupedBodyAggregationStrategy)
                .completionFromBatchConsumer()
                .log(LoggingLevel.DEBUG, LOGGER, "trx_imp_notification")
                .process(processor)
                .choice()
                .when(header(DO_NOT_DELETE).isEqualTo(true))
                .stop()
                .otherwise()
                .process(deletionHandler)
                .to(createConsumer())
                .end();
    }

    private String createConsumer() {
        return "file:target";
    }

    private Endpoint createProducer(String uri) throws Exception {
        JpaComponent jpaComponent = new JpaComponent();
        jpaComponent.setCamelContext(new DefaultCamelContext());
        JpaEndpoint endpoint = (JpaEndpoint) jpaComponent.createEndpoint(uri);
        endpoint.setNamedQuery(Master.SELECT_ALL);
        endpoint.setDelay(60000);
        endpoint.setTransacted(true);
        endpoint.setConsumeDelete(false);
        return endpoint;
    }

    private Processor deletionHandler = exchange -> {
        Message in = exchange.getIn();
        EntityManager entityManager = in.getHeader(JpaConstants.ENTITY_MANAGER, EntityManager.class);
        List<Master> masterList = (List<Master>) in.getBody();
        for (Master master : masterList) {
            entityManager.createNativeQuery(createDeleteStmt(master, "DETAIL")).executeUpdate();
            entityManager.createNativeQuery(createDeleteStmt(master, "MASTER")).executeUpdate();
        }
    };

    private String createDeleteStmt(Master master, String tableName) {
        return "DELETE FROM " + tableName + " WHERE stepNumber=" + master.masterID.stepNumber + " AND stepName='" +
                master.masterID.stepName + "'";
    }

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

    boolean b = false;

    Processor processor = exchange -> {

        ArrayList list = exchange.getIn().getBody(ArrayList.class);
        out.println("-------------------------------------");
        for (Object listItem : list) {
            Master entity = (Master) listItem;
            out.println(entity.masterID.stepName + " | " + entity.groupId);
        }
        out.println("-------------------------------------");

        if (b) {
            exchange.getIn().setHeader(DO_NOT_DELETE, true);
        }

        if (!b) {
            b = true;
        }
    };

}
