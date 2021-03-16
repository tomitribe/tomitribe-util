package org.tomitribe.util.editor;

public abstract class AbstractConverter extends java.beans.PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(toObjectImpl(text));
    }

    protected abstract Object toObjectImpl(String text);
}
