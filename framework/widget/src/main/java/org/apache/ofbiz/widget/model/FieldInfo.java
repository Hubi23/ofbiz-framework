/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.widget.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilXml;
import org.apache.ofbiz.widget.renderer.FormStringRenderer;
import org.w3c.dom.Element;

/**
 * Form field abstract class.
 */
public abstract class FieldInfo {

    public static final String module = FieldInfo.class.getName();

    public static final int DISPLAY = 1;
    public static final int HYPERLINK = 2;
    public static final int TEXT = 3;
    public static final int TEXTAREA = 4;
    public static final int DATE_TIME = 5;
    public static final int DROP_DOWN = 6;
    public static final int CHECK = 7;
    public static final int RADIO = 8;
    public static final int SUBMIT = 9;
    public static final int RESET = 10;
    public static final int HIDDEN = 11;
    public static final int IGNORED = 12;
    public static final int TEXTQBE = 13;
    public static final int DATEQBE = 14;
    public static final int RANGEQBE = 15;
    public static final int LOOKUP = 16;
    public static final int FILE = 17;
    public static final int PASSWORD = 18;
    public static final int IMAGE = 19;
    public static final int DISPLAY_ENTITY = 20;
    public static final int CONTAINER = 21;
    public static final int MENU = 22;
    public static final int FORM = 23;
    public static final int GRID = 24;
    public static final int SCREEN = 25;
    // the numbering here represents the priority of the source;
    //when setting a new fieldInfo on a modelFormField it will only set
    //the new one if the fieldSource is less than or equal to the existing
    //fieldSource, which should always be passed as one of the following...
    public static final int SOURCE_EXPLICIT = 1;
    public static final int SOURCE_AUTO_ENTITY = 2;
    public static final int SOURCE_AUTO_SERVICE = 3;
    private static Map<String, Integer> fieldTypeByName = createFieldTypeMap();
    private static List<Integer> nonInputFieldTypeList = createNonInputFieldTypeList();

    private static Map<String, Integer> createFieldTypeMap() {
        return Map.ofEntries(Map.entry("display", 1), Map.entry("hyperlink", 2), Map.entry("text", 3), Map.entry("textarea", 4), Map.entry("date-time", 5), Map.entry("drop-down", 6), Map.entry("check", 7), Map.entry("radio", 8), Map.entry("submit", 9), Map.entry("reset", 10), Map.entry("hidden", 11), Map.entry("ignored", 12), Map.entry("text-find", 13), Map.entry("date-find", 14), Map.entry("range-find", 15), Map.entry("lookup", 16), Map.entry("file", 17), Map.entry("password", 18), Map.entry("image", 19), Map.entry("display-entity", 20), Map.entry("container", 21), Map.entry("include-menu", 22), Map.entry("include-form", 23), Map.entry("include-grid", 24), Map.entry("include-screen", 25));
    }

    private static List<Integer> createNonInputFieldTypeList() {
        return List.of(FieldInfo.IGNORED, FieldInfo.HIDDEN, FieldInfo.DISPLAY, FieldInfo.DISPLAY_ENTITY, FieldInfo.HYPERLINK, FieldInfo.MENU, FieldInfo.FORM, FieldInfo.GRID, FieldInfo.SCREEN);
    }

    public static int findFieldTypeFromName(String name) {
        Integer fieldTypeInt = FieldInfo.fieldTypeByName.get(name);
        if (fieldTypeInt != null) {
            return fieldTypeInt;
        }
        throw new IllegalArgumentException("Could not get fieldType for field type name " + name);
    }

    public static boolean isInputFieldType(Integer fieldType) {
        return ! nonInputFieldTypeList.contains(fieldType);
    }

    private final int fieldType;
    private final int fieldSource;
    private final ModelFormField modelFormField;

    /** XML Constructor */
    protected FieldInfo(Element element, ModelFormField modelFormField) {
        this.fieldSource = FieldInfo.SOURCE_EXPLICIT;
        this.fieldType = findFieldTypeFromName(UtilXml.getTagNameIgnorePrefix(element));
        this.modelFormField = modelFormField;
    }

    /** Value Constructor */
    protected FieldInfo(int fieldSource, int fieldType, ModelFormField modelFormField) {
        this.fieldType = fieldType;
        this.fieldSource = fieldSource;
        this.modelFormField = modelFormField;
    }

    public abstract void accept(ModelFieldVisitor visitor) throws Exception;

    /**
     * Returns a new instance of this object.
     *
     * @param modelFormField
     */
    public abstract FieldInfo copy(ModelFormField modelFormField);

    public int getFieldSource() {
        return fieldSource;
    }

    public int getFieldType() {
        return fieldType;
    }

    public ModelFormField getModelFormField() {
        return modelFormField;
    }

    public abstract void renderFieldString(Appendable writer, Map<String, Object> context, FormStringRenderer formStringRenderer)
            throws IOException;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ModelFieldVisitor visitor = new XmlWidgetFieldVisitor(sb);
        try {
            accept(visitor);
        } catch (Exception e) {
            Debug.logWarning(e, "Exception thrown in XmlWidgetFieldVisitor: ", module);
        }
        return sb.toString();
    }
}
