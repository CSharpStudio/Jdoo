package jdoo.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdoo.data.Cursor;
import jdoo.exceptions.JdooException;
import jdoo.tools.Sql;

public class Db {
    static Logger _logger = LoggerFactory.getLogger(Db.class);

    public static boolean is_initialized(Cursor cr) {
        return Sql.table_exists(cr, "ir_module_module");
    }

    public static void initialize(Cursor cr) {
        InputStream f = Db.class.getResourceAsStream("/addons/base/base_data.sql");
        if (f == null) {
            String m = "File not found: 'base.sql' (provided by module 'base').";
            _logger.error(m);
            throw new JdooException(m);
        }
        try (Scanner base_sql_file = new Scanner(f).useDelimiter("\\A")) {
            cr.execute(base_sql_file.next());
        }
        // todo
    }
}
