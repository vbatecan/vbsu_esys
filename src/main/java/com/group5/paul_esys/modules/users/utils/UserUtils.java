package com.group5.paul_esys.modules.users.utils;

import com.group5.paul_esys.modules.users.models.enums.Role;
import com.group5.paul_esys.modules.users.models.user.UserInformation;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserUtils {

  public static UserInformation<?> mapResultSetToUserInformation(ResultSet rs)
    throws SQLException {
    return new UserInformation<>(
      rs.getLong("id"),
      rs.getString("email"),
      rs.getString("password"),
      Role.valueOf(rs.getString("role").toUpperCase())
    );
  }
}
