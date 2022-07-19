package jdoo.addons.tenants.models;

import org.jdoo.*;

/**
 * 租户
 * 
 * @author lrz
 */
@Model.UniqueConstraint(name = "unique_key", fields = "key")
@Model.Meta(name = "tenants.tenants")
public class Tenants extends Model {
    static Field name = Field.Char().label("名称").help("租户名称").index(true).required(true);
    static Field key = Field.Char().label("识别码").help("租户识别码").index(true).required(true);
}
