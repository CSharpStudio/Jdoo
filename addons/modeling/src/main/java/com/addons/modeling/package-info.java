
@org.jdoo.Manifest(name = "modeling", category = "开发", label = "建模工具", models = {
                Diagram.class,
                DesignModel.class,
                DesignModelField.class,
                DiagramShape.class,
                IrModule.class
}, controllers = {
                CodeController.class
}, data = {
                "views/diagram.xml"
})
package com.addons.modeling;

import com.addons.modeling.controllers.CodeController;
import com.addons.modeling.models.*;
