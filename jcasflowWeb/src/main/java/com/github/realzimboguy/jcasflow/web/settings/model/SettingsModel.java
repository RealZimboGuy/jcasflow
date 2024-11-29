package com.github.realzimboguy.jcasflow.web.settings.model;

public class SettingsModel {

	private String settingKey;
	private String settingValue;

	public SettingsModel() {

	}

	public SettingsModel(String settingKey, String settingValue) {

		this.settingKey = settingKey;
		this.settingValue = settingValue;
	}

	public String getSettingKey() {

		return settingKey;
	}

	public void setSettingKey(String settingKey) {

		this.settingKey = settingKey;
	}

	public String getSettingValue() {

		return settingValue;
	}

	public void setSettingValue(String settingValue) {

		this.settingValue = settingValue;
	}
}
