package com.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * ModelAndView - Support des vues + données du modèle (Sprint 5)
 */
public class ModelAndView {

    private String view;
    private Map<String, Object> data = new HashMap<>();

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView addAttribute(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public Map<String, Object> getData() {
        return data;
    }
}