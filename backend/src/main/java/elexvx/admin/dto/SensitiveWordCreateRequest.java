package elexvx.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class SensitiveWordCreateRequest {
  @NotBlank(message = "敏感词不能为空")
  private String word;

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }
}
