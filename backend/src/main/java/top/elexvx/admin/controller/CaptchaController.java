package top.elexvx.admin.controller;

import top.elexvx.admin.service.CaptchaService;
import top.elexvx.admin.vo.ApiResponse;
import top.elexvx.admin.vo.CaptchaResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class CaptchaController {
  private final CaptchaService captchaService;

  public CaptchaController(CaptchaService captchaService) {
    this.captchaService = captchaService;
  }

  @GetMapping("/captcha")
  public ApiResponse<CaptchaResult> captcha() {
    return ApiResponse.success(captchaService.generate());
  }
}
