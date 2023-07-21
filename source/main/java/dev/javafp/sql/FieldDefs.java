/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.sql;

import dev.javafp.eq.Eq;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.TextUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FieldDefs
{
    public final Class<?> klass;
    public final ImList<FieldDef> defs;
    public final ImList<Integer> primaryKeyNos;

    public FieldDefs(Class<?> klass, ImList<FieldDef> defs, ImList<Integer> primaryKeyNos)
    {
        this.klass = klass;
        this.defs = defs;
        this.primaryKeyNos = primaryKeyNos;
    }

    public static FieldDefs on(Class<?> klass, String primaryKeyNos, String... ss)
    {
        ImList<FieldDef> fs = ImList.on(ss).group(3).map(a -> FieldDef.on(a.at(1), a.at(2), FieldDef.SqlType.valueOf(a.at(3)), getField(klass, a.at(1))));

        return new FieldDefs(klass, fs, toImList(primaryKeyNos));
    }

    public static FieldDefs of(Class<?> klas)
    {
        try
        {
            Field f = klas.getField("fieldDefs");

            return (FieldDefs) f.get(klas);
        } catch (Exception e)
        {
            throw new UnexpectedChecked(e);
        }

    }

    private static ImList<Integer> toImList(String primaryKeyNos)
    {
        return ParseUtils.split(' ', primaryKeyNos).map(s -> Integer.valueOf(s));
    }

    public ImList<String> columnNames()
    {
        return defs.map(d -> d.columnName);
    }

    public ImList<FieldDef> primaryKeyFields()
    {
        return primaryKeyNos.map(i -> defs.at(i));
    }

    public ImList<String> columnNamesQuoted()
    {
        return columnNames().map(n -> TextUtils.quote(n, "\""));
    }

    public ImList<Object> getValues(Object thing)
    {
        return defs.map(def -> def.getValue(thing));
    }

    public ImList<Object> primaryKeyValues(Object thing)
    {
        return primaryKeyFields().map(def -> def.getValue(thing));
    }

    public ImList<Object> getFromResultSet(ResultSet rs)
    {
        return defs.map(def -> def.fromResultSet(rs));
    }

    private static Field getField(Class<?> klass, String name)
    {
        try
        {
            return klass.getField(name);
        } catch (NoSuchFieldException e)
        {
            throw new UnexpectedChecked("Missing field on class " + klass.getSimpleName(), e);
        }
    }

    public ImList<String> names()
    {
        return defs.map(d -> d.name);
    }

    public FieldDef get(String name)
    {
        return defs.find(d -> Eq.uals(name, d.name)).orElse(null);
    }

    public void setValues(PreparedStatement ps, int startIndex, Object thing)
    {
        // ImPair up with the indexes

        ImList<ImPair<FieldDef, Integer>> pairs = defs.zip(ImRange.step(startIndex, 1));

        pairs.foreach(p -> p.fst.setValue(ps, p.snd, thing));
    }

    //    /**
    //     * Get a list of objects from the SCD for class `klass` using `query` - This function will add the window time tests at the
    //     * emd of the query
    //     */
    //    public <A> ImList<A> getObjectsForClass( Class<A> klass)
    //    {
    //        String q = String.format("select %s from %s",
    //                columnNames().toString(", "),
    //                TableNames.getTableNameFromClass(klass));
    //
    //
    //    }
}