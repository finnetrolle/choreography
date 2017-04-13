package ru.finnetrolle.choreography.passage

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicInteger

@Component
class Listener {

    @Value('${service.name}') String name

    @Autowired AmqpTemplate template

    private AtomicInteger counter = new AtomicInteger(0)

    @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = '${service.name}', durable = "true", autoDelete = "true"),
                exchange = @Exchange(value = "choreo_prime", ignoreDeclarationExceptions = "true"),
                key = 'payment.${service.name}'))
    String handleDirect(String data) {
        def i = counter.incrementAndGet()
        println "$i\t>>\t$name received from DIRECT: $data"
        return "$data;$name;${UUID.randomUUID().toString()}".toString()
//        template.convertAndSend("choreo_done", "", "$data;${UUID.randomUUID().toString()}")
    }

    @RabbitListener(queues = '${additional.read.from}')
    String handleDefault(String data) {
        def i = counter.incrementAndGet()
        println "$i\t>>\t$name received from DEFAULT: $data"
        return "$data;$name;${UUID.randomUUID().toString()}".toString()
//        template.convertAndSend("choreo_done", "", "$data;${UUID.randomUUID().toString()}")
    }

}
