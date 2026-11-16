package com.cinematography.web.client;

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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * {@link RestTemplate} for service-to-service calls over HTTPS, trusting the service's own key store
 * ({@code server.ssl.key-store}).
 */
@AutoConfiguration
public class CinematographyHttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RestTemplate restTemplate(@Value("${server.ssl.key-store}") Resource trustStore,
            @Value("${server.ssl.key-store-password}") String trustStorePassword)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		SSLContext sslContext = new SSLContextBuilder()
				.loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
				.build();

		// HostnameVerificationPolicy.CLIENT is required, not optional. Passing only a
		// HostnameVerifier defaults the policy to BOTH, which switches on JSSE endpoint
		// identification inside the TLS handshake — NoopHostnameVerifier never runs and
		// outbound calls to https://SERVICE-NAME fail with "No subject alternative names
		// matching IP address". SSLConnectionSocketFactory had no built-in check, so the
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
}
