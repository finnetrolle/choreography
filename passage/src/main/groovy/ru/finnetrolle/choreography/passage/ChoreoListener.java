package ru.finnetrolle.choreography.passage;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RabbitListener(bindings = {
        @QueueBinding(
                value = @Queue(value = "${service.name}", durable = "true", autoDelete = "true"),
                exchange = @Exchange(value = "choreo_prime", ignoreDeclarationExceptions = "true"),
                key = "payment.${service.name}"),
        @QueueBinding(
                value = @Queue(value = "${additional.read.from}", durable = "true", autoDelete = "false", ignoreDeclarationExceptions = "true"),
                exchange = @Exchange(value = "choreo_prime", ignoreDeclarationExceptions = "true"),
                key = "#")})
public @interface ChoreoListener {
}
