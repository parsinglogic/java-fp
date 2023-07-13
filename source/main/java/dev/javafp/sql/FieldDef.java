/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.sql;

import dev.javafp.ex.Throw;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FieldDef extends ImValuesImpl
{

    public final String name;
    public final String columnName;
    public final SqlType type;
    public final Field field;

    private FieldDef(String name, String columnName, SqlType type, Field field)
    {
        this.name = name;
        this.columnName = columnName;
        this.type = type;
        this.field = field;

    }

    public Object getValue(Object thing)
    {
        return getValue(field, thing);
    }

    private Object getValue(Field field, Object thing)
    {
        try
        {
            return field.get(thing);
        } catch (IllegalAccessException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public Object fromResultSet(ResultSet rs)
    {
        try
        {
            switch (type)
            {
            case STRING:
                return rs.getString(columnName);

            case INTEGER:
                return rs.getInt(columnName);
            }

        } catch (SQLException e)
        {
            throw new UnexpectedChecked(e);
        }

        return Throw.Exception.ifYouGetHere();
    }

    public void setValue(PreparedStatement ps, int i, Object thing)
    {
        try
        {
            switch (type)
            {
            case STRING:
                ps.setString(i, (String) getValue(thing));
                break;

            case INTEGER:
                ps.setInt(i, (Integer) getValue(thing));
                break;
            }

        } catch (Exception e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public enum SqlType
    {
        STRING,
        INTEGER
    }

    public static FieldDef on(String name, String columnName, SqlType type, Field field)
    {
        return new FieldDef(name, columnName, type, field);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("name", "columnName", "type", "field");
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(name, columnName, type, field);
    }
}