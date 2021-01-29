package jdoo.models;

public class TransientModel extends Model {
    public TransientModel() {
        _auto = true; // automatically create database backend
        _register = false; // not visible in ORM registry, meant to be python-inherited only
        _abstract = false; // not abstract
        _transient = true; // transient
    }
}
