package endpoint.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import endpoint.repository.IdRef;
import endpoint.repository.Repository;

// FIXME cyclic dependece with repository
// TODO make it not static and repository aware
public class JsonUtils {

	private static Gson buildGson(Repository r) {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(DateUtils.TIMESTAMP_FORMAT);
		builder.registerTypeAdapter(IdRef.class, new IdRefJsonAdapter(r));
		return builder.create();
	}

	public static Object from(Repository r, String json, Type type) {
		JsonElement jsoneElement = (JsonElement) new JsonParser().parse(json);
		Gson gson = buildGson(r);
		return gson.fromJson(jsoneElement, type);
	}

	public static String to(Object o) {
		Gson gson = buildGson(null);
		return gson.toJson(o);
	}

	@SuppressWarnings("unchecked")
	public static <T> T from(Repository r, String json, Class<T> clazz) {
		return (T) from(r, json, (Type) clazz);
	}

	public static Object fromMap(String json, Type keyType, Type valueType) {
		return fromMap(json, (Class<?>) keyType, (Class<?>) valueType);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> fromList(Repository r, String json, Class<T> clazz) {
		ParameterizedTypeImpl type = new ParameterizedTypeImpl(List.class, new Type[] { clazz }, null);
		return (List<T>) from(r, json, type);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> fromMap(Repository r, String json, Class<K> keyClazz, Class<V> valueClazz) {
		ParameterizedTypeImpl type = new ParameterizedTypeImpl(Map.class, new Type[] { keyClazz, valueClazz }, null);
		return (Map<K, V>) from(r, json, type);
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, List<V>> fromMapList(Repository r, String json, Class<K> keyClazz, Class<V> valueClazz) {
		Type listType = new ParameterizedTypeImpl(List.class, new Type[] { valueClazz }, null);
		Type type = new ParameterizedTypeImpl(Map.class, new Type[] { keyClazz, listType }, null);
		return (Map<K, List<V>>) from(r, json, type);
	}

	public static String readJson(BufferedReader reader) {
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return sb.toString();
	}

	public static boolean isJsonArray(String json) {
		JsonElement parsed = new JsonParser().parse(json);
		return parsed.isJsonArray();
	}
}
