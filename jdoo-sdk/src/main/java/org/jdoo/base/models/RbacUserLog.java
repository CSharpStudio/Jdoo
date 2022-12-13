package org.jdoo.base.models;

import org.jdoo.*;

/**
 * 用户日志
 * 
 * @author lrz
 */
@Model.Service(remove = { "create", "createBatch", "delete", "update", "copy" })
@Model.Meta(name = "rbac.user.log", label = "用户日志")
public class RbacUserLog extends Model {
}
