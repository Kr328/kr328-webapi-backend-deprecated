package com.github.kr328.webapi.api.clash.utils;

import com.github.kr328.webapi.api.clash.model.ClashPreprocessorRoot;
import com.github.kr328.webapi.api.clash.model.ClashRoot;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Map;

public class RootUtils {
    public static ClashPreprocessorRoot loadClashPreprocessorRoot(String data) {
        Constructor constructor = new Constructor(ClashPreprocessorRoot.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {setSkipMissingProperties(true);}
            @Override
            public Property getProperty(Class<?> type, String name) {
                StringBuilder result = new StringBuilder();
                for ( String s : name.split("-+") ) {
                    char[] cs = s.toCharArray();
                    if ( cs.length > 0 ) cs[0] = Character.toUpperCase(cs[0]);
                    result.append(cs);
                }
                if ( result.length() > 0 ) result.setCharAt(0 ,Character.toLowerCase(result.charAt(0)));
                return super.getProperty(type, result.toString());
            }
        });

        Yaml yaml = new Yaml(constructor);

        return yaml.loadAs(data, ClashPreprocessorRoot.class);
    }

    public static ClashRoot loadClashRoot(String data) {
        Constructor constructor = new Constructor(ClashRoot.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {setSkipMissingProperties(true);}
            @Override
            public Property getProperty(Class<?> type, String name) {
                StringBuilder result = new StringBuilder();
                for ( String s : name.split("-+") ) {
                    char[] cs = s.toCharArray();
                    if ( cs.length > 0 ) cs[0] = Character.toUpperCase(cs[0]);
                    result.append(cs);
                }
                if ( result.length() > 0 ) result.setCharAt(0 ,Character.toLowerCase(result.charAt(0)));
                return super.getProperty(type, result.toString());
            }
        });

        Yaml yaml = new Yaml(constructor);

        return yaml.loadAs(data, ClashRoot.class);
    }

    public static String dump(Map<String, Object> root) {
        Yaml yaml = new Yaml();

        return yaml.dumpAsMap(root);
    }
}
