package jdoo.base;

import jdoo.modules.Registry;

public class __init__ {
    public static void init(Registry registry){
        registry.register(IrModelAccess.class);
        registry.register(IrDefault.class);

        registry.register(PartnerCategory.class);
        registry.register(Partner.class);
        registry.register(ResBank.class);
        registry.register(ResUsers.class);
    }
}
