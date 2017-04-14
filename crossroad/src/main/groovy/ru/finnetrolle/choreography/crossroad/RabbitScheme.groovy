package ru.finnetrolle.choreography.crossroad

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class RabbitScheme {

    @Autowired AmqpTemplate template

    @PostConstruct
    def init() {
        primeBinding()
        failoverBinding()
        failedBinding()
        doneBinding()
        template.convertAndSend("amq.rabbitmq.trace", "", "RISE!")
    }

    @Bean
    Queue primaryQueue() {
        return new Queue("primary", true, false, false, [
                'x-message-ttl'         : 3000,
                'x-dead-letter-exchange': 'choreo_failover'])
    }

    @Bean
    Queue failoverQueue() {
        return new Queue("failover", true, false, false, [
                'x-message-ttl'         : 7000,
                'x-dead-letter-exchange': 'choreo_failed'])
    }

    @Bean
    Queue failedQueue() {
        return new Queue("failed", true, false, false)
    }

    @Bean
    Queue doneQueue() {
        return new Queue("done", true, false, false, ['x-message-ttl': 10000])
    }

    @Bean
    TopicExchange doneExchange() {
        return new TopicExchange("choreo_done", true, false)
    }

    @Bean
    TopicExchange failedExchange() {
        return new TopicExchange("choreo_failed", true, false)
    }

    @Bean
    TopicExchange failoverExchange() {
        return new TopicExchange("choreo_failover", true, false)
    }

    @Bean
    TopicExchange primeExchange() {
        return new TopicExchange("choreo_prime", true, false)
    }

    @Bean
    org.springframework.amqp.core.Binding primeBinding() {
        return BindingBuilder.bind(primaryQueue()).to(primeExchange()).with("payment.default")
    }

    @Bean
    org.springframework.amqp.core.Binding failoverBinding() {
        return BindingBuilder.bind(failoverQueue()).to(failoverExchange()).with("#")
    }

    @Bean
    org.springframework.amqp.core.Binding failedBinding() {
        return BindingBuilder.bind(failedQueue()).to(failedExchange()).with("#")
    }

    @Bean
    org.springframework.amqp.core.Binding doneBinding() {
        return BindingBuilder.bind(doneQueue()).to(doneExchange()).with("#")
    }
}
