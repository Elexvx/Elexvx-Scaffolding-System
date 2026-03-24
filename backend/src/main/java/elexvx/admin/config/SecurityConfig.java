package elexvx.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import elexvx.admin.security.AuthTokenFilter;
import elexvx.admin.web.ApiResponses;
import elexvx.admin.vo.ApiResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {
  private final AuthTokenFilter authTokenFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(AuthTokenFilter authTokenFilter, ObjectMapper objectMapper) {
    this.authTokenFilter = authTokenFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
      .csrf(AbstractHttpConfigurer::disable)
      .headers(headers -> headers
        .frameOptions(frame -> frame.sameOrigin())
        .contentTypeOptions(withDefaults())
        .cacheControl(withDefaults())
        .contentSecurityPolicy(csp -> csp.policyDirectives(
          "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
            + "script-src 'self' 'unsafe-inline'; connect-src 'self'; font-src 'self' data:"
        ))
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=(), camera=(), payment=()"))
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.UNAUTHORIZED);
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.FORBIDDEN);
        })
      )
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .headers(headers -> headers
        .frameOptions(frame -> frame.deny())
        .contentTypeOptions(withDefaults())
        .cacheControl(withDefaults())
        .httpStrictTransportSecurity(hsts -> hsts
          .includeSubDomains(true)
          .preload(true)
          .maxAgeInSeconds(31536000)
        )
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=(), camera=(), payment=()"))
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
        .requestMatchers(
          "/modules/**",
          "/auth/login",
          "/auth/login/sms",
          "/auth/login/email",
          "/auth/sms/send",
          "/auth/email/send",
          "/auth/login/confirm",
          "/auth/login/pending/stream",
          "/auth/register",
          "/auth/captcha",
          "/auth/captcha/**",
          "/captcha/**",
          "/auth/password/reset",
          "/auth/logout",
          "/system/ui/public",
          "/system/watermark",
          "/health",
          "/error",
          "/favicon.ico"
        ).permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.UNAUTHORIZED);
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          if (response.isCommitted()) return;
          writeError(response, HttpStatus.FORBIDDEN);
        })
      )
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable);

    http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private void writeError(HttpServletResponse response, HttpStatus status) throws IOException {
    if (response.isCommitted()) return;
    boolean unauthorized = status == HttpStatus.UNAUTHORIZED;
    ApiResponse<Void> body = unauthorized
      ? ApiResponses.unauthorized("UNAUTHORIZED", "未登录或登录已失效，请重新登录")
      : ApiResponses.forbidden("FORBIDDEN", "权限不足，请联系管理员开通权限后重试");
    ApiResponses.writeJson(response, status, body, objectMapper);
  }
}
