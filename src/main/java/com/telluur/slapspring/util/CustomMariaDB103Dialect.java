package com.telluur.slapspring.util;

import org.hibernate.dialect.MariaDB103Dialect;

public class CustomMariaDB103Dialect extends MariaDB103Dialect {

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }
}
