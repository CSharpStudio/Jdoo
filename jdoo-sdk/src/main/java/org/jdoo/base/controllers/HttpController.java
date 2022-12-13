package org.jdoo.base.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.jdoo.core.Environment;
import org.jdoo.https.Controller;
import org.jdoo.https.TenantResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin
@org.springframework.stereotype.Controller
public class HttpController extends Controller {
    @Autowired
    TenantResolver tenantResolver;

    @RequestMapping(value = "/{tenant}", method = RequestMethod.GET)
    public void home(HttpServletRequest request, HttpServletResponse response) {
        Environment env = getEnv();
        if (StringUtils.isEmpty(env.getUserId())) {
            redirectToLogin();
        } else {
            getEnv().get("ir.http").call("home", request, response);
        }
    }

    @RequestMapping(value = "/{tenant}/login", method = RequestMethod.GET)
    public void login(HttpServletRequest request, HttpServletResponse response) {
        getEnv().get("ir.http").call("login", request, response);
    }

    @RequestMapping(value = "/{tenant}/view", method = RequestMethod.GET)
    public void view(HttpServletRequest request, HttpServletResponse response) {
        Environment env = getEnv();
        if (StringUtils.isEmpty(env.getUserId())) {
            redirectToLogin();
        } else {
            getEnv().get("ir.http").call("view", request, response);
        }
    }
}
