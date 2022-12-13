package org.jdoo;

import org.jdoo.core.BaseModel;

public class TransientModel extends BaseModel {
    public TransientModel() {
        isAuto = true;
        isAbstract = false;
        isTransient = true;
    }
}
