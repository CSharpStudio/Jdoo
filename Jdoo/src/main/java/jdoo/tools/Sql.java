package jdoo.tools;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import jdoo.data.Cursor;
import jdoo.util.Default;
import jdoo.util.Dict;
import jdoo.util.Tuple;
import jdoo.util.Utils;

public class Sql {
    private static Logger _schema = LogManager.getLogger("jdoo.schema");
    static Map<String, String> _TABLE_KIND = Utils.map("BASE TABLE", "r").map("VIEW", "v").map("FOREIGN TABLE", "f")
            .map("LOCAL TEMPORARY", "t").build();

    static Map<String, String> _CONFDELTYPES = Utils.map("RESTRICT", "r").map("NO ACTION", "a").map("CASCADE", "c")
            .map("SET NULL", "n").map("SET DEFAULT", "d").build();

    public static List<String> existing_tables(Cursor cr, Collection<String> tablenames) {
        String query = "SELECT c.relname\n" //
                + "  FROM pg_class c\n" //
                + "  JOIN pg_namespace n ON (n.oid = c.relnamespace)\n"//
                + " WHERE c.relname in %s\n" //
                + "   AND c.relkind IN ('r', 'v', 'm')\n" //
                + "   AND n.nspname = 'public'\n";
        cr.execute(query, new Tuple<>(tablenames));
        List<String> result = new ArrayList<>();
        for (Tuple<?> row : cr.fetchall()) {
            result.add((String) row.get(0));
        }
        return result;
    }

    public static boolean table_exists(Cursor cr, String tablename) {
        return existing_tables(cr, Arrays.asList(tablename)).size() == 1;
    }

    public static String table_kind(Cursor cr, String tablename) {
        String query = "SELECT table_type FROM information_schema.tables WHERE table_name=%s";
        cr.execute(query, new Tuple<>(tablename));
        return cr.rowcount() > 0 ? _TABLE_KIND.get(cr.fetchone().get(0)) : null;
    }

    public static void create_model_table(Cursor cr, String tablename, @Default String comment) {
        cr.execute(MessageFormat.format("CREATE TABLE \"{0}\" (id VARCHAR(36) NOT NULL, PRIMARY KEY(id))", tablename));
        if (StringUtils.hasText(comment)) {
            cr.execute(MessageFormat.format("COMMENT ON TABLE \"{0}\" IS %s", tablename), new Tuple<>(comment));
        }
        _schema.debug("Table {}: created", tablename);
    }

    public static Map<String, Dict> table_columns(Cursor cr, String tablename) {
        String query = "SELECT column_name, udt_name, character_maximum_length, is_nullable"//
                + " FROM information_schema.columns WHERE table_name=%s";
        cr.execute(query, new Tuple<>(tablename));
        Map<String, Dict> result = new HashMap<>();
        for (Dict row : cr.dictfetchall()) {
            result.put((String) row.get("column_name"), row);
        }
        return result;
    }

    public static boolean column_exists(Cursor cr, String tablename, String columnname) {
        String query = "SELECT 1 FROM information_schema.columns WHERE table_name=%s AND column_name=%s";
        cr.execute(query, new Tuple<>(tablename, columnname));
        return cr.rowcount() > 0;
    }

    public static void create_column(Cursor cr, String tablename, String columnname, String columntype,
            @Default String comment) {
        String coldefault = columntype.toUpperCase() == "BOOLEAN" ? "DEFAULT false" : "";
        cr.execute(MessageFormat.format("ALTER TABLE \"{0}\" ADD COLUMN \"{1}\" {2} {3}", tablename, columnname,
                columntype, coldefault));
        if (StringUtils.hasText(comment)) {
            cr.execute(MessageFormat.format("COMMENT ON COLUMN \"{0}\".\"{1}\" IS %s", tablename, columnname),
                    new Tuple<>(comment));
        }
        _schema.debug("Table {}: added column {} of type {}", tablename, columnname, columntype);
    }

    public static void rename_column(Cursor cr, String tablename, String columnname1, String columnname2) {
        cr.execute(MessageFormat.format("ALTER TABLE \"{0}\" RENAME COLUMN \"{1}\" TO \"{2}\"", tablename, columnname1,
                columnname2));
        _schema.debug("Table {}: renamed column {} to {}", tablename, columnname1, columnname2);
    }

    public static void convert_column(Cursor cr, String tablename, String columnname, String columntype) {

    }

    public static void set_not_null(Cursor cr, String tablename, String columnname) {

    }

    public static void drop_not_null(Cursor cr, String tablename, String columnname) {
        cr.execute(
                MessageFormat.format("ALTER TABLE \"{0}\" ALTER COLUMN \"{1}\" DROP NOT NULL", tablename, columnname));
        _schema.debug("Table {}: column {}: dropped constraint NOT NULL", tablename, columnname);
    }

