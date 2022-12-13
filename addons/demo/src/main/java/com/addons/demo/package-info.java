@org.jdoo.Manifest(name = "demo", 
category = "示例", 
label = "示例", 
models = { 
        Book.class,
        Category.class,
        Many2One.class,
        One2Many.class,
        Many2Many.class,
        FieldType.class,
        Factory.class,
        Workshop.class,
        Equipment.class,
        Partner.class,
        Customer.class,
        Supplier.class,
}, 
data = {
        "views/field_type_views.xml",
        "views/book_views.xml",
        "views/manufacturing_views.xml",
        "views/delegate_views.xml",
        "data/book_data.xml",
        "data/field_type_data.xml",
        "data/manufacturing_data.xml"
})
package com.addons.demo;

import com.addons.demo.models.book.*;
import com.addons.demo.models.fields.*;
import com.addons.demo.models.manufacturing.*;
import com.addons.demo.models.delegate.*;
