package com.cinematography.web.datasource;

import java.util.concurrent.atomic.AtomicReference;

public class DynamicDatabaseContext {
	
	private DynamicDatabaseContext() {
//		This constructor is private and will not be called
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
	  
private static final AtomicReference<String> CURRENT_DB = new AtomicReference<>();

    public static void setCurrentDatabaseName(String dbName) {
        CURRENT_DB.set(dbName);
    }

    public static String getCurrentDatabaseName() {
        return CURRENT_DB.get();
    }
}

