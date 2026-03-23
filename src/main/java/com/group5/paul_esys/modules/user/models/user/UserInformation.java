package com.group5.paul_esys.modules.user.models.user;

import com.group5.paul_esys.modules.user.models.enums.Role;
import lombok.Data;

@Data

public class UserInformation {
  private Integer userId;
  private String email;
  private String password;
  private Role role;

  public UserInformation(int userId, String email, String password, String role) {
  }

  public Integer getUserId() {
    return userId;
  }

  public UserInformation setUserId(Integer userId) {
    this.userId = userId;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public UserInformation setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public UserInformation setPassword(String password) {
    this.password = password;
    return this;
  }

  public Role getRole() {
    return role;
  }

  public UserInformation setRole(Role role) {
    this.role = role;
    return this;
  }
}
