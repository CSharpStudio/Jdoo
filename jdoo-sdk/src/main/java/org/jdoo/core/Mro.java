package org.jdoo.core;

import java.util.ArrayList;
import java.util.List;

import org.jdoo.exceptions.TypeException;

/**
 * Method Resolve Order (MRO) - 类对象属性的解析顺序
 * 
 * @author lrz
 */
public class Mro {
    public static List<MetaModel> calculate(MetaModel startingType, List<MetaModel> bases) {
        if (bases.contains(startingType)) {
            throw new TypeException(
                    String.format("a __bases__ item causes an inheritance cycle (%s)", startingType.name));
        }
        List<MetaModel> mro = new ArrayList<>();
        mro.add(startingType);
        if (bases.size() != 0) {
            List<List<MetaModel>> mroList = new ArrayList<>();
            for (MetaModel dt : bases) {
                // mroList.add(new ArrayList<MetaModel>(Arrays.asList(dt)));
                mroList.add(new ArrayList<MetaModel>(dt.getMro()));
            }
            mroList.add(new ArrayList<MetaModel>(bases));
            for (;;) {
                boolean removed = false;
                boolean sawNonZero = false;
                MetaModel lastHead = null;
                for (int i = 0; i < mroList.size(); i++) {
                    if (mroList.get(i).size() == 0) {
                        continue;
                    }
                    sawNonZero = true;
                    MetaModel head = lastHead = mroList.get(i).get(0);
                    boolean inTail = false;
                    for (int j = 0; j < mroList.size(); j++) {
                        List<?> list = mroList.get(j);
                        if (list.size() != 0 && !list.get(0).equals(head) && list.contains(head)) {
                            inTail = true;
                            break;
                        }
                    }

                    if (!inTail) {
                        if (mro.contains(head)) {
                            throw new TypeException("a __bases__ item causes an inheritance cycle");
                        }
                        mro.add(head);
                        for (int j = 0; j < mroList.size(); j++) {
                            mroList.get(j).remove(head);
                        }
                        removed = true;
                        break;
                    }
                }
                if (!sawNonZero) {
                    break;
                }
                if (!removed) {
                    MetaModel other = null;
                    String error = String.format(
                            "Cannot create a consistent method resolution\norder (MRO) for bases %s", lastHead.name);
                    for (int i = 0; i < mroList.size(); i++) {
                        List<MetaModel> list = mroList.get(i);
                        if (list.size() != 0 && !list.get(0).equals(lastHead)) {
                            other = list.get(0);
                            error += ", ";
                            error += other.name;
                        }
                    }
                    throw new TypeException(error);
                }
            }
        }
        return mro;
    }
}
