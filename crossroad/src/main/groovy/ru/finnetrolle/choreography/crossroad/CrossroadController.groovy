package ru.finnetrolle.choreography.crossroad

import groovy.transform.Immutable
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.AsyncRabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.util.concurrent.ListenableFuture
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RestController
class CrossroadController {

    @Autowired
    private AmqpTemplate template

    @GetMapping("/pay")
    PayResponse pay(@RequestParam("method") String method, @RequestParam("amount") BigDecimal amount) {

        CompletableFuture<String> receiver = CompletableFuture.completedFuture(template.convertSendAndReceive("choreo_prime", "payment.${method}", amount))
        def result = receiver.get(10000, TimeUnit.MILLISECONDS)
        return new PayResponse(method, amount, result)
    }

    @Immutable
    class PayResponse {
        String method
        BigDecimal amount
        String result
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    void handler() {}
}
