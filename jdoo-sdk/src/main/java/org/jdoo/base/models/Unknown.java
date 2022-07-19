package org.jdoo.base.models;

import org.jdoo.Model;
import org.jdoo.AbstractModel;

/**
 * Abstract model used as a substitute for relational fields with an unknown
 * model_relate.
 * 
 * @author lrz
 */
@Model.Meta(name = "_unknown", description = "Unknown")
public class Unknown extends AbstractModel {

}
