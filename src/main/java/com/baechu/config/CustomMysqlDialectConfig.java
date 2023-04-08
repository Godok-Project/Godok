package com.baechu.config;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class CustomMysqlDialectConfig extends MySQL8Dialect {
	public CustomMysqlDialectConfig() {
		super();

		registerFunction(
			"match",
			new SQLFunctionTemplate(StandardBasicTypes.DOUBLE,"match(?1,?2) against (?3 in boolean mode)"));
	}
}
