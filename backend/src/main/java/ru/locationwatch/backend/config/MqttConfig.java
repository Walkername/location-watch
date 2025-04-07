package ru.locationwatch.backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Configuration
@EnableIntegration
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.device}")
    private String device;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() throws Exception {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        // URL брокера с SSL
        options.setServerURIs(new String[] { brokerUrl });

        // Настройка SSL контекста
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
                null, // KeyManager (не требуется для односторонней аутентификации)
                createTrustManager(), // TrustManager с сертификатом сервера
                new SecureRandom()
        );

        options.setSocketFactory(sslContext.getSocketFactory());
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setSocketFactory(sslContext.getSocketFactory());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	// Настройка адаптера для входящих сообщений
    @Bean
    public MessageProducer inboundAdapter() throws Exception {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId,
                        mqttClientFactory(),
                        "$devices/" + device + "/events");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    private TrustManager[] createTrustManager() throws Exception {
        // Загрузка сертификата из ресурсов
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca = cf.generateCertificate(
                new ClassPathResource("certs/rootca.crt").getInputStream()
        );

        // Создание KeyStore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("broker", ca);

        // Инициализация TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        return tmf.getTrustManagers();
    }
}
