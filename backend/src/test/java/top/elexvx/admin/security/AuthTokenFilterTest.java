package top.elexvx.admin.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import top.elexvx.admin.service.ConcurrentLoginService;
import top.elexvx.admin.service.VerificationCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

class AuthTokenFilterTest {

  @Test
  void resolveTokenWithoutAuthorizationHeaderSkipsConcurrentLoginLookup() {
    ConcurrentLoginService concurrentLoginService = new ConcurrentLoginService(new VerificationCacheService());
    try {
      AuthTokenFilter filter = new AuthTokenFilter(null, concurrentLoginService);

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRequestURI("/api/system/user/page");

      String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

      assertNull(token);
    } finally {
      concurrentLoginService.shutdownHeartbeatExecutor();
    }
  }

  @Test
  void resolveTokenWithAuthorizationHeaderReturnsBearerToken() {
    ConcurrentLoginService concurrentLoginService = new ConcurrentLoginService(new VerificationCacheService());
    try {
      AuthTokenFilter filter = new AuthTokenFilter(null, concurrentLoginService);

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRequestURI("/api/system/user/page");
      request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer abc");

      String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

      assertEquals("abc", token);
    } finally {
      concurrentLoginService.shutdownHeartbeatExecutor();
    }
  }

  @Test
  void concurrentStreamResolvesTokenFromTicket() {
    ConcurrentLoginService concurrentLoginService = new ConcurrentLoginService(new VerificationCacheService());
    try {
      String ticket = concurrentLoginService.issueConcurrentStreamTicket("Bearer abc");
      AuthTokenFilter filter = new AuthTokenFilter(null, concurrentLoginService);

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRequestURI("/api/auth/concurrent/stream");
      request.setParameter("ticket", ticket);

      String token = (String) ReflectionTestUtils.invokeMethod(filter, "resolveToken", request);

      assertEquals("abc", token);
    } finally {
      concurrentLoginService.shutdownHeartbeatExecutor();
    }
  }
}
