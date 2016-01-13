package net.ttddyy.dsproxy.test;

import net.ttddyy.dsproxy.proxy.ParameterKey;

/**
 * @author Tadaya Tsuyukubo
 */
public class ParameterKeyValue {

    public enum OperationType {
        SET_PARAM, SET_NULL, REGISTER_OUT
    }

    private ParameterKey key;
    private Object value;
    private OperationType type;

    public ParameterKeyValue(int indexKey, Object value, OperationType type) {
        this(new ParameterKey(indexKey), value, type);
    }
    public ParameterKeyValue(String nameKey, Object value, OperationType type) {
        this(new ParameterKey(nameKey), value, type);
    }
    public ParameterKeyValue(ParameterKey key, Object value, OperationType type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public ParameterKey getKey() {
        return key;
    }

    public void setKey(ParameterKey key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }
}
