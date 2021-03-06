package io.micronaut.spring.web.annotation;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static org.junit.Assert.*;

import javax.validation.constraints.Pattern;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to Micronaut for Spring!");
        return "welcome";
    }

    @PostMapping("/request")
    public Flux<String> request(ServerHttpRequest request, HttpMethod method) {
        assertEquals("/request", request.getPath().value());
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals(HttpMethod.POST, method);
        assertEquals("Bar", request.getHeaders().getFirst("Foo"));
        return request.getBody().map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        });
    }

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") @Pattern(regexp = "\\D+") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @PostMapping("/greeting")
    public Greeting greetingByPost(@RequestBody Greeting greeting) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, greeting.getContent()));
    }

    @DeleteMapping("/greeting")
    public ResponseEntity<?> deleteGreeting() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Foo", "Bar");
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }


    @RequestMapping("/greeting-status")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Greeting greetingWithStatus(@RequestParam(value="name", defaultValue="World") @Pattern(regexp = "\\D+") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }
}