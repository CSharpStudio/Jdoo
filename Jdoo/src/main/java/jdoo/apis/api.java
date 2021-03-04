package jdoo.apis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdoo.exceptions.ModelException;
import jdoo.models.MethodInfo;
import jdoo.models.RecordSet;
import jdoo.util.Kvalues;
import jdoo.util.Kwargs;
import jdoo.util.Utils;

public class api {
    private static Logger _logger = LoggerFactory.getLogger(api.class);

    /**
     * Return a decorator that specifies the field dependencies of a "compute"
     * method (for new-style function fields). Each argument must be a string that
     * consists in a dot-separated sequence of field names::
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface depends {
        String[] value() default {};
    }

    /**
     * Return a decorator that specifies the context dependencies of a non-stored
     * "compute" method. Each argument is a key in the context's dictionary::
     * 
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface depends_context {
        String[] value();
    }

    /**
     * Decorate a record-style method where ``self`` is a recordset, but its
     * contents is not relevant, only the model is. Such a method::
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface model {
    }

    /**
     * Return a decorator for methods that return instances of ``model``
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface returns {
        /**
         * a model name, or "self" for the current model
         * 
         * @return
         */
        String value();

        /**
         * a function ``downgrade(self, value)`` to convert the record-style ``value``
         * to a traditional-style output
         * 
         * @return
         */
        Class<? extends Function<RecordSet, Object>> downgrade() default DefaultFunction.class;

        /**
         * a function ``upgrade(self, value)`` to convert the traditional-style
         * ``value`` to a record-style output
         * 
         * @return
         */
        Class<? extends Function<RecordSet, Object>> upgrade() default DefaultFunction.class;
    }

    /**
     * Decorate a method that takes a list of dictionaries and creates multiple
     * records. The method may be called with either a single dict or a list of
     * dicts:: <blockquote>
     * 
     * <pre>
     *record = model.create(vals) 
     *records = model.create([vals, ...])
     * </pre>
     * 
     * </blockquote>
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface model_create_multi {
    }

    @SuppressWarnings("unchecked")
    static Kvalues split_context(Kwargs kwargs) {
        Object ctx = kwargs.remove("context");
        if (ctx instanceof Map) {
            return new Kvalues((Map<String, Object>) ctx);
        } else {
            return Kvalues.empty();
        }
    }

    static Object downgrade(MethodInfo method, Object value) {
        MethodInfo.Returns spec = method.returns();
        if (spec != null && spec.downgrade() != null && value instanceof RecordSet) {
            return spec.downgrade().apply((RecordSet) value);
        }
        return value;
    }

    static Object call_kw_model(MethodInfo method, RecordSet model, List<Object> args, Kwargs kwargs) {
        Kvalues context = split_context(kwargs);
        RecordSet recs = model.with_context(context);
        debug(recs, method.getMethod().getName(), args, kwargs);
        Object result = method.invoke(recs, args, kwargs);
        return downgrade(method, result);
    }

    static Object call_kw_model_create(MethodInfo method, RecordSet model, List<Object> args, Kwargs kwargs) {
        Kvalues context = split_context(kwargs);
        RecordSet recs = model.with_context(context);
        debug(recs, method.getMethod().getName(), args, kwargs);
        RecordSet result = (RecordSet) method.invoke(recs, args, kwargs);
        return args.size() > 0 && args.get(0) instanceof Map ? result.id() : result.ids();
    }

    static Object call_kw_multi(MethodInfo method, RecordSet model, List<Object> args, Kwargs kwargs) {
        Object ids = args.isEmpty() ? args : args.get(0);
        if (!args.isEmpty()) {
            args.remove(0);
        }
        Kvalues context = split_context(kwargs);
        RecordSet recs = model.with_context(context).browse(ids);
        debug(recs, method.getMethod().getName(), args, kwargs);
        Object result = method.invoke(recs, args, kwargs);
        return downgrade(method, result);
    }

    static void debug(RecordSet model, String method, List<Object> args, Kwargs kwargs) {
        if (_logger.isDebugEnabled()) {
            List<String> params = new ArrayList<>();
            for (Object arg : args) {
                params.add(Utils.repr(arg));
            }
            for (Entry<String, Object> kw : kwargs.entrySet()) {
                params.add(kw.getKey() + "=" + Utils.repr(kw.getValue()));
            }
            _logger.debug("call {}.{}({})", model, method, String.join(",", params));
        }
    }

    public static Object call_kw(RecordSet model, String name, List<Object> args, Kwargs kwargs) {
        MethodInfo method = model.type().findMethod(name);
        if (method == null)
            throw new ModelException("Model:" + model.name() + " has no method:" + name);
        Object result;
        if (method.api() == "model") {
            result = call_kw_model(method, model, args, kwargs);
        } else if (method.api() == "model_create") {
            result = call_kw_model_create(method, model, args, kwargs);
        } else {
            result = call_kw_multi(method, model, args, kwargs);
        }
        model.flush();
        return result;
    }
}

class DefaultFunction implements Function<RecordSet, Object> {
    @Override
    public Object apply(RecordSet o) {
        return o;
    }
}
