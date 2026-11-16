package com.cinematography.gateway.config;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Hooks;

@Slf4j
@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class AppConfig {

	@Value("${server.ssl.key-store}")
    private Resource trustStore;

	@Value("${server.ssl.key-store-password}")
    private String trustStorePassword;

	@LoadBalanced // <-- add this annotation to enable client-side load balancing for RestTemplate
    @Bean
    RestTemplate restTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		SSLContext sslContext = new SSLContextBuilder()
				.loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
				.build();

		// HostnameVerificationPolicy.CLIENT is required, not optional. Passing only a
		// HostnameVerifier defaults the policy to BOTH, which switches on JSSE endpoint
		// identification inside the TLS handshake — NoopHostnameVerifier never runs and
		// calls to https://TOKEN-API fail with "No subject alternative names matching
		// IP address". The old SSLConnectionSocketFactory had no built-in check, so the
		// verifier alone was enough; DefaultClientTlsStrategy is not equivalent.
		TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(
				sslContext, HostnameVerificationPolicy.CLIENT, NoopHostnameVerifier.INSTANCE);

		final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder
				.create()
				.setTlsSocketStrategy(tlsSocketStrategy)
				.build();

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.setConnectionManager(cm)
				.build();
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

		return new RestTemplate(requestFactory);
    }

	@Bean
	@LoadBalanced
	WebClient.Builder loadBalancedWebClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	WebClient webClient(WebClient.Builder builder, ObservationRegistry registry) {
		return builder
				.observationRegistry(registry)
				.filter((request, next) -> {
				log.info("Resolved request -> Method: {}, URL: {}", request.method(), request.url());
				return next.exchange(request);
			})
			.build();
	}


	@PostConstruct
    public void setupReactorTracing() {
        Hooks.enableAutomaticContextPropagation(); // 🔥 Required for WebFlux + OTEL
    }
}