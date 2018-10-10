package beauty.requestable.util.serialize.parser;

public interface JSONFieldParser<T> {
	public T parse(JSONFieldInfo field);
}
