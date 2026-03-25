package com.group5.paul_esys.modules.users.models.user;

import com.group5.paul_esys.modules.users.models.enums.Role;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import at.favre.lib.crypto.bcrypt.BCrypt.Verifyer;

public class UserInformation {
  private Long id;
  private String email;
  private String password;
  private Role role;

  public boolean verifyPassword(String plainTextPassword) {
    Verifyer verifyer = BCrypt.verifyer();
    Result result = verifyer.verify(plainTextPassword.toCharArray(), this.password);
    return result.verified;
  }

  public UserInformation() {
  }

  public UserInformation(Long id, String email, String password, Role role) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public UserInformation setId(Long id) {
    this.id = id;
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
