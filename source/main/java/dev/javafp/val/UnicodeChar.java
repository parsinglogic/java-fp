package dev.javafp.val;

import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;

public class UnicodeChar extends ImValuesImpl
{

    // the code point of this character
    public final int codePoint;

    public UnicodeChar(int codePoint)
    {
        this.codePoint = codePoint;
    }

    public static UnicodeChar on(int codePoint)
    {
        Throw.Exception.ifFalse(Character.isValidCodePoint(codePoint), "" + codePoint + "is not a valid code point");
        return new UnicodeChar(codePoint);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(codePoint);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("codePoint");
    }
}
