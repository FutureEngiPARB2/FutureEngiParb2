package com.cvpro.competition.ModelManagement;

public class Model {

    public String name;
    public String path;
    public String label_name;
    public String label_path;
    public String type;
    public int width;
    public int height;
    public Boolean isDelete;

    public Model(String name, String path, String label_name, String label_path, String type, int width, int height, Boolean isDelete) {
        this.name = name;
        this.path = path;
        this.label_name = label_name;
        this.label_path = label_path;
        this.type = type;
        this.width = width;
        this.height = height;
        this.isDelete = isDelete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public String getLabel_name() {
        return label_name;
    }
    public void setLabel_name(String label_name) {
        this.label_name = label_name;
    }
    public String getLabel_path() {
        return label_path;
    }
    public void setLabel_path(String label_path) {
        this.label_path = label_path;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public Boolean getDelete() {
        return isDelete;
    }
    public void setDelete(Boolean delete) {
        isDelete = delete;
    }
}