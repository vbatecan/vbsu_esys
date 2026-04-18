package com.group5.paul_esys.utils;

import com.group5.paul_esys.modules.config.Config;
import com.group5.paul_esys.modules.config.ConnectionType;

public final class SqlDialectUtil {

  private SqlDialectUtil() {
  }

  public static boolean isMySql() {
    return Config.CONNECTION_TYPE == ConnectionType.MYSQL;
  }

  public static String limitOneClause() {
    return isMySql() ? " LIMIT 1" : " FETCH FIRST 1 ROWS ONLY";
  }
}
