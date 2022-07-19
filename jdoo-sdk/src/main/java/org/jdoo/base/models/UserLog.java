package org.jdoo.base.models;

import org.jdoo.*;

/**
 * 用户日志
 * 
 * @author lrz
 */
@Model.Service(remove = "create")
@Model.Service(remove = "createBatch")
@Model.Service(remove = "delete")
@Model.Service(remove = "update")
@Model.Service(remove = "name_get")
@Model.Meta(name = "rbac.user.log", description = "用户日志")
public class UserLog extends Model {
}
