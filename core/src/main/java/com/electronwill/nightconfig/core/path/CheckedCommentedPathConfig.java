package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TheElectronWill
 */
class CheckedCommentedPathConfig extends CommentedConfigWrapper<CommentedPathConfig>
		implements CommentedPathConfig {
	/**
	 * Creates a new CheckedConfig around a commented configuration.
	 * <p>
	 * The values that are in the config when this method is called are also checked.
	 *
	 * @param config the configuration to wrap
	 */
	CheckedCommentedPathConfig(CommentedPathConfig config) {
		super(config);
	}

	@Override
	public Path getPath() {
		return config.getPath();
	}

	@Override
	public void save() {
		config.save();
	}

	@Override
	public void load() {
		config.load();
	}

	@Override
	public void close() {
		config.close();
	}

	@Override
	public CommentedPathConfig checked() {
		return this;
	}

	@Override
	public <T> T set(List<String> path, Object value) {
		return super.set(path, checkedValue(value));
	}

	@Override
	public boolean add(List<String> path, Object value) {
		return super.add(path, checkedValue(value));
	}

	@Override
	public Map<String, Object> valueMap() {
		return new TransformingMap<>(super.valueMap(), v -> v, this::checkedValue, o -> o);
	}

	@Override
	public Set<? extends CommentedConfig.Entry> entrySet() {
		return new TransformingSet<>((Set<CommentedConfig.Entry>)super.entrySet(), v -> v, this::checkedValue, o -> o);
	}

	@Override
	public String toString() {
		return "checked of " + config;
	}

	/**
	 * Checks that a value is supported by the config. Throws an unchecked exception if the value
	 * isn't supported.
	 */
	private void checkValue(Object value) {
		ConfigFormat<?> format = configFormat();
		if (value != null && !format.supportsType(value.getClass())) {
			throw new IllegalArgumentException(
					"Unsupported value type: " + value.getClass().getTypeName());
		} else if (value == null && !format.supportsType(null)) {
			throw new IllegalArgumentException(
					"Null values aren't supported by this configuration.");
		}
		if (value instanceof Config) {
			((Config)value).valueMap().forEach((k, v) -> checkValue(v));
		}
	}

	/**
	 * Checks that a value is supported by the config, and returns it if it's supported. Throws an
	 * unchecked exception if the value isn't supported.
	 */
	private <T> T checkedValue(T value) {
		checkValue(value);
		return value;
	}
}