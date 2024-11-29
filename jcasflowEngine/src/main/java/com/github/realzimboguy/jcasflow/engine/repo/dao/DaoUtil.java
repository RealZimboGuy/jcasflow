package com.github.realzimboguy.jcasflow.engine.repo.dao;

import java.util.UUID;

public class DaoUtil {

	public static String getBucket(UUID id, int size){
		//return right 3 chars
		return id.toString().substring(id.toString().length() - size);
	}
}
