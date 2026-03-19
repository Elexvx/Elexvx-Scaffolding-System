package top.elexvx.admin.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/captcha", "/auth/captcha"})
public class AjCaptchaController {
  private final com.anji.captcha.service.CaptchaService captchaService;

  public AjCaptchaController(com.anji.captcha.service.CaptchaService captchaService) {
    this.captchaService = captchaService;
  }

  @PostMapping("/get")
  public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
    data.setBrowserInfo(getRemoteId(request));
    return captchaService.get(data);
  }

  @PostMapping("/check")
  public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
    data.setBrowserInfo(getRemoteId(request));
    return captchaService.check(data);
  }

  private static String getRemoteId(HttpServletRequest request) {
    String xfwd = request.getHeader("X-Forwarded-For");
    String ip = getRemoteIpFromXfwd(xfwd);
    String ua = request.getHeader("user-agent");
    if (StringUtils.isNotBlank(ip)) {
      return ip + ua;
    }
    return request.getRemoteAddr() + ua;
  }

  private static String getRemoteIpFromXfwd(String xfwd) {
    if (StringUtils.isNotBlank(xfwd)) {
      String[] ipList = xfwd.split(",");
      return StringUtils.trim(ipList[0]);
    }
    return null;
  }
}
