package com.group5.paul_esys.modules.user.models.user;

import lombok.Data;

@Data
public class LoginData {

  private String email;
  private String password;

  public boolean isValid() {
    return email != null && password != null && password.length() >= 8;
  }

  public String getEmail() {
    return email;
  }

  public LoginData setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public LoginData setPassword(String password) {
    this.password = password;
    return this;
  }
}