    public static String constraint_definition(Cursor cr, String tablename, String constraintname) {
        String query = "SELECT COALESCE(d.description, pg_get_constraintdef(c.oid))\n" //
                + "FROM pg_constraint c\n" //
                + "JOIN pg_class t ON t.oid = c.conrelid\n" //
                + "LEFT JOIN pg_description d ON c.oid = d.objoid\n" //
                + "WHERE t.relname = %s AND conname = %s;";
        cr.execute(query, new Tuple<>(tablename, constraintname));
        return cr.rowcount() > 0 ? (String) cr.fetchone().get(0) : null;
    }

    public static void add_constraint(Cursor cr, String tablename, String constraintname, String definition) {

    }

    public static void drop_constraint(Cursor cr, String tablename, String constraintname) {

    }

    public static void add_foreign_key(Cursor cr, String tablename1, String columnname1, String tablename2,
            String columnname2, String ondelete) {
        String query = "ALTER TABLE \"{0}\" ADD FOREIGN KEY (\"{1}\") REFERENCES \"{2}\"(\"{3}\") ON DELETE {}";
        cr.execute(MessageFormat.format(query, tablename1, columnname1, tablename2, columnname2, ondelete));
        _schema.debug("Table {}: added foreign key {} references {}({}) ON DELETE {}", tablename1, columnname1,
                tablename2, columnname2, ondelete);
    }

    public static boolean fix_foreign_key(Cursor cr, String tablename1, String columnname1, String tablename2,
            String columnname2, String ondelete) {
        String deltype = _CONFDELTYPES.getOrDefault(ondelete.toUpperCase(), "a");
        String query = "SELECT con.conname, c2.relname, a2.attname, con.confdeltype as deltype\n"//
                + "  FROM pg_constraint as con, pg_class as c1, pg_class as c2,\n"//
                + "	   pg_attribute as a1, pg_attribute as a2\n"//
                + " WHERE con.contype='f' AND con.conrelid=c1.oid AND con.confrelid=c2.oid\n"
                + "   AND array_lower(con.conkey, 1)=1 AND con.conkey[1]=a1.attnum\n"//
                + "   AND array_lower(con.confkey, 1)=1 AND con.confkey[1]=a2.attnum\n"//
                + "   AND a1.attrelid=c1.oid AND a2.attrelid=c2.oid\n"//
                + "   AND c1.relname=%s AND a1.attname=%s\n";//
        cr.execute(query, new Tuple<>(tablename1, columnname1));
        boolean found = false;
        for (Tuple<?> fk : cr.fetchall()) {
            if (!found && fk.get(1).equals(tablename2) && fk.get(2).equals(columnname2) && fk.get(3).equals(deltype)) {
                found = true;
            } else {
                drop_constraint(cr, tablename1, (String) fk.get(0));
            }
        }
        if (!found) {
            add_foreign_key(cr, tablename1, columnname1, tablename2, columnname2, ondelete);
            return true;
        }
        return false;
    }

    public static boolean index_exists(Cursor cr, String indexname) {
        cr.execute("SELECT 1 FROM pg_indexes WHERE indexname=%s", new Tuple<>(indexname));
        return cr.rowcount() > 0;
    }

    public static void create_index(Cursor cr, String indexname, String tablename, Collection<String> expressions) {
        if (index_exists(cr, indexname)) {
            return;
        }
        String args = org.apache.tomcat.util.buf.StringUtils.join(expressions, ',');
        cr.execute(MessageFormat.format("CREATE UNIQUE INDEX \"{0}\" ON \"{1}\" ({2})", indexname, tablename, args));
        _schema.debug("Table {}: created index {} ({})", tablename, indexname, args);
    }

    public static void drop_index(Cursor cr, String indexname, String tablename) {
        cr.execute(MessageFormat.format("DROP INDEX IF EXISTS \"{0}\"", indexname));
        _schema.debug("Table {}: dropped index {}", tablename, indexname);
    }

    public static void drop_view_if_exists(Cursor cr, String viewname) {
        cr.execute(String.format("DROP view IF EXISTS %s CASCADE", viewname));
    }

    public static String getValueSql(Object param) {
        if (param == null) {
            return "''";
        } else if (param instanceof String) {
            return "'" + ((String) param).replace("'", "''") + "'";
        } else if (param instanceof Date) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "'" + sf.format((Date) param) + "'";
        } else if (param instanceof Collection<?>) {
            Collection<?> c = (Collection<?>) param;
            List<String> list = new ArrayList<>(c.size());
            for (Object o : c) {
                list.add(getValueSql(o));
            }
            return "(" + org.apache.tomcat.util.buf.StringUtils.join(list) + ")";
        }
        return param.toString();
    }
}
