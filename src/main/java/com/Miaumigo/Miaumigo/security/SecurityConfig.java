package com.Miaumigo.Miaumigo.security;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import java.util.List;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> {
				})
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/adotantes").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/lares/cadastro").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/animais").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/animais/*").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/lares").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/lares/*/operadores").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/v1/adotantes/me/animais-recomendados").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/v1/adotantes/me/solicitacoes").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/v1/lares/me/solicitacoes").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/animais").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/animais/*/solicitacoes").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/animais/*/texto-divulgacao").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/solicitacoes/*/cancelamento").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/solicitacoes/*/aprovacao").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/solicitacoes/*/rejeicao").authenticated()
						.anyRequest().permitAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
				}))
				.build();
	}

	@Bean
	public JwtEncoder jwtEncoder(@Value("${jwt.secret}") String secret) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secret.getBytes()));
	}

	@Bean
	public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
		SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(key)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
				"http://localhost:8080",
				"http://127.0.0.1:8080",
				"http://localhost:5173",
				"http://127.0.0.1:5173",
				"http://192.168.1.13:5173",
				"http://localhost:5500",
				"http://127.0.0.1:5500",
				"https://miaumigo.onrender.com"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(false);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
