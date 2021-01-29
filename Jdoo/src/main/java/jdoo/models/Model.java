package jdoo.models;

public class Model extends BaseModel {
    public Model() {
        _auto = true; // automatically create database backend
        _register = false; // not visible in ORM registry, meant to be python-inherited only
        _abstract = false; // not abstract
        _transient = false; // not transient
    }
}