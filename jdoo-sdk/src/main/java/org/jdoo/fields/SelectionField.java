package org.jdoo.fields;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jdoo.core.Constants;
import org.jdoo.core.MetaField;
import org.jdoo.core.MetaModel;
import org.jdoo.data.ColumnType;
import org.jdoo.data.SqlDialect;
import org.jdoo.Records;
import org.jdoo.Selection;

/**
 * 选择
 * 
 * @author lrz
 */
public class SelectionField extends BaseField<SelectionField> {
    
    @JsonIgnore
    Selection selection;
    @JsonIgnore
    Map<String, String> options;

    public SelectionField() {
        type = Constants.SELECTION;
        columnType = ColumnType.VarChar;
    }

    @Override
    public String getDbColumnType(SqlDialect sqlDialect) {
        return sqlDialect.getColumnType(columnType, 240, null);
    }

    public Map<String, String> getOptions(Records rec) {
        if (options == null) {
            options = selection.get(rec);
        }
        return options;
    }

    public SelectionField selection(Selection selection) {
        args.put("selection", selection);
        return this;
    }

    public SelectionField selection(Map<String, String> selection) {
        args.put("selection", Selection.value(selection));
        return this;
    }

    public SelectionField addSelection(Map<String, String> toAdd) {
        args.put("selection_add", toAdd);
        return this;
    }

    @Override
    protected Map<String, Object> getAttrs(MetaModel model, String name) {
        Map<String, Object> attrs = super.getAttrs(model, name);
        attrs.remove("selection_add");
        return attrs;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setupAttrs(MetaModel model, String name) {
        super.setupAttrs(model, name);
        Map<String, String> toAdd = new HashMap<>(16);
        for (MetaField field : resolveMro(model, name)) {
            if (field instanceof SelectionField) {
                SelectionField sf = (SelectionField) field;
                Map<String, String> newValues = (Map<String, String>) sf.args.get("selection_add");
                if (newValues != null) {
                    toAdd.putAll(newValues);
                }
            }
        }
        if (selection == null) {
            selection = Selection.value(toAdd);
        } else {
            selection.add(toAdd);
        }
    }
}
