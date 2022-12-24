package com.telluur.slapspring.util.jpa;

import org.hibernate.dialect.MariaDB103Dialect;

/**
 * Forces MariaDB to speak the correct unicode dialect used by Discord.
 */
public class CustomMariaDB103Dialect extends MariaDB103Dialect {

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
    }
}
