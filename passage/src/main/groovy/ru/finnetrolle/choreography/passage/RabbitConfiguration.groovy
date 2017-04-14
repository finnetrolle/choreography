package ru.finnetrolle.choreography.passage

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.support.RetryTemplate

@EnableRabbit
@Configuration
class RabbitConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfiguration.class);

    @Value('${rabbit.connection.hostname}')
    private String hostname;

    @Value('${rabbit.connection.username}')
    private String username;

    @Value('${rabbit.connection.password}')
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        log.info("Creating connection factory");
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setAddresses(hostname);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setConnectionTimeout(1000);
        factory.setRequestedHeartBeat(100);
        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        log.info("Creating amqp admin");
        RabbitAdmin admin = new RabbitAdmin(connectionFactory());
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        log.info("Creating rabbit template");
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(jsonMessageConverter());
        RetryTemplate retry = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(3600000);
        retry.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retry);
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        log.info("Creating container factory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(5);
        factory.setPrefetchCount(1);
        factory.setReceiveTimeout(1000L);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }

}
