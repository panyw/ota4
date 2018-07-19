package com.fotile.c2i.ota.util;

/**
 * @author ： panyw .
 * @date ：2018/4/2 18:04
 * @COMPANY ： Fotile智能厨电研究院
 * @description ：
 */

public class InfoBean
{
    private boolean ota_state;
    private String recipes_url;
    private int version_code;
    private boolean is_debug = false;
    public InfoBean(){
        this.ota_state = true;
        this.recipes_url = "";
        this.version_code = -1;
        this.is_debug = false;
    }
    public InfoBean(boolean ota_state ,String recipes_url,int version_code){
        this.ota_state = ota_state;
        this.recipes_url = recipes_url;
        this.version_code = version_code;
    }
    public InfoBean(boolean ota_state ,String recipes_url,int version_code,boolean is_debug){
        this.ota_state = ota_state;
        this.recipes_url = recipes_url;
        this.version_code = version_code;
        this.is_debug = is_debug;
    }
    public boolean isOta_state() {
        return ota_state;
    }

    public void setOta_state(boolean ota_state) {
        this.ota_state = ota_state;
    }

    public String getRecipes_url() {
        return recipes_url;
    }

    public void setRecipes_url(String recipes_url) {
        this.recipes_url = recipes_url;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public boolean isIs_debug() {
        return is_debug;
    }

    public void setIs_debug(boolean is_debug) {
        this.is_debug = is_debug;
    }
}
